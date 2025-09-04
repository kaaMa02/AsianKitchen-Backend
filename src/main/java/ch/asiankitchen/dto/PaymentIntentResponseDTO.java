package ch.asiankitchen.dto;

public class PaymentIntentResponseDTO {
    private String clientSecret;
    private Amounts amounts;

    public PaymentIntentResponseDTO() {}
    public PaymentIntentResponseDTO(String clientSecret, Amounts amounts) {
        this.clientSecret = clientSecret;
        this.amounts = amounts;
    }

    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

    public Amounts getAmounts() { return amounts; }
    public void setAmounts(Amounts amounts) { this.amounts = amounts; }

    public static class Amounts {
        private long total;     // rappen
        private long tax;       // rappen
        private long net;       // rappen
        private Double vatRatePct; // e.g. 2.6

        public Amounts() {}
        public Amounts(long total, long tax, long net, Double vatRatePct) {
            this.total = total; this.tax = tax; this.net = net; this.vatRatePct = vatRatePct;
        }

        public long getTotal() { return total; }
        public long getTax() { return tax; }
        public long getNet() { return net; }
        public Double getVatRatePct() { return vatRatePct; }

        public void setTotal(long total) { this.total = total; }
        public void setTax(long tax) { this.tax = tax; }
        public void setNet(long net) { this.net = net; }
        public void setVatRatePct(Double vatRatePct) { this.vatRatePct = vatRatePct; }
    }
}
