package br.edu.canteen.controller;

import br.edu.canteen.dto.SettingsRequest;
import br.edu.canteen.dto.SettingsResponse;
import br.edu.canteen.service.SettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/canteen")
@Tag(name = "Canteen", description = "Name, opening hours and service fee")
public class CanteenController {

    private final SettingsService settingsService;

    public CanteenController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    @Operation(summary = "Shows the canteen details and whether it is open now")
    public SettingsResponse get() {
        return settingsService.get();
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Updates the canteen details")
    public SettingsResponse refresh(@Valid @RequestBody SettingsRequest data) {
        return settingsService.refresh(data);
    }
}
