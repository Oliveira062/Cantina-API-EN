package br.edu.canteen.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "canteen_settings")
public class CanteenSettings {

    public static final Long SINGLE_ID = 1L;

    @Id
    private Long id = SINGLE_ID;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "service_fee", nullable = false, precision = 5, scale = 4)
    private BigDecimal serviceFee;

    @Column(nullable = false)
    private LocalTime opensAt;

    @Column(nullable = false)
    private LocalTime closesAt;

    @Column(name = "enforce_hours", nullable = false)
    private boolean enforceHours;

    protected CanteenSettings() {
    }

    public boolean isOpen(LocalTime time) {
        return !time.isBefore(opensAt) && time.isBefore(closesAt);
    }

    public void refresh(String name, BigDecimal serviceFee, LocalTime opensAt, LocalTime closesAt,
                          boolean enforceHours) {
        this.name = name.trim();
        this.serviceFee = serviceFee;
        this.opensAt = opensAt;
        this.closesAt = closesAt;
        this.enforceHours = enforceHours;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getServiceFee() { return serviceFee; }
    public LocalTime getOpensAt() { return opensAt; }
    public LocalTime getClosesAt() { return closesAt; }
    public boolean isEnforceHours() { return enforceHours; }
}
