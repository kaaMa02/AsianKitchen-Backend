package ch.asiankitchen.service;

import ch.asiankitchen.model.CustomerInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DeliveryZoneService {

    private final Set<String> allowedPlz;
    private final String rejectMessage;

    public DeliveryZoneService(
            @Value("${app.delivery.allowed-plz:}") String allowedPlzCsv,
            @Value("${app.delivery.reject-message:We don’t deliver to this address.}") String rejectMessage
    ) {
        this.allowedPlz = Stream.of(allowedPlzCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        this.rejectMessage = rejectMessage;
    }

    public boolean isAllowed(CustomerInfo info) {
        if (info == null || info.getAddress() == null) return false;
        String plz = info.getAddress().getPlz();
        return plz != null && allowedPlz.contains(plz.trim());
    }

    public String getRejectMessage() {
        return rejectMessage;
    }
}