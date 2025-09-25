package ch.asiankitchen.service;

import ch.asiankitchen.model.ContactMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@asian-kitchen.online}")
    private String from;

    @Value("${app.mail.to.contact:}")
    private String ownerTo;

    /** Generic helper with logging */
    @Async
    public void sendSimple(String to, String subject, String body, @Nullable String replyTo) {
        try {
            SimpleMailMessage m = new SimpleMailMessage();
            m.setFrom(from);
            m.setTo(to);
            m.setSubject(subject);
            m.setText(body);
            if (replyTo != null && !replyTo.isBlank()) {
                // SimpleMailMessage supports reply-to in Spring (mapped header)
                m.setReplyTo(replyTo);
            }
            log.info("MAIL: sending to='{}' subject='{}'", to, subject);
            mailSender.send(m);
            log.info("MAIL: sent to='{}' subject='{}'", to, subject);
        } catch (MailException e) {
            log.error("MAIL: FAILED to='{}' subject='{}' : {}", to, subject, e.getMessage(), e);
        }
    }

    public void sendToOwner(ContactMessage msg) {
        if (ownerTo == null || ownerTo.isBlank()) {
            log.warn("MAIL: ownerTo not configured; skipping owner notification.");
            return;
        }
        sendSimple(
                ownerTo,
                "New contact message from " + nullSafe(msg.getName()),
                """
                Name: %s
                Email: %s
          
                Message:
                %s
                """.formatted(nullSafe(msg.getName()), nullSafe(msg.getEmail()), nullSafe(msg.getMessage())),
                msg.getEmail() // reply-to the customer
        );
    }

    public void sendAckToCustomer(ContactMessage msg) {
        sendSimple(
                nullSafe(msg.getEmail()),
                "Thanks for contacting Asian Kitchen",
                """
                Hi %s,
          
                Thanks for reaching out! We received your message and will get back to you soon.
          
                — Asian Kitchen
                """.formatted(nullSafe(msg.getName())),
                null
        );
    }

    private static String nullSafe(String s) { return s == null ? "" : s; }
}
