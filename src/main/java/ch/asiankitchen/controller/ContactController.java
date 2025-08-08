package ch.asiankitchen.controller;

import ch.asiankitchen.dto.ContactMessageRequestDTO;
import ch.asiankitchen.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    private final ContactService service;

    public ContactController(ContactService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void send(@Valid @RequestBody ContactMessageRequestDTO dto) {
        service.handle(dto);
    }
}