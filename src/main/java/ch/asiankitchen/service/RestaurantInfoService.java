package ch.asiankitchen.service;

import ch.asiankitchen.dto.RestaurantInfoReadDTO;
import ch.asiankitchen.dto.RestaurantInfoWriteDTO;
import ch.asiankitchen.exception.ResourceNotFoundException;
import ch.asiankitchen.repository.RestaurantInfoRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantInfoService {
    private final RestaurantInfoRepository repo;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("H:mm");

    /** Map day key string to ISO-8601 1..7 (Mon..Sun). Returns 0 if unknown. */
    private static int toDayNumber(String raw) {
        if (raw == null) return 0;
        String k = raw.trim().toLowerCase(Locale.ROOT);
        return switch (k) {
            // numeric
            case "1", "01" -> 1;
            case "2", "02" -> 2;
            case "3", "03" -> 3;
            case "4", "04" -> 4;
            case "5", "05" -> 5;
            case "6", "06" -> 6;
            case "7", "07" -> 7;
            // english names
            case "mon", "monday" -> 1;
            case "tue", "tues", "tuesday" -> 2;
            case "wed", "wednesday" -> 3;
            case "thu", "thur", "thurs", "thursday" -> 4;
            case "fri", "friday" -> 5;
            case "sat", "saturday" -> 6;
            case "sun", "sunday" -> 7;
            default -> 0;
        };
    }

    /**
     * Validates and normalizes an openingHours JSON string into canonical storage format:
     * Map&lt;Integer, List&lt;Window&gt;&gt; with keys 1..7 and times "H:mm".
     * - Accepts either 1..7 or Mon..Sun keys on input.
     * - Validates times by parsing with H:mm (e.g. "11:00").
     * - Sorts windows by open time. Allows close &lt; open (overnight).
     * Returns null if input is null/blank (treated as closed everyday).
     */
    private String normalizeOpeningHoursOrThrow(String json) {
        if (json == null || json.isBlank()) return null;

        try {
            // Be generous on input: read map of day -> array of {open, close} as strings
            Map<String, List<Map<String, String>>> raw =
                    mapper.readValue(json, new TypeReference<>() {});

            Map<Integer, List<HoursService.Window>> normalized = new HashMap<>();

            for (Map.Entry<String, List<Map<String, String>>> e : raw.entrySet()) {
                int day = toDayNumber(e.getKey());
                if (day == 0) continue; // ignore unknown keys instead of failing completely

                List<HoursService.Window> windows = new ArrayList<>();
                for (Map<String, String> w : e.getValue()) {
                    String open = Optional.ofNullable(w.get("open")).orElse("").trim();
                    String close = Optional.ofNullable(w.get("close")).orElse("").trim();
                    // Validate format strictly
                    LocalTime.parse(open, TIME_FMT);
                    LocalTime.parse(close, TIME_FMT);
                    HoursService.Window win = new HoursService.Window();
                    win.open = open;
                    win.close = close;
                    windows.add(win);
                }
                // Sort by open time so storage is stable
                windows.sort(Comparator.comparing(x -> LocalTime.parse(x.open, TIME_FMT)));
                normalized.put(day, windows);
            }

            // Fill missing days with empty lists so consumers never NPE/assume null
            for (int i = 1; i <= 7; i++) {
                normalized.putIfAbsent(i, List.of());
            }

            // Store in canonical numeric-day JSON
            // Use LinkedHashMap to preserve day order 1..7 in JSON
            Map<Integer, List<HoursService.Window>> ordered = new LinkedHashMap<>();
            for (int i = 1; i <= 7; i++) ordered.put(i, normalized.get(i));

            return mapper.writeValueAsString(ordered);
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Invalid openingHours. Expect JSON with days 1..7 or Mon..Sun and times in H:mm, e.g. " +
                            "{\"1\":[{\"open\":\"11:00\",\"close\":\"14:00\"},{\"open\":\"17:00\",\"close\":\"22:00\"}]}"
            );
        }
    }

    @Transactional
    public RestaurantInfoReadDTO create(RestaurantInfoWriteDTO dto) {
        String normalized = normalizeOpeningHoursOrThrow(dto.getOpeningHours());
        var entity = dto.toEntity();
        entity.setOpeningHours(normalized);
        var saved = repo.save(entity);
        return RestaurantInfoReadDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public RestaurantInfoReadDTO getById(UUID id) {
        return repo.findById(id)
                .map(RestaurantInfoReadDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("RestaurantInfo", id));
    }

    @Transactional(readOnly = true)
    public List<RestaurantInfoReadDTO> listAll() {
        return repo.findAll().stream()
                .map(RestaurantInfoReadDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public RestaurantInfoReadDTO update(UUID id, RestaurantInfoWriteDTO dto) {
        var existing = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RestaurantInfo", id));

        String normalized = normalizeOpeningHoursOrThrow(dto.getOpeningHours());

        existing.setName(dto.getName());
        existing.setAboutText(dto.getAboutText());
        existing.setPhone(dto.getPhone());
        existing.setEmail(dto.getEmail());
        existing.setInstagramUrl(dto.getInstagramUrl());
        existing.setGoogleMapsUrl(dto.getGoogleMapsUrl());
        existing.setOpeningHours(normalized); // <-- always canonical
        existing.setAddress(dto.getAddress().toEntity());

        return RestaurantInfoReadDTO.fromEntity(repo.save(existing));
    }

    @Transactional
    public void delete(UUID id) {
        repo.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<RestaurantInfoReadDTO> getCurrent() {
        return repo.findAll().stream()
                .findFirst()
                .map(RestaurantInfoReadDTO::fromEntity);
    }
}
