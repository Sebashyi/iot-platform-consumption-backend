package com.m3verificaciones.appweb.consumption.service;

import com.m3verificaciones.appweb.consumption.exception.*;
import com.m3verificaciones.appweb.consumption.repository.ConsumptionPerYearRepository;
import com.m3verificaciones.appweb.consumption.model.ConsumptionPerYear;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsumptionPerYearService {
    private final ConsumptionPerYearRepository consumptionPerYearRepository;

    public ConsumptionPerYearService(ConsumptionPerYearRepository consumptionPerYearRepository) {
        this.consumptionPerYearRepository = consumptionPerYearRepository;
    }

    @Transactional
    public ConsumptionPerYear saveConsumptionPerYear(ConsumptionPerYear consumptionPerYear) {
        try {
            if (consumptionPerYear.getDevEui() == null || consumptionPerYear.getSerial() == null) {
                throw new InvalidConsumptionDataException("devEui or serial");
            }
            return consumptionPerYearRepository.save(consumptionPerYear);
        } catch (Exception e) {
            throw new ConsumptionPersistenceException("yearly consumption creation", e);
        }
    }

    @Transactional
    public boolean deleteConsumptionPerYear(String id) {
        if (!consumptionPerYearRepository.existsById(id)) {
            return false;
        }
        try {
            consumptionPerYearRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            throw new ConsumptionPersistenceException("yearly consumption deletion", e);
        }
    }

    public ConsumptionPerYear getConsumptionPerYearById(String id) {
        return consumptionPerYearRepository.findById(id)
                .orElseThrow(() -> new ConsumptionNotFoundException("yearly consumption with id: " + id));
    }

    public List<ConsumptionPerYear> getAllConsumptionsPerYear() {
        try {
            return consumptionPerYearRepository.findAll();
        } catch (Exception e) {
            throw new ConsumptionPersistenceException("yearly consumptions retrieval", e);
        }
    }

    @Transactional
    public ConsumptionPerYear updateConsumptionPerYear(String id, ConsumptionPerYear updatedConsumptionPerYear) {
        return consumptionPerYearRepository.findById(id)
                .map(existing -> {
                    try {
                        // Actualizar campos necesarios
                        existing.setYearlyConsumption(updatedConsumptionPerYear.getYearlyConsumption());
                        existing.setPreviousConsumptionValue(updatedConsumptionPerYear.getPreviousConsumptionValue());
                        // ... otros campos que se puedan actualizar
                        
                        return consumptionPerYearRepository.save(existing);
                    } catch (Exception e) {
                        throw new ConsumptionPersistenceException("yearly consumption update", e);
                    }
                })
                .orElseThrow(() -> new ConsumptionNotFoundException("yearly consumption with id: " + id));
    }

    @Transactional
    public void saveOrUpdateConsumptionPerYear(String devEui, String serial, LocalDateTime dateConsumption, 
            String model, String diameter, BigDecimal monthlyConsumption) {
        try {
            LocalDateTime startOfYear = dateConsumption.withDayOfYear(1).toLocalDate().atStartOfDay();

            ConsumptionPerYear existingRecord = consumptionPerYearRepository.findByDevEuiAndSerialAndYear(devEui, serial,
                    startOfYear);

            if (existingRecord != null) {
                existingRecord.setYearlyConsumption(monthlyConsumption.add(existingRecord.getYearlyConsumption()));
                existingRecord.setPreviousConsumptionValue(monthlyConsumption);
                this.saveConsumptionPerYear(existingRecord);
            } else {
                ConsumptionPerYear newRecord = new ConsumptionPerYear();
                newRecord.setDevEui(devEui);
                newRecord.setSerial(serial);
                newRecord.setModel(model);
                newRecord.setDiameter(diameter);
                newRecord.setDateConsumption(startOfYear);
                newRecord.setPreviousConsumptionValue(monthlyConsumption);
                newRecord.setYearlyConsumption(BigDecimal.ZERO);
                this.saveConsumptionPerYear(newRecord);
            }
        } catch (Exception e) {
            throw new ConsumptionPersistenceException("yearly consumption saveOrUpdate operation", e);
        }
    }

    @Transactional(readOnly = true)
    public List<ConsumptionPerYear> getConsumptionsByDevEui(String devEui) {
        try {
            List<ConsumptionPerYear> records = consumptionPerYearRepository.findByDevEui(devEui);
            if (records.isEmpty()) {
                throw new ConsumptionNotFoundException("yearly consumptions for devEui: " + devEui);
            }
            return records;
        } catch (Exception e) {
            throw new ConsumptionPersistenceException("yearly consumptions retrieval by devEui", e);
        }
    }
}