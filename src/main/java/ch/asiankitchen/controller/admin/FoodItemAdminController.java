package ch.asiankitchen.controller.admin;

import ch.asiankitchen.dto.FoodItemDTO;
import ch.asiankitchen.service.FoodItemService;
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
@RequestMapping("/api/admin/food-items")
@PreAuthorize("hasRole('ADMIN')")
public class FoodItemAdminController {
    private final FoodItemService service;

    public FoodItemAdminController(FoodItemService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<FoodItemDTO> create(@Valid @RequestBody FoodItemDTO dto) {
        FoodItemDTO created = service.create(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public List<FoodItemDTO> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public FoodItemDTO getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public FoodItemDTO update(
            @PathVariable UUID id,
            @Valid @RequestBody FoodItemDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}