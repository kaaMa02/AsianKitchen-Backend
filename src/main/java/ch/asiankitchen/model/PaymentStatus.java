package ch.asiankitchen.model;

public enum PaymentStatus {
    REQUIRES_PAYMENT_METHOD,
    REQUIRES_CONFIRMATION,
    REQUIRES_ACTION,
    PROCESSING,
    SUCCEEDED,
    CANCELED,
    FAILED,
    NOT_REQUIRED
}