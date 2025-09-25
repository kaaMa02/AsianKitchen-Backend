package ch.asiankitchen.service;

import ch.asiankitchen.model.BuffetOrder;
import ch.asiankitchen.model.CustomerOrder;
import ch.asiankitchen.model.PaymentStatus;
import ch.asiankitchen.repository.BuffetOrderRepository;
import ch.asiankitchen.repository.CustomerOrderRepository;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StripeWebhookService {

    private final CustomerOrderRepository customerRepo;
    private final BuffetOrderRepository buffetRepo;
    private final CustomerOrderService customerService;
    private final BuffetOrderService buffetService;

    public StripeWebhookService(CustomerOrderRepository customerRepo,
                                BuffetOrderRepository buffetRepo,
                                CustomerOrderService customerService,
                                BuffetOrderService buffetService) {
        this.customerRepo = customerRepo;
        this.buffetRepo = buffetRepo;
        this.customerService = customerService;
        this.buffetService = buffetService;
    }

    @Transactional
    public void handle(Event event) {
        String type = event.getType();

        // We support both PI and Checkout events — keep whichever you use.
        switch (type) {
            case "payment_intent.succeeded" -> {
                PaymentIntent pi = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (pi != null) markPaidAndNotify(pi.getId());
            }
            case "payment_intent.payment_failed" -> {
                PaymentIntent pi = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (pi != null) markFailed(pi.getId());
            }
            case "checkout.session.completed" -> {
                Session s = (Session) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (s != null && s.getPaymentIntent() != null) {
                    markPaidAndNotify(s.getPaymentIntent());
                }
            }
            default -> {
                // ignore others
            }
        }
    }

    private void markPaidAndNotify(String paymentIntentId) {
        // Try menu order first
        customerRepo.findByPaymentIntentId(paymentIntentId).ifPresent(order -> {
            if (order.getPaymentStatus() != PaymentStatus.SUCCEEDED) {
                order.setPaymentStatus(PaymentStatus.SUCCEEDED);
            }
            // send confirmation with track link
            customerService.sendCustomerConfirmationWithTrackLink(order);
        });

        // Then try buffet order
        buffetRepo.findByPaymentIntentId(paymentIntentId).ifPresent(order -> {
            if (order.getPaymentStatus() != PaymentStatus.SUCCEEDED) {
                order.setPaymentStatus(PaymentStatus.SUCCEEDED);
            }
            buffetService.sendCustomerConfirmationWithTrackLink(order);
        });
    }

    private void markFailed(String paymentIntentId) {
        customerRepo.findByPaymentIntentId(paymentIntentId).ifPresent(o -> o.setPaymentStatus(PaymentStatus.FAILED));
        buffetRepo.findByPaymentIntentId(paymentIntentId).ifPresent(o -> o.setPaymentStatus(PaymentStatus.FAILED));
    }
}
