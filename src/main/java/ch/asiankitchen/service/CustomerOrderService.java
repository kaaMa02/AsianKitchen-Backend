package ch.asiankitchen.service;

import ch.asiankitchen.dto.*;
import ch.asiankitchen.exception.ResourceNotFoundException;
import ch.asiankitchen.model.*;
import ch.asiankitchen.repository.CustomerOrderRepository;
import ch.asiankitchen.repository.MenuItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.*;

@Service
public class CustomerOrderService {
    private final CustomerOrderRepository repo;
    private final MenuItemRepository menuItemRepo;
    private final EmailService email;

    public CustomerOrderService(CustomerOrderRepository repo, MenuItemRepository menuItemRepo, EmailService email) {
        this.repo = repo;
        this.menuItemRepo = menuItemRepo;
        this.email = email;
    }

    @Transactional
    public CustomerOrderReadDTO create(CustomerOrderWriteDTO dto) {
        var order = dto.toEntity();
        order.setStatus(OrderStatus.NEW); // paymentStatus defaults in @PrePersist

        // Attach real MenuItem entities (and sanity checks)
        order.getOrderItems().forEach(oi -> {
            var id = oi.getMenuItem().getId(); // comes from OrderItemWriteDTO
            MenuItem mi = menuItemRepo.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("MenuItem", id));
            if (!mi.isAvailable()) {
                throw new IllegalArgumentException("Menu item not available: " + mi.getId());
            }
            oi.setMenuItem(mi);
            oi.setCustomerOrder(order); // ensure backref
        });

        // Compute total from DB prices (server-truth)
        order.setTotalPrice(computeTotal(order));

        if (order.getPaymentMethod() != PaymentMethod.CARD) {
            order.setPaymentStatus(PaymentStatus.NOT_REQUIRED);
        }

        var saved = repo.save(order);
        return CustomerOrderReadDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public CustomerOrderReadDTO getById(UUID id) {
        return repo.findById(id)
                .map(CustomerOrderReadDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerOrder", id));
    }

    @Transactional(readOnly = true)
    public List<CustomerOrderReadDTO> listByUser(UUID userId) {
        return repo.findByUserId(userId).stream()
                .map(CustomerOrderReadDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public CustomerOrderReadDTO updateStatus(UUID id, OrderStatus status) {
        var order = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerOrder", id));
        order.setStatus(status);
        return CustomerOrderReadDTO.fromEntity(repo.save(order));
    }

    @Transactional(readOnly = true)
    public CustomerOrderReadDTO track(UUID id, String email) {
        return repo.findById(id)
                .filter(o -> o.getCustomerInfo().getEmail().equalsIgnoreCase(email))
                .map(CustomerOrderReadDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerOrder", id));
    }

    @Transactional(readOnly = true)
    public List<CustomerOrderReadDTO> listAll() {
        return repo.findAll().stream()
                .map(CustomerOrderReadDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CustomerOrderReadDTO> listAllVisibleForAdmin() {
        var statuses = List.of(PaymentStatus.SUCCEEDED, PaymentStatus.NOT_REQUIRED);
        return repo.findAdminVisibleWithItems(statuses)
                .stream().map(CustomerOrderReadDTO::fromEntity).toList();
    }

    /** Call this from your Stripe webhook when the payment becomes SUCCEEDED */
    public void sendCustomerConfirmationWithTrackLink(CustomerOrder order) {
        final String to = order.getCustomerInfo().getEmail();
        if (to == null || to.isBlank()) return;

        // Example public tracking link for menu orders
        String trackUrl = "https://asian-kitchen.online/track?orderId=%s&email=%s"
                .formatted(order.getId(), UriUtils.encode(to, StandardCharsets.UTF_8));

        String subject = "Your order at Asian Kitchen";
        String body = """
                Hi %s,

                Thanks for your order! We have received your payment.

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

    private BigDecimal computeTotal(CustomerOrder order) {
        var total = order.getOrderItems().stream()
                .map(oi -> oi.getMenuItem().getPrice().multiply(BigDecimal.valueOf(oi.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, java.math.RoundingMode.HALF_UP);

        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Order total must be greater than zero.");
        }
        return total;
    }
}
