package com.m3verificaciones.appweb.consumption.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.m3verificaciones.appweb.consumption.util.unique_id.UniqueIdGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import jakarta.validation.constraints.NotBlank;

@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "consumption_per_year",
uniqueConstraints = @UniqueConstraint(columnNames = {"devEui", "dateConsumption"}))
public class ConsumptionPerYear {
    @Id
    @Column(length = 10, unique = true, nullable = false, name = "unique_key")
    private String uniqueKey;

    @NotBlank
    @Column(name = "dev_eui")
    private String devEui;

    private String serial;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, name = "date_consumption")
    private LocalDateTime dateConsumption;

    @Column(nullable = false, name = "yearly_consumption")
    private BigDecimal yearlyConsumption;

    @Column(nullable = false, name = "previous_consumption_value")
    private BigDecimal previousConsumptionValue;

    private String model;

    private String diameter;

    @Transient
    @JsonIgnore
    private UniqueIdGenerator key;

    @PrePersist
    protected void onCreate() {
        this.uniqueKey = UniqueIdGenerator.generateUniqueKey();
    }
}
