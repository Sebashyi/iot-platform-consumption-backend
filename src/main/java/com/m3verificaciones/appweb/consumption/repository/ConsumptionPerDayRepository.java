package com.m3verificaciones.appweb.consumption.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.m3verificaciones.appweb.consumption.model.ConsumptionPerDay;

public interface ConsumptionPerDayRepository extends JpaRepository<ConsumptionPerDay, String> {
    @Query("SELECT c FROM ConsumptionPerDay c WHERE c.devEui = :devEui AND c.serial = :serial AND c.dateConsumption = :startOfDay")
    ConsumptionPerDay findByDevEuiAndSerialAndDay(
            @Param("devEui") String devEui,
            @Param("serial") String serial,
            @Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT c FROM ConsumptionPerDay c WHERE c.devEui = :devEui")
    List<ConsumptionPerDay> findByDevEui(@Param("devEui") String devEui);
}
