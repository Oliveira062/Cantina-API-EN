package br.edu.canteen.dto;

import java.math.BigDecimal;
import java.time.LocalTime;

public record SettingsResponse(String name, BigDecimal serviceFee, LocalTime opensAt, LocalTime closesAt,
                                   boolean enforceHours, boolean openNow) {
}
