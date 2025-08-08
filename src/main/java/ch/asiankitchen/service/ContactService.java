package ch.asiankitchen.service;

import ch.asiankitchen.dto.ContactMessageRequestDTO;
import ch.asiankitchen.model.ContactMessage;
import ch.asiankitchen.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactMessageRepository repo;
    private final EmailService emailService;

    @Transactional
    public void handle(ContactMessageRequestDTO dto) {
        ContactMessage saved = repo.save(ContactMessage.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .message(dto.getMessage())
                .build());

        // Send emails (sync is simplest; for prod you may offload to a queue)
        emailService.sendToOwner(saved);
        emailService.sendAckToCustomer(saved);
    }
}
