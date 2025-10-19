package ch.asiankitchen.scheduler;

import ch.asiankitchen.model.*;
import ch.asiankitchen.repository.*;
import ch.asiankitchen.service.EmailService;
import ch.asiankitchen.service.WebPushService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OrderEscalationScheduler {

    private final CustomerOrderRepository customerOrderRepo;
    private final BuffetOrderRepository buffetOrderRepo;
    private final ReservationRepository reservationRepo;
    private final EmailService mailService;
    private final WebPushService webPush;

    @Value("${app.mail.to.escalation}")
    private String escalationEmail;

    @Value("${app.order.alert-seconds:60}")
    private int alertSeconds;

    // tick every second
    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void tick() {
        var now = LocalDateTime.now();

        // Ensure NEW orders have autoCancelAt set (safety for any legacy rows)
        customerOrderRepo.findAllByStatus(OrderStatus.NEW).forEach(o -> {
            if (o.getAutoCancelAt() == null) {
                o.setAutoCancelAt(o.getCreatedAt().plusSeconds(alertSeconds));
                customerOrderRepo.save(o);
            }
        });
        buffetOrderRepo.findAllByStatus(OrderStatus.NEW).forEach(o -> {
            if (o.getAutoCancelAt() == null) {
                o.setAutoCancelAt(o.getCreatedAt().plusSeconds(alertSeconds));
                buffetOrderRepo.save(o);
            }
        });
        reservationRepo.findAllByStatus(ReservationStatus.REQUESTED).forEach(r -> {
            if (r.getAutoCancelAt() == null) {
                r.setAutoCancelAt(r.getCreatedAt().plusSeconds(alertSeconds));
                reservationRepo.save(r);
            }
        });

        // Auto-cancel at 60s boundary + email second owner + push
        customerOrderRepo.findAllByStatus(OrderStatus.NEW).forEach(o -> {
            if (o.getAutoCancelAt() != null && !now.isBefore(o.getAutoCancelAt())
                    && o.getPaymentStatus() != PaymentStatus.SUCCEEDED) {
                o.setStatus(OrderStatus.CANCELLED);
                customerOrderRepo.save(o);
                if (escalationEmail != null && !escalationEmail.isBlank()) {
                    try {
                        mailService.sendSimple(
                                escalationEmail,
                                "Order auto-cancelled (no action in 60s)",
                                "Order %s (%s) auto-cancelled.\nCustomer: %s %s\nPhone: %s"
                                        .formatted(o.getId(), o.getOrderType(),
                                                o.getCustomerInfo().getFirstName(), o.getCustomerInfo().getLastName(),
                                                o.getCustomerInfo().getPhone()),
                                null
                        );
                    } catch (Exception ignored) {}
                }
                try { webPush.broadcast("admin", "{\"title\":\"Order auto-cancelled\",\"body\":\"" + o.getId() + "\"}"); } catch (Exception ignored) {}
            }
        });

        buffetOrderRepo.findAllByStatus(OrderStatus.NEW).forEach(o -> {
            if (o.getAutoCancelAt() != null && !now.isBefore(o.getAutoCancelAt())
                    && o.getPaymentStatus() != PaymentStatus.SUCCEEDED) {
                o.setStatus(OrderStatus.CANCELLED);
                buffetOrderRepo.save(o);
                if (escalationEmail != null && !escalationEmail.isBlank()) {
                    try {
                        mailService.sendSimple(
                                escalationEmail,
                                "Buffet auto-cancelled (no action in 60s)",
                                "Buffet %s auto-cancelled.\nCustomer: %s %s\nPhone: %s"
                                        .formatted(o.getId(),
                                                o.getCustomerInfo().getFirstName(), o.getCustomerInfo().getLastName(),
                                                o.getCustomerInfo().getPhone()),
                                null
                        );
                    } catch (Exception ignored) {}
                }
                try { webPush.broadcast("admin", "{\"title\":\"Buffet auto-cancelled\",\"body\":\"" + o.getId() + "\"}"); } catch (Exception ignored) {}
            }
        });

        reservationRepo.findAllByStatus(ReservationStatus.REQUESTED).forEach(r -> {
            if (r.getAutoCancelAt() != null && !now.isBefore(r.getAutoCancelAt())) {
                r.setStatus(ReservationStatus.CANCELLED);
                reservationRepo.save(r);
                if (escalationEmail != null && !escalationEmail.isBlank()) {
                    try {
                        mailService.sendSimple(
                                escalationEmail,
                                "Reservation auto-cancelled (no action in 60s)",
                                "Reservation %s auto-cancelled.\nCustomer: %s %s\nPhone: %s\nTime: %s"
                                        .formatted(r.getId(),
                                                r.getCustomerInfo().getFirstName(), r.getCustomerInfo().getLastName(),
                                                r.getCustomerInfo().getPhone(),
                                                r.getReservationDateTime()),
                                null
                        );
                    } catch (Exception ignored) {}
                }
                try { webPush.broadcast("admin", "{\"title\":\"Reservation auto-cancelled\",\"body\":\"" + r.getId() + "\"}"); } catch (Exception ignored) {}
            }
        });
    }
}
