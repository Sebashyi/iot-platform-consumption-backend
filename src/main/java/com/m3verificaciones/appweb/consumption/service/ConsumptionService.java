package com.m3verificaciones.appweb.consumption.service;

import com.m3verificaciones.appweb.consumption.model.Consumption;
import com.m3verificaciones.appweb.consumption.model.ConsumptionPerHour;
import com.m3verificaciones.appweb.consumption.repository.ConsumptionRepository;
import com.m3verificaciones.appweb.consumption.repository.ConsumptionPerHourRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;

@Service
public class ConsumptionService {

    private final ConsumptionRepository consumptionRepository;
    private final ConsumptionPerHourService consumptionPerHourService;
    private final ConsumptionPerHourRepository consumptionPerHourRepository;

    public ConsumptionService(ConsumptionRepository consumptionRepository,
            ConsumptionPerHourService consumptionPerHourService, ConsumptionPerHourRepository consumptionPerHourRepository) {
        this.consumptionPerHourRepository = consumptionPerHourRepository;
        this.consumptionPerHourService = consumptionPerHourService;
        this.consumptionRepository = consumptionRepository;
    }

    public Consumption saveConsumption(Consumption consumption) {
        consumptionRepository.save(consumption);
    
        LocalDateTime dateConsumption = consumption.getDateConsumption().withNano(0);
        LocalDateTime hourlySlot = dateConsumption.withMinute(0).withSecond(0).withNano(0).plusHours(1);
    
        ConsumptionPerHour existingRecord = consumptionPerHourRepository.findByDevEuiAndSerialAndHour(
            consumption.getDevEui(),
            consumption.getSerial(),
            hourlySlot
        );
    
        BigDecimal newConsumptionValue = consumption.getConsumptionValue();
    
        if (existingRecord != null) {
            BigDecimal previousConsumptionValue = existingRecord.getPreviousConsumptionValue();
    
            if (previousConsumptionValue.compareTo(newConsumptionValue) == 0) {
                throw new IllegalArgumentException("Dato duplicado: ya existe un registro con el mismo valor de consumo para esta hora.");
            }
    
            BigDecimal hourlyConsumption = newConsumptionValue.subtract(previousConsumptionValue);
    
            if (hourlyConsumption.compareTo(BigDecimal.ZERO) < 0) {
                hourlyConsumption = BigDecimal.ZERO;
            }

            BigDecimal updatedTotal = existingRecord.getHourlyConsumption().add(hourlyConsumption);
            if (updatedTotal.compareTo(BigDecimal.ZERO) < 0) {
                updatedTotal = BigDecimal.ZERO;
            }
    
            existingRecord.setHourlyConsumption(updatedTotal);
            existingRecord.setPreviousConsumptionValue(newConsumptionValue);
            existingRecord.setDateConsumption(hourlySlot);
    
            consumptionPerHourService.saveConsumptionPerHour(existingRecord, hourlyConsumption);
        } else {
            ConsumptionPerHour lastRecord = consumptionPerHourRepository.findLatestByDevEuiAndSerial(consumption.getDevEui(), consumption.getSerial());

            BigDecimal hourlyConsumption = BigDecimal.ZERO;
            if (lastRecord != null) {
                hourlyConsumption = newConsumptionValue.subtract(lastRecord.getPreviousConsumptionValue());
                if (hourlyConsumption.compareTo(BigDecimal.ZERO) < 0) {
                    hourlyConsumption = BigDecimal.ZERO;
                }
            }
            ConsumptionPerHour newRecord = new ConsumptionPerHour();
            newRecord.setDevEui(consumption.getDevEui());
            newRecord.setSerial(consumption.getSerial());
            newRecord.setModel(consumption.getModel());
            newRecord.setDiameter(consumption.getDiameter());
            newRecord.setDateConsumption(hourlySlot);
            newRecord.setHourlyConsumption(hourlyConsumption);
            newRecord.setPreviousConsumptionValue(newConsumptionValue);
        
            consumptionPerHourService.saveConsumptionPerHour(newRecord, hourlyConsumption);
        }
    
        return consumption;
    }

    public boolean deleteConsumption(String id) {
        try {
            consumptionRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Consumption getConsumptionById(String id) {
        return consumptionRepository.findById(id).orElse(null);
    }

    public List<Consumption> getAllConsumptions() {
        return consumptionRepository.findAll();
    }

    @Transactional
    public Consumption updateConsumption(String id, Consumption updatedConsumption) {
        return consumptionRepository.findById(id).map(consumptionRepository::save).orElse(null);
    }
}
