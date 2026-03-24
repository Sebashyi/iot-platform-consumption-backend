package com.m3verificaciones.appweb.consumption.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "meter")
public class Meter {
    @Id
    @Column(name = "unique_key")
    private String uniqueKey;

    @Column(name = "dev_eui")
    private String devEui;

    @Column(name = "company_unique_key")
    private String companyUniqueKey;
}
