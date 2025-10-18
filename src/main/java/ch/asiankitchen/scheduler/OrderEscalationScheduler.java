package ch.asiankitchen.scheduler;

import ch.asiankitchen.model.*;
import ch.asiankitchen.repository.*;
import ch.asiankitchen.service.EmailService;
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

    @Value("${app.mail.to.escalation}")
    private String escalationEmail;

    @Value("${app.order.escalate-minutes:5}")
    private int escalateMinutes;

    // runs every minute
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void tick() {
        var now = LocalDateTime.now();

        // Escalate if unseen for > escalateMinutes
        customerOrderRepo.findAllByStatus(OrderStatus.NEW).forEach(o -> {
            if (o.getSeenAt() == null && o.getEscalatedAt() == null
                    && o.getCreatedAt().isBefore(now.minusMinutes(escalateMinutes))) {
                o.setEscalatedAt(now);
                customerOrderRepo.save(o);
                try {
                    mailService.sendSimple(
                            escalationEmail,
                            "New order unseen",
                            "Order %s (%s) is waiting.\nCustomer: %s %s"
                                    .formatted(o.getId(), o.getOrderType(),
                                            o.getCustomerInfo().getFirstName(), o.getCustomerInfo().getLastName()),
                            null
                    );
                } catch (Exception ignored) {}
            }
        });

        buffetOrderRepo.findAllByStatus(OrderStatus.NEW).forEach(o -> {
            if (o.getSeenAt() == null && o.getEscalatedAt() == null
                    && o.getCreatedAt().isBefore(now.minusMinutes(escalateMinutes))) {
                o.setEscalatedAt(now);
                buffetOrderRepo.save(o);
                try {
                    mailService.sendSimple(
                            escalationEmail,
                            "New buffet order unseen",
                            "Buffet order %s is waiting.".formatted(o.getId()),
                            null
                    );
                } catch (Exception ignored) {}
            }
        });

        reservationRepo.findAllByStatus(ReservationStatus.REQUESTED).forEach(r -> {
            if (r.getSeenAt() == null && r.getEscalatedAt() == null
                    && r.getCreatedAt().isBefore(now.minusMinutes(escalateMinutes))) {
                r.setEscalatedAt(now);
                reservationRepo.save(r);
                try {
                    mailService.sendSimple(
                            escalationEmail,
                            "Reservation unseen",
                            "Reservation %s is waiting.".formatted(r.getId()),
                            null
                    );
                } catch (Exception ignored) {}
            }
        });

        // Auto-cancel NEW unpaid orders past autoCancelAt
        customerOrderRepo.findAllByStatus(OrderStatus.NEW).forEach(o -> {
            if (o.getAutoCancelAt() != null && now.isAfter(o.getAutoCancelAt())
                    && o.getPaymentStatus() != PaymentStatus.SUCCEEDED) {
                o.setStatus(OrderStatus.CANCELLED);
                customerOrderRepo.save(o);
                try { /* email customer cancellation */ } catch (Exception ignored) {}
            }
        });

        buffetOrderRepo.findAllByStatus(OrderStatus.NEW).forEach(o -> {
            if (o.getAutoCancelAt() != null && now.isAfter(o.getAutoCancelAt())
                    && o.getPaymentStatus() != PaymentStatus.SUCCEEDED) {
                o.setStatus(OrderStatus.CANCELLED);
                buffetOrderRepo.save(o);
                try { /* email */ } catch (Exception ignored) {}
            }
        });
    }
}
