package ch.asiankitchen.service;

import ch.asiankitchen.dto.DiscountConfigReadDTO;
import ch.asiankitchen.dto.DiscountConfigWriteDTO;
import ch.asiankitchen.model.DiscountConfig;
import ch.asiankitchen.repository.DiscountConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DiscountService {

    private final DiscountConfigRepository repo;

    private static final UUID SINGLETON_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");

    @Transactional(readOnly = true)
    public DiscountConfigReadDTO getCurrent() {
        DiscountConfig c = repo.findById(SINGLETON_ID).orElseGet(() -> {
            DiscountConfig d = DiscountConfig.builder()
                    .id(SINGLETON_ID)
                    .enabled(false)
                    .percentMenu(BigDecimal.ZERO)
                    .percentBuffet(BigDecimal.ZERO)
                    .build();
            return repo.save(d);
        });
        return toReadDTO(c);
    }

    @Transactional
    public DiscountConfigReadDTO update(DiscountConfigWriteDTO dto) {
        DiscountConfig c = repo.findById(SINGLETON_ID).orElseThrow();
        c.setEnabled(dto.isEnabled());
        c.setPercentMenu(safe(dto.getPercentMenu()));
        c.setPercentBuffet(safe(dto.getPercentBuffet()));
        c.setStartsAt(dto.getStartsAt());
        c.setEndsAt(dto.getEndsAt());
        return toReadDTO(repo.save(c));
    }

    @Transactional(readOnly = true)
    public ActiveDiscount resolveActive() {
        DiscountConfigReadDTO c = getCurrent();
        if (!c.isEnabled()) return ActiveDiscount.none();

        OffsetDateTime now = OffsetDateTime.now();
        if (c.getStartsAt() != null && now.isBefore(c.getStartsAt())) return ActiveDiscount.none();
        if (c.getEndsAt() != null && now.isAfter(c.getEndsAt())) return ActiveDiscount.none();

        return new ActiveDiscount(
                c.getPercentMenu(),
                c.getPercentBuffet()
        );
    }

    public record ActiveDiscount(BigDecimal percentMenu, BigDecimal percentBuffet) {
        public static ActiveDiscount none() { return new ActiveDiscount(BigDecimal.ZERO, BigDecimal.ZERO); }
        public boolean hasMenu() { return percentMenu != null && percentMenu.compareTo(BigDecimal.ZERO) > 0; }
        public boolean hasBuffet() { return percentBuffet != null && percentBuffet.compareTo(BigDecimal.ZERO) > 0; }
    }

    private static DiscountConfigReadDTO toReadDTO(DiscountConfig c) {
        return DiscountConfigReadDTO.builder()
                .id(c.getId())
                .enabled(c.isEnabled())
                .percentMenu(safe(c.getPercentMenu()))
                .percentBuffet(safe(c.getPercentBuffet()))
                .startsAt(c.getStartsAt())
                .endsAt(c.getEndsAt())
                .build();
    }

    private static java.math.BigDecimal safe(java.math.BigDecimal v) {
        return v == null ? java.math.BigDecimal.ZERO : v;
    }
}
