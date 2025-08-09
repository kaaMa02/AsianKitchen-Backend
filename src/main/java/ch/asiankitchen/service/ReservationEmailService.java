package ch.asiankitchen.service;

import ch.asiankitchen.model.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationEmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@asian-kitchen.online}")
    private String from;

    @Value("${app.mail.to.reservations:}")
    private String restaurantEmail;

    public void sendNewReservationToRestaurant(Reservation res) {
        if (restaurantEmail == null || restaurantEmail.isBlank()) return;

        var m = new SimpleMailMessage();
        m.setFrom(from);
        m.setTo(restaurantEmail);
        m.setSubject("New Reservation Request");
        m.setText("""
                New reservation request:

                Name: %s %s
                Email: %s
                Phone: %s

                Date/Time: %s
                People: %d
                Special Requests: %s
                """.formatted(
                res.getCustomerInfo().getFirstName(),
                res.getCustomerInfo().getLastName(),
                res.getCustomerInfo().getEmail(),
                res.getCustomerInfo().getPhone(),
                res.getReservationDateTime(),
                res.getNumberOfPeople(),
                res.getSpecialRequests() != null ? res.getSpecialRequests() : "None"
        ));
        mailSender.send(m);
    }

    public void sendConfirmationToCustomer(Reservation res) {
        var m = new SimpleMailMessage();
        m.setFrom(from);
        m.setTo(res.getCustomerInfo().getEmail());
        m.setSubject("Your Reservation is Confirmed");
        m.setText("""
                Hi %s,

                Your reservation for %d people on %s has been confirmed.

                We look forward to serving you!

                — Asian Kitchen
                """.formatted(
                res.getCustomerInfo().getFirstName(),
                res.getNumberOfPeople(),
                res.getReservationDateTime()
        ));
        mailSender.send(m);
    }

    public void sendRejectionToCustomer(Reservation res) {
        var m = new SimpleMailMessage();
        m.setFrom(from);
        m.setTo(res.getCustomerInfo().getEmail());
        m.setSubject("Your Reservation Could Not Be Confirmed");
        m.setText("""
                Hi %s,

                Unfortunately, we cannot confirm your reservation for %d people on %s.

                Please contact us if you have questions or wish to choose another time.

                — Asian Kitchen
                """.formatted(
                res.getCustomerInfo().getFirstName(),
                res.getNumberOfPeople(),
                res.getReservationDateTime()
        ));
        mailSender.send(m);
    }
}
