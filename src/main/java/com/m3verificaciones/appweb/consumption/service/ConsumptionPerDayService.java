package com.m3verificaciones.appweb.consumption.service;

import com.m3verificaciones.appweb.consumption.exception.*;
import com.m3verificaciones.appweb.consumption.repository.ConsumptionPerDayRepository;
import com.m3verificaciones.appweb.consumption.model.ConsumptionPerDay;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsumptionPerDayService {
    private final ConsumptionPerDayRepository consumptionPerDayRepository;
    private final ConsumptionPerMonthService consumptionPerMonthService;

    public ConsumptionPerDayService(ConsumptionPerDayRepository consumptionPerDayRepository,
            ConsumptionPerMonthService consumptionPerMonthService) {
        this.consumptionPerMonthService = consumptionPerMonthService;
        this.consumptionPerDayRepository = consumptionPerDayRepository;
    }

    @Transactional
    public ConsumptionPerDay saveConsumptionPerDay(ConsumptionPerDay consumptionPerDay, BigDecimal dailyConsumption) {
        try {
            if (consumptionPerDay.getDevEui() == null || consumptionPerDay.getSerial() == null) {
                throw new InvalidConsumptionDataException("devEui or serial");
            }

            ConsumptionPerDay savedDailyRecord = consumptionPerDayRepository.save(consumptionPerDay);
            consumptionPerMonthService.saveOrUpdateConsumptionPerMonth(
                    consumptionPerDay.getDevEui(),
                    consumptionPerDay.getSerial(),
                    consumptionPerDay.getDateConsumption(),
                    consumptionPerDay.getModel(),
                    consumptionPerDay.getDiameter(),
                    dailyConsumption);
            return savedDailyRecord;
        } catch (Exception e) {
            throw new ConsumptionPersistenceException("creation", e);
        }
    }

    @Transactional
    public void deleteConsumptionPerDay(String id) {
        if (!consumptionPerDayRepository.existsById(id)) {
            throw new ConsumptionNotFoundException(id);
        }
        try {
            consumptionPerDayRepository.deleteById(id);
        } catch (Exception e) {
            throw new ConsumptionPersistenceException("deletion", e);
        }
    }

    public ConsumptionPerDay getConsumptionPerDayById(String id) {
        return consumptionPerDayRepository.findById(id)
                .orElseThrow(() -> new ConsumptionNotFoundException(id));
    }

    public List<ConsumptionPerDay> getAllConsumptionsPerDay() {
        try {
            return consumptionPerDayRepository.findAll();
        } catch (Exception e) {
            throw new ConsumptionPersistenceException("retrieval", e);
        }
    }

    @Transactional
    public ConsumptionPerDay updateConsumptionPerDay(String id, ConsumptionPerDay updatedConsumptionPerDay) {
        return consumptionPerDayRepository.findById(id)
                .map(existing -> {
                    // Actualizar campos necesarios
                    existing.setDailyConsumption(updatedConsumptionPerDay.getDailyConsumption());
                    existing.setPreviousConsumptionValue(updatedConsumptionPerDay.getPreviousConsumptionValue());
                    // ... otros campos que se puedan actualizar

                    try {
                        return consumptionPerDayRepository.save(existing);
                    } catch (Exception e) {
                        throw new ConsumptionPersistenceException("update", e);
                    }
                })
                .orElseThrow(() -> new ConsumptionNotFoundException(id));
    }

    @Transactional
    public void saveOrUpdateConsumptionPerDay(String devEui, String serial, LocalDateTime dateConsumption,
            String model, String diameter, BigDecimal hourlyConsumption) {
        try {
            LocalDateTime startOfDay = dateConsumption.toLocalDate().atStartOfDay();

            ConsumptionPerDay existingRecord = consumptionPerDayRepository.findByDevEuiAndSerialAndDay(devEui, serial,
                    startOfDay);

            if (existingRecord != null) {
                existingRecord.setDailyConsumption(hourlyConsumption.add(existingRecord.getDailyConsumption()));
                existingRecord.setPreviousConsumptionValue(hourlyConsumption);
                this.saveConsumptionPerDay(existingRecord, hourlyConsumption);
            } else {
                ConsumptionPerDay newRecord = new ConsumptionPerDay();
                newRecord.setDevEui(devEui);
                newRecord.setSerial(serial);
                newRecord.setModel(model);
                newRecord.setDiameter(diameter);
                newRecord.setDateConsumption(startOfDay);
                newRecord.setPreviousConsumptionValue(hourlyConsumption);
                newRecord.setDailyConsumption(BigDecimal.ZERO);
                this.saveConsumptionPerDay(newRecord, BigDecimal.ZERO);
            }
        } catch (Exception e) {
            throw new ConsumptionPersistenceException("saveOrUpdate operation", e);
        }
    }

    @Transactional(readOnly = true)
    public List<ConsumptionPerDay> getConsumptionsByDevEui(String devEui) {
        try {
            List<ConsumptionPerDay> records = consumptionPerDayRepository.findByDevEui(devEui);
            if (records.isEmpty()) {
                throw new ConsumptionNotFoundException("for devEui: " + devEui);
            }
            return records;
        } catch (Exception e) {
            throw new ConsumptionPersistenceException("retrieval by devEui", e);
        }
    }
}