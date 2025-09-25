package ch.asiankitchen.dto;

public record AdminAlertsDTO(
        long reservationsRequested,
        long ordersNew,
        long buffetOrdersNew
) {}
