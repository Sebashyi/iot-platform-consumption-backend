package com.m3verificaciones.appweb.consumption.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.m3verificaciones.appweb.consumption.model.ConsumptionPerYear;

public interface ConsumptionPerYearRepository extends JpaRepository<ConsumptionPerYear, String> {
    @Query("SELECT c FROM ConsumptionPerYear c WHERE c.devEui = :devEui AND c.serial = :serial AND c.dateConsumption = :startOfYear")
    ConsumptionPerYear findByDevEuiAndSerialAndYear(
            @Param("devEui") String devEui,
            @Param("serial") String serial,
            @Param("startOfYear") LocalDateTime startOfYear);

    @Query("SELECT c FROM ConsumptionPerYear c WHERE c.devEui = :devEui")
    List<ConsumptionPerYear> findByDevEui(@Param("devEui") String devEui);

    @Query("SELECT c FROM ConsumptionPerYear c WHERE c.devEui IN :devEuis ORDER BY c.dateConsumption DESC")
    List<ConsumptionPerYear> findByDevEuisOrderByDateDesc(@Param("devEuis") List<String> devEuis);
}
