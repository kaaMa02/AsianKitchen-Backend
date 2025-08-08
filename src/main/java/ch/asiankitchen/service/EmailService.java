package ch.asiankitchen.service;

import ch.asiankitchen.model.ContactMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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

    public void sendToOwner(ContactMessage msg) {
        if (ownerTo == null || ownerTo.isBlank()) return;
        var m = new SimpleMailMessage();
        m.setFrom(from);
        m.setTo(ownerTo);
        m.setSubject("New contact message from " + msg.getName());
        m.setText("""
                Name: %s
                Email: %s

                Message:
                %s
                """.formatted(msg.getName(), msg.getEmail(), msg.getMessage()));
        mailSender.send(m);
    }

    public void sendAckToCustomer(ContactMessage msg) {
        var m = new SimpleMailMessage();
        m.setFrom(from);
        m.setTo(msg.getEmail());
        m.setSubject("Thanks for contacting Asian Kitchen");
        m.setText("""
                Hi %s,

                Thanks for reaching out! We received your message and will get back to you soon.

                — Asian Kitchen
                """.formatted(msg.getName()));
        mailSender.send(m);
    }
}