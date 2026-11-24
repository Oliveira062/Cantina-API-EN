package br.edu.canteen.service;

import br.edu.canteen.config.CanteenProperties;
import br.edu.canteen.dto.SettingsRequest;
import br.edu.canteen.dto.SettingsResponse;
import br.edu.canteen.exception.InvalidDataException;
import br.edu.canteen.exception.InvalidOrderOperationException;
import br.edu.canteen.model.CanteenSettings;
import br.edu.canteen.repository.SettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZoneId;

@Service
public class SettingsService {

    private final SettingsRepository repository;
    private final ZoneId zone;

    public SettingsService(SettingsRepository repository, CanteenProperties properties) {
        this.repository = repository;
        this.zone = ZoneId.of(properties.timeZone());
    }

    @Transactional(readOnly = true)
    public CanteenSettings load() {
        return repository.findById(CanteenSettings.SINGLE_ID)
                .orElseThrow(() -> new IllegalStateException("Canteen settings not found in the database."));
    }

    @Transactional(readOnly = true)
    public BigDecimal serviceFee() {
        return load().getServiceFee();
    }

    @Transactional(readOnly = true)
    public SettingsResponse get() {
        return toResponse(load());
    }

    @Transactional
    public SettingsResponse refresh(SettingsRequest data) {
        if (!data.opensAt().isBefore(data.closesAt())) {
            throw new InvalidDataException("The opening time must be before the closing time.");
        }
        CanteenSettings config = load();
        config.refresh(data.name(), data.serviceFee(), data.opensAt(), data.closesAt(),
                data.enforceHours());
        return toResponse(repository.save(config));
    }

    @Transactional(readOnly = true)
    public void checkOpeningHours() {
        CanteenSettings config = load();
        if (config.isEnforceHours() && !config.isOpen(LocalTime.now(zone))) {
            throw new InvalidOrderOperationException(String.format(
                    "The canteen is closed. Opening hours: %s to %s.",
                    config.getOpensAt(), config.getClosesAt()));
        }
    }

    private SettingsResponse toResponse(CanteenSettings config) {
        return new SettingsResponse(config.getName(), config.getServiceFee(), config.getOpensAt(),
                config.getClosesAt(), config.isEnforceHours(), config.isOpen(LocalTime.now(zone)));
    }
}
