package com.m3verificaciones.appweb.consumption.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.m3verificaciones.appweb.consumption.model.ConsumptionPerMonth;

public interface ConsumptionPerMonthRepository extends JpaRepository<ConsumptionPerMonth, String> {
    @Query("SELECT c FROM ConsumptionPerMonth c WHERE c.devEui = :devEui AND c.serial = :serial AND c.dateConsumption = :startOfMonth")
    ConsumptionPerMonth findByDevEuiAndSerialAndMonth(
            @Param("devEui") String devEui,
            @Param("serial") String serial,
            @Param("startOfMonth") LocalDateTime startOfMonth);

    @Query("SELECT c FROM ConsumptionPerMonth c WHERE c.devEui = :devEui")
    List<ConsumptionPerMonth> findByDevEui(@Param("devEui") String devEui);

    @Query("SELECT c FROM ConsumptionPerMonth c WHERE c.devEui IN :devEuis ORDER BY c.dateConsumption DESC")
    List<ConsumptionPerMonth> findByDevEuisOrderByDateDesc(@Param("devEuis") List<String> devEuis);
}
