package ch.asiankitchen.controller.admin;

import ch.asiankitchen.dto.RestaurantInfoReadDTO;
import ch.asiankitchen.dto.RestaurantInfoWriteDTO;
import ch.asiankitchen.service.RestaurantInfoService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/restaurant-info")
@PreAuthorize("hasRole('ADMIN')")
public class RestaurantInfoAdminController {
    private final RestaurantInfoService service;

    public RestaurantInfoAdminController(RestaurantInfoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<RestaurantInfoReadDTO> create(
            @Valid @RequestBody RestaurantInfoWriteDTO dto) {
        RestaurantInfoReadDTO created = service.create(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public List<RestaurantInfoReadDTO> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public RestaurantInfoReadDTO getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public RestaurantInfoReadDTO update(
            @PathVariable UUID id,
            @Valid @RequestBody RestaurantInfoWriteDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}