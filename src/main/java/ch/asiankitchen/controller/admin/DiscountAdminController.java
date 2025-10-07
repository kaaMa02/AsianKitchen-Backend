package ch.asiankitchen.controller.admin;

import ch.asiankitchen.dto.DiscountConfigReadDTO;
import ch.asiankitchen.dto.DiscountConfigWriteDTO;
import ch.asiankitchen.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/discounts")
@RequiredArgsConstructor
public class DiscountAdminController {

    private final DiscountService service;

    @GetMapping("/current")
    public DiscountConfigReadDTO get() {
        return service.getCurrent();
    }

    @PutMapping("/current")
    public DiscountConfigReadDTO put(@RequestBody DiscountConfigWriteDTO dto) {
        return service.update(dto);
    }
}
