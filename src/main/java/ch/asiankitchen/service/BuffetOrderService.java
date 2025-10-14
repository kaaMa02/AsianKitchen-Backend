package ch.asiankitchen.service;

import ch.asiankitchen.dto.BuffetOrderReadDTO;
import ch.asiankitchen.dto.BuffetOrderWriteDTO;
import ch.asiankitchen.exception.ResourceNotFoundException;
import ch.asiankitchen.model.BuffetOrder;
import ch.asiankitchen.model.OrderStatus;
import ch.asiankitchen.model.PaymentMethod;
import ch.asiankitchen.model.PaymentStatus;
import ch.asiankitchen.repository.BuffetOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.*;

@Service
public class BuffetOrderService {
    private final BuffetOrderRepository repo;
    private final EmailService email;
    private final WebPushService webPushService;

    public BuffetOrderService(BuffetOrderRepository repo, EmailService email, WebPushService webPushService) {
        this.repo = repo;
        this.email = email;
        this.webPushService = webPushService;
    }

    @Transactional
    public BuffetOrderReadDTO create(BuffetOrderWriteDTO dto) {
        var order = dto.toEntity();
        order.setStatus(OrderStatus.NEW);
        if (order.getPaymentMethod() != PaymentMethod.CARD) {
            order.setPaymentStatus(PaymentStatus.NOT_REQUIRED);
        }
        var saved = repo.save(order);

        if (order.getPaymentMethod() != PaymentMethod.CARD) {
            try {
                webPushService.broadcast("admin",
                        """
                        {"title":"New Order (Buffet)","body":"%s order %s","url":"/admin/buffet-orders"}
                        """.formatted(order.getOrderType(), order.getId()));
            } catch (Exception ignored) {}
        }

        if (saved.getPaymentMethod() != PaymentMethod.CARD) {
            sendCustomerConfirmationWithTrackLink(saved);
        }

        return BuffetOrderReadDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public BuffetOrderReadDTO getById(UUID id) {
        return repo.findById(id)
                .map(BuffetOrderReadDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("BuffetOrder", id));
    }

    @Transactional(readOnly = true)
    public List<BuffetOrderReadDTO> listByUser(UUID userId) {
        return repo.findByUserId(userId).stream()
                .map(BuffetOrderReadDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BuffetOrderReadDTO> listAll() {
        return repo.findAll().stream()
                .map(BuffetOrderReadDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BuffetOrderReadDTO> listAllVisibleForAdmin() {
        var statuses = List.of(PaymentStatus.SUCCEEDED, PaymentStatus.NOT_REQUIRED);
        return repo.findAdminVisibleWithItems(statuses)
                .stream().map(BuffetOrderReadDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public BuffetOrderReadDTO track(UUID id, String email) {
        return repo.findById(id)
                .filter(o -> o.getCustomerInfo().getEmail().equalsIgnoreCase(email))
                .map(BuffetOrderReadDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("BuffetOrder", id));
    }

    @Transactional
    public BuffetOrderReadDTO updateStatus(UUID id, OrderStatus status) {
        var order = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BuffetOrder", id));
        order.setStatus(status);
        return BuffetOrderReadDTO.fromEntity(repo.save(order));
    }

    /** Call this from your Stripe webhook when payment becomes SUCCEEDED */
    public void sendCustomerConfirmationWithTrackLink(BuffetOrder order) {
        final String to = order.getCustomerInfo().getEmail();
        if (to == null || to.isBlank()) return;

        String trackUrl = "https://asian-kitchen.online/track-buffet?orderId=%s&email=%s"
                .formatted(order.getId(), UriUtils.encode(to, StandardCharsets.UTF_8));

        String subject = "Your buffet order at Asian Kitchen";
        String body = """
                Hi %s,

                Thanks for your buffet order! We have received your payment.

                You can track your order here:
                %s

                Order ID: %s
                Total: CHF %s

                — Asian Kitchen
                """.formatted(
                nullSafe(order.getCustomerInfo().getFirstName()),
                trackUrl,
                order.getId(),
                order.getTotalPrice()
        );

        email.sendSimple(to, subject, body, null);
    }

    private static String nullSafe(String s) { return s == null ? "" : s; }

}