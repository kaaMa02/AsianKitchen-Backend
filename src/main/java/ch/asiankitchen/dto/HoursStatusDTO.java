package ch.asiankitchen.dto;

import lombok.*;

import java.time.ZonedDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HoursStatusDTO {

    public enum Reason {
        OPEN,                // currently open
        BEFORE_OPEN,         // today - before first window
        BETWEEN_WINDOWS,     // today - between windows
        AFTER_CLOSE,         // today - after last window
        CLOSED_TODAY,        // no windows today
        CUTOFF_DELIVERY      // within cutoff minutes to close (delivery only)
    }

    private boolean openNow;
    private Reason reason;

    /** start of the active/next window in restaurant tz (nullable) */
    private ZonedDateTime windowOpensAt;

    /** end of the active/next window in restaurant tz (nullable) */
    private ZonedDateTime windowClosesAt;

    /** helpful text you can show directly if you want */
    private String message;
}
