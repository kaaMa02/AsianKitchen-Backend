package ch.asiankitchen.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Accepts:
 *  - 2025-11-13T11:00
 *  - 2025-11-13T11:00:00
 *  - 2025-11-13T11:00:00Z
 *  - 2025-11-13T11:00:00+01:00
 *
 * Returns LocalDateTime in the application's local zone (default Europe/Zurich).
 * NOTE: Controllers/services should treat this as "local wall time".
 */
public class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter[] LOCAL_FORMATS = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
    };

    private final ZoneId appZone;

    public FlexibleLocalDateTimeDeserializer() {
        this(ZoneId.of(System.getProperty("app.timezone", "Europe/Zurich")));
    }

    public FlexibleLocalDateTimeDeserializer(ZoneId appZone) {
        this.appZone = appZone;
    }

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String s = p.getValueAsString();
        if (s == null || s.isBlank()) return null;

        // Try strict local formats first (no offset)
        for (DateTimeFormatter f : LOCAL_FORMATS) {
            try { return LocalDateTime.parse(s, f); } catch (DateTimeParseException ignored) {}
        }

        // Try ISO_ZONED or INSTANT with offset/Z
        try {
            // If it has an offset/Z, parse to instant, then to LocalDateTime in app zone
            Instant inst = Instant.parse(s);
            return LocalDateTime.ofInstant(inst, appZone);
        } catch (DateTimeParseException ignored) {}

        try {
            OffsetDateTime odt = OffsetDateTime.parse(s);
            return odt.atZoneSameInstant(appZone).toLocalDateTime();
        } catch (DateTimeParseException ignored) {}

        try {
            ZonedDateTime zdt = ZonedDateTime.parse(s);
            return zdt.withZoneSameInstant(appZone).toLocalDateTime();
        } catch (DateTimeParseException ignored) {}

        // Last resort: let Java parse and coerce to local zone
        try {
            Instant inst = Instant.parse(s);
            return LocalDateTime.ofInstant(inst, appZone);
        } catch (Exception ignored) {}

        throw ctxt.weirdStringException(s, LocalDateTime.class, "Unparseable datetime");
    }
}
