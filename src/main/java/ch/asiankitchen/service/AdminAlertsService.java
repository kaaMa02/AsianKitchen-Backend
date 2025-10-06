package ch.asiankitchen.service;

import ch.asiankitchen.dto.AdminAlertsDTO;
import ch.asiankitchen.model.OrderStatus;
import ch.asiankitchen.model.PaymentMethod;
import ch.asiankitchen.model.PaymentStatus;
import ch.asiankitchen.model.ReservationStatus;
import ch.asiankitchen.repository.BuffetOrderRepository;
import ch.asiankitchen.repository.CustomerOrderRepository;
import ch.asiankitchen.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AdminAlertsService {

    private final ReservationRepository reservations;
    private final CustomerOrderRepository orders;
    private final BuffetOrderRepository buffet;

    public AdminAlertsService(ReservationRepository reservations,
                              CustomerOrderRepository orders,
                              BuffetOrderRepository buffet) {
        this.reservations = reservations;
        this.orders = orders;
        this.buffet = buffet;
    }

    /** Snapshot of raw counts (not deltas) */
    private AdminAlertsDTO snapshot() {
        long r = reservations.countByStatus(ReservationStatus.REQUESTED);

        long o = orders.countByStatusAndPaymentStatus(OrderStatus.NEW, PaymentStatus.SUCCEEDED)
                + orders.countByStatusAndPaymentMethod(OrderStatus.NEW, PaymentMethod.CASH);

        long b = buffet.countByStatusAndPaymentStatus(OrderStatus.NEW, PaymentStatus.SUCCEEDED)
                + buffet.countByStatusAndPaymentMethod(OrderStatus.NEW, PaymentMethod.CASH);

        return new AdminAlertsDTO(r, o, b);
    }

    /* ---------------- "Seen" state per admin user ---------------- */

    private static final class Seen {
        long reservationsRequested;
        long ordersNew;
        long buffetOrdersNew;
    }

    /** username -> last seen raw counts */
    private final Map<String, Seen> seenByUser = new ConcurrentHashMap<>();

    public AdminAlertsDTO unseenForUser(String username) {
        AdminAlertsDTO now = snapshot();
        Seen s = seenByUser.computeIfAbsent(username, k -> new Seen());

        long r = Math.max(0, now.reservationsRequested() - s.reservationsRequested);
        long o = Math.max(0, now.ordersNew() - s.ordersNew);
        long b = Math.max(0, now.buffetOrdersNew() - s.buffetOrdersNew);

        return new AdminAlertsDTO(r, o, b);
    }

    /** Mark one or more buckets as seen at their current raw counts */
    public void markSeen(String username, Set<String> kinds) {
        AdminAlertsDTO now = snapshot();
        Seen s = seenByUser.computeIfAbsent(username, k -> new Seen());

        if (kinds.contains("reservations")) s.reservationsRequested = now.reservationsRequested();
        if (kinds.contains("orders"))       s.ordersNew              = now.ordersNew();
        if (kinds.contains("buffet"))       s.buffetOrdersNew        = now.buffetOrdersNew();
    }
}
