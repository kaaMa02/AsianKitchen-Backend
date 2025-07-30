package ch.asiankitchen.controller.admin;

import ch.asiankitchen.dto.BuffetItemReadDTO;
import ch.asiankitchen.dto.BuffetItemWriteDTO;
import ch.asiankitchen.service.BuffetItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/buffet-items")
@PreAuthorize("hasRole('ADMIN')")
public class BuffetItemAdminController {
    private final BuffetItemService service;

    public BuffetItemAdminController(BuffetItemService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<BuffetItemReadDTO> create(
            @Valid @RequestBody BuffetItemWriteDTO dto) {
        BuffetItemReadDTO created = service.create(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public List<BuffetItemReadDTO> list() {
        return service.listAll();
    }

    @PutMapping("/{id}")
    public BuffetItemReadDTO update(
            @PathVariable UUID id,
            @Valid @RequestBody BuffetItemWriteDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}