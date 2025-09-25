package ch.asiankitchen.service;

import ch.asiankitchen.dto.AdminAlertsDTO;
import ch.asiankitchen.model.OrderStatus;
import ch.asiankitchen.model.ReservationStatus;
import ch.asiankitchen.repository.BuffetOrderRepository;
import ch.asiankitchen.repository.CustomerOrderRepository;
import ch.asiankitchen.repository.ReservationRepository;
import org.springframework.stereotype.Service;

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

    public AdminAlertsDTO summary() {
        long r = reservations.countByStatus(ReservationStatus.REQUESTED);
        long o = orders.countByStatus(OrderStatus.NEW);
        long b = buffet.countByStatus(OrderStatus.NEW);
        return new AdminAlertsDTO(r, o, b);
    }
}
