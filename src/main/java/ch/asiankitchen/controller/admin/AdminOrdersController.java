// backend/src/main/java/ch/asiankitchen/controller/admin/AdminOrdersController.java
package ch.asiankitchen.controller.admin;

import ch.asiankitchen.dto.*;
import ch.asiankitchen.model.*;
import ch.asiankitchen.repository.*;
import ch.asiankitchen.service.OrderWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminOrdersController {

    private final CustomerOrderRepository customerOrderRepo;
    private final BuffetOrderRepository buffetOrderRepo;
    private final ReservationRepository reservationRepo;
    private final OrderWorkflowService workflow;

    // 1) New order cards feed
    @GetMapping("/orders/new")
    public List<NewOrderCardDTO> newOrders() {
        var out = new ArrayList<NewOrderCardDTO>();

        customerOrderRepo.findNewWithItems().forEach(o -> out.add(NewOrderCardDTO.builder()
                .id(o.getId())
                .kind("menu")
                .customerInfo(CustomerInfoDTO.fromEntity(o.getCustomerInfo()))
                .orderType(o.getOrderType())
                .status(o.getStatus())
                .createdAt(o.getCreatedAt())
                .specialInstructions(o.getSpecialInstructions())
                .timing(OrderTimingReadDTO.builder()
                        .asap(o.isAsap()).requestedAt(o.getRequestedAt())
                        .minPrepMinutes(o.getMinPrepMinutes())
                        .adminExtraMinutes(o.getAdminExtraMinutes())
                        .committedReadyAt(o.getCommittedReadyAt())
                        .autoCancelAt(o.getAutoCancelAt())
                        .seenAt(o.getSeenAt())
                        .escalatedAt(o.getEscalatedAt())
                        .build())
                .paymentStatus(o.getPaymentStatus())
                .paymentMethod(o.getPaymentMethod())
                .paymentIntentId(o.getPaymentIntentId())
                .totalPrice(o.getTotalPrice())
                .menuItems(o.getOrderItems().stream().map(OrderItemReadDTO::fromEntity).toList())
                .build()));

        buffetOrderRepo.findNewWithItems().forEach(o -> out.add(NewOrderCardDTO.builder()
                .id(o.getId())
                .kind("buffet")
                .customerInfo(CustomerInfoDTO.fromEntity(o.getCustomerInfo()))
                .orderType(o.getOrderType())
                .status(o.getStatus())
                .createdAt(o.getCreatedAt())
                .specialInstructions(o.getSpecialInstructions())
                .timing(OrderTimingReadDTO.builder()
                        .asap(o.isAsap()).requestedAt(o.getRequestedAt())
                        .minPrepMinutes(o.getMinPrepMinutes())
                        .adminExtraMinutes(o.getAdminExtraMinutes())
                        .committedReadyAt(o.getCommittedReadyAt())
                        .autoCancelAt(o.getAutoCancelAt())
                        .seenAt(o.getSeenAt())
                        .escalatedAt(o.getEscalatedAt())
                        .build())
                .paymentStatus(o.getPaymentStatus())
                .paymentMethod(o.getPaymentMethod())
                .paymentIntentId(o.getPaymentIntentId())
                .totalPrice(o.getTotalPrice())
                .buffetItems(o.getBuffetOrderItems().stream().map(BuffetOrderItemReadDTO::fromEntity).toList())
                .build()));

        reservationRepo.findAllByStatus(ReservationStatus.REQUESTED).forEach(r -> out.add(NewOrderCardDTO.builder()
                .id(r.getId())
                .kind("reservation")
                .customerInfo(CustomerInfoDTO.fromEntity(r.getCustomerInfo()))
                .status(OrderStatus.NEW) // map for UI
                .createdAt(r.getCreatedAt())
                .specialInstructions(r.getSpecialRequests())
                .timing(OrderTimingReadDTO.builder()
                        .asap(false).requestedAt(r.getReservationDateTime())
                        .committedReadyAt(r.getReservationDateTime())
                        .autoCancelAt(r.getAutoCancelAt())
                        .seenAt(r.getSeenAt())
                        .escalatedAt(r.getEscalatedAt())
                        .build())
                .build()));

        out.sort(Comparator.comparing(NewOrderCardDTO::getCreatedAt).reversed());
        return out;
    }

    // 2) Mark seen
    @PostMapping("/{kind}-orders/{id}/seen")
    public ResponseEntity<Void> seen(@PathVariable String kind, @PathVariable UUID id) {
        workflow.markSeen(kind, id);
        return ResponseEntity.ok().build();
    }

    // 3) Confirm
    public record ConfirmBody(Boolean print, Integer extraMinutes) {}

    @PostMapping("/{kind}-orders/{id}/confirm")
    public ResponseEntity<Void> confirm(
            @PathVariable String kind,
            @PathVariable UUID id,
            @RequestBody ConfirmBody body) {

        // Single call handles both confirm and optional inline +minutes
        workflow.confirmOrder(kind, id, body.extraMinutes(), Boolean.TRUE.equals(body.print()));

        return ResponseEntity.ok().build();
    }

    // 4) Cancel
    public record CancelBody(String reason, boolean refundIfPaid) {}
    @PostMapping("/{kind}-orders/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable String kind, @PathVariable UUID id, @RequestBody CancelBody body) {
        workflow.cancelOrder(kind, id, body.reason, body.refundIfPaid);
        return ResponseEntity.ok().build();
    }

    // 5) Patch timing (legacy path; UI now sends inline via confirm)
    @PatchMapping("/{kind}-orders/{id}/timing")
    public ResponseEntity<Void> timing(@PathVariable String kind, @PathVariable UUID id, @RequestBody OrderTimingPatchDTO body) {
        if (body.getAdminExtraMinutes() != null) {
            workflow.patchExtraMinutes(kind, id, Math.max(0, body.getAdminExtraMinutes()));
        }
        return ResponseEntity.ok().build();
    }
}
