package com.m3verificaciones.appweb.consumption.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.m3verificaciones.appweb.consumption.model.Consumption;

public interface ConsumptionRepository extends JpaRepository<Consumption, String> {
}
