package ch.asiankitchen.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Accepts:
 *  - "YYYY-MM-DDTHH:mm"
 *  - "YYYY-MM-DDTHH:mm:ss"
 *  - either of the above with fractional seconds
 *  - ISO instants with Z (e.g., "2025-11-11T16:05:00Z")
 *  - ISO strings with offsets (e.g., "2025-11-11T16:05:00+01:00")
 *
 * Output: LocalDateTime on the application's wall clock.
 * For inputs with Z/offset, we project the instant to Europe/Zurich wall time.
 */
public class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter WALL_MIN = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final DateTimeFormatter WALL_SEC = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final ZoneId APP_ZONE = ZoneId.of("Europe/Zurich"); // keep in sync with app.timezone

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        String raw = p.getValueAsString();
        if (raw == null) return null;
        raw = raw.trim();
        if (raw.isEmpty()) return null;

        // Case 1: Z or explicit offset present → parse as instant and convert to app wall-time
        if (raw.endsWith("Z") || raw.matches(".*[+-]\\d{2}:?\\d{2}$")) {
            try {
                // First try strict Instant
                try {
                    Instant inst = Instant.parse(raw);
                    return LocalDateTime.ofInstant(inst, APP_ZONE);
                } catch (Exception ignore) {
                    // Then try OffsetDateTime
                    OffsetDateTime odt = OffsetDateTime.parse(raw);
                    return LocalDateTime.ofInstant(odt.toInstant(), APP_ZONE);
                }
            } catch (Exception e) {
                throw JsonMappingException.from(p, "Invalid scheduledAt (offset/Z): " + raw, e);
            }
        }

        // Case 2: Wall-clock (no zone). Strip fractional seconds if present and parse.
        String s = raw.replaceFirst("(\\.\\d+)$", ""); // drop .SSS... if present
        try {
            if (s.length() == 16) return LocalDateTime.parse(s, WALL_MIN);         // yyyy-MM-ddTHH:mm
            if (s.length() >= 19) return LocalDateTime.parse(s.substring(0, 19), WALL_SEC); // yyyy-MM-ddTHH:mm:ss
            // last resort
            return LocalDateTime.parse(s);
        } catch (Exception e) {
            throw JsonMappingException.from(p, "Invalid scheduledAt format: " + raw, e);
        }
    }
}
