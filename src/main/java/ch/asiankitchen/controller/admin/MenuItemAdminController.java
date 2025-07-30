package ch.asiankitchen.controller.admin;

import ch.asiankitchen.dto.MenuItemDTO;
import ch.asiankitchen.dto.MenuItemWriteDTO;
import ch.asiankitchen.service.MenuItemService;
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
@RequestMapping("/api/admin/menu-items")
@PreAuthorize("hasRole('ADMIN')")
public class MenuItemAdminController {
    private final MenuItemService service;

    public MenuItemAdminController(MenuItemService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<MenuItemDTO> create(
            @Valid @RequestBody MenuItemWriteDTO dto) {
        MenuItemDTO created = service.create(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public List<MenuItemDTO> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public MenuItemDTO getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public MenuItemDTO update(
            @PathVariable UUID id,
            @Valid @RequestBody MenuItemWriteDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}