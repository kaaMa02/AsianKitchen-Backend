// src/main/java/ch/asiankitchen/service/OrderWorkflowService.java
package ch.asiankitchen.service;

import ch.asiankitchen.model.*;
import ch.asiankitchen.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderWorkflowService {

    private final CustomerOrderRepository customerOrderRepo;
    private final BuffetOrderRepository buffetOrderRepo;
    private final EmailService mailService;      // reserved for future use if needed
    private final WebPushService webPushService;

    @Value("${app.order.min-prep-minutes:45}")
    private int defaultMinPrep;

    @Value("${app.order.alert-seconds:60}")
    private int alertSeconds;

    // ---- compute committed times on creation (and the 60s alert window) ----
    public void applyInitialTiming(CustomerOrder o) {
        if (o.getMinPrepMinutes() == null || o.getMinPrepMinutes() <= 0) o.setMinPrepMinutes(defaultMinPrep);
        if (o.getAdminExtraMinutes() == null) o.setAdminExtraMinutes(0);

        if (o.isAsap()) {
            var ready = o.getCreatedAt().plusMinutes(o.getMinPrepMinutes() + o.getAdminExtraMinutes());
            o.setCommittedReadyAt(ready);
        } else {
            o.setCommittedReadyAt(o.getRequestedAt()); // admin must not change; UI enforces
        }

        // NEW orders (ASAP or scheduled) must be confirmed/cancelled within alertSeconds
        o.setAutoCancelAt(o.getCreatedAt().plusSeconds(alertSeconds));
    }

    public void applyInitialTiming(BuffetOrder o) {
        if (o.getMinPrepMinutes() == null || o.getMinPrepMinutes() <= 0) o.setMinPrepMinutes(defaultMinPrep);
        if (o.getAdminExtraMinutes() == null) o.setAdminExtraMinutes(0);

        if (o.isAsap()) {
            var ready = o.getCreatedAt().plusMinutes(o.getMinPrepMinutes() + o.getAdminExtraMinutes());
            o.setCommittedReadyAt(ready);
        } else {
            o.setCommittedReadyAt(o.getRequestedAt());
        }

        o.setAutoCancelAt(o.getCreatedAt().plusSeconds(alertSeconds));
    }

    // ---- admin interactions ----
    @Transactional
    public void markSeen(String kind, UUID id) {
        switch (kind) {
            case "menu" -> customerOrderRepo.findById(id).ifPresent(o -> {
                o.setSeenAt(LocalDateTime.now());
                customerOrderRepo.save(o);
            });
            case "buffet" -> buffetOrderRepo.findById(id).ifPresent(o -> {
                o.setSeenAt(LocalDateTime.now());
                buffetOrderRepo.save(o);
            });
            default -> {}
        }
    }

    @Transactional
    public void patchExtraMinutes(String kind, UUID id, int extra) {
        final int add = Math.max(0, extra); // effectively final for lambdas
        if ("menu".equals(kind)) {
            customerOrderRepo.findById(id).ifPresent(o -> {
                if (o.isAsap() && o.getStatus() == OrderStatus.NEW) {
                    o.setAdminExtraMinutes(add);
                    o.setCommittedReadyAt(o.getCreatedAt().plusMinutes(o.getMinPrepMinutes() + add));
                    customerOrderRepo.save(o);
                }
            });
        } else if ("buffet".equals(kind)) {
            buffetOrderRepo.findById(id).ifPresent(o -> {
                if (o.isAsap() && o.getStatus() == OrderStatus.NEW) {
                    o.setAdminExtraMinutes(add);
                    o.setCommittedReadyAt(o.getCreatedAt().plusMinutes(o.getMinPrepMinutes() + add));
                    buffetOrderRepo.save(o);
                }
            });
        }
    }

    @Transactional
    public void confirmOrder(String kind, UUID id, boolean print) {
        if ("menu".equals(kind)) {
            customerOrderRepo.findById(id).ifPresent(o -> {
                o.setStatus(OrderStatus.CONFIRMED);
                customerOrderRepo.save(o);
                try { webPushService.broadcast("admin", "{\"title\":\"Order confirmed\",\"body\":\"" + o.getId() + "\"}"); } catch (Exception ignored){}
                try { /* send customer confirmation with tracking link */ } catch (Exception ignored){}
                if (print) { /* print ticket */ }
            });
        } else if ("buffet".equals(kind)) {
            buffetOrderRepo.findById(id).ifPresent(o -> {
                o.setStatus(OrderStatus.CONFIRMED);
                buffetOrderRepo.save(o);
                try { webPushService.broadcast("admin", "{\"title\":\"Buffet confirmed\",\"body\":\"" + o.getId() + "\"}"); } catch (Exception ignored){}
                try { /* send confirmation */ } catch (Exception ignored){}
            });
        }
    }

    @Transactional
    public void cancelOrder(String kind, UUID id, String reason, boolean refundIfPaid) {
        if ("menu".equals(kind)) {
            customerOrderRepo.findById(id).ifPresent(o -> {
                o.setStatus(OrderStatus.CANCELLED);
                customerOrderRepo.save(o);
                try { /* email cancellation */ } catch (Exception ignored){}
                // optional: refund via Stripe if paid & requested
            });
        } else if ("buffet".equals(kind)) {
            buffetOrderRepo.findById(id).ifPresent(o -> {
                o.setStatus(OrderStatus.CANCELLED);
                buffetOrderRepo.save(o);
                try { /* email */ } catch (Exception ignored){}
            });
        }
    }
}
