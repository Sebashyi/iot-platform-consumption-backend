package com.m3verificaciones.appweb.consumption.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.m3verificaciones.appweb.consumption.model.ConsumptionPerHour;

import java.util.List;

public interface ConsumptionPerHourRepository extends JpaRepository<ConsumptionPerHour, String> {
        ConsumptionPerHour findByDevEuiAndSerial(String devEui, String serial);

        @Query("SELECT c FROM ConsumptionPerHour c WHERE c.devEui = :devEui AND c.serial = :serial ORDER BY c.dateConsumption DESC")
        List<ConsumptionPerHour> findLatestByDevEuiAndSerial(
            @Param("devEui") String devEui,
            @Param("serial") String serial);

        @Query("SELECT c FROM ConsumptionPerHour c WHERE c.devEui = :devEui")
        List<ConsumptionPerHour> findByDevEui(@Param("devEui") String devEui);

}
