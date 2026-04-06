package com.m3verificaciones.appweb.consumption.service;

import com.m3verificaciones.appweb.consumption.exception.*;
import com.m3verificaciones.appweb.consumption.repository.ConsumptionPerMonthRepository;
import com.m3verificaciones.appweb.consumption.model.ConsumptionPerMonth;
import com.m3verificaciones.appweb.consumption.repository.MeterRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsumptionPerMonthService {
    private final ConsumptionPerMonthRepository consumptionPerMonthRepository;
    private final ConsumptionPerYearService consumptionPerYearService;
    private final MeterRepository meterRepository;

    public ConsumptionPerMonthService(ConsumptionPerMonthRepository consumptionPerMonthRepository,
            ConsumptionPerYearService consumptionPerYearService,
            MeterRepository meterRepository) {
        this.consumptionPerYearService = consumptionPerYearService;
        this.consumptionPerMonthRepository = consumptionPerMonthRepository;
        this.meterRepository = meterRepository;
    }

    @Transactional
    public ConsumptionPerMonth saveConsumptionPerMonth(ConsumptionPerMonth consumptionPerMonth,
            BigDecimal monthlyConsumption) {
        try {
            if (consumptionPerMonth.getDevEui() == null || consumptionPerMonth.getSerial() == null) {
                throw new InvalidConsumptionDataException("devEui or serial");
            }

            ConsumptionPerMonth savedMonthlyRecord = consumptionPerMonthRepository.save(consumptionPerMonth);
            consumptionPerYearService.saveOrUpdateConsumptionPerYear(
                    consumptionPerMonth.getDevEui(),
                    consumptionPerMonth.getSerial(),
                    consumptionPerMonth.getDateConsumption(),
                    consumptionPerMonth.getModel(),
                    consumptionPerMonth.getDiameter(),
                    monthlyConsumption);
            return savedMonthlyRecord;
        } catch (Exception e) {
            throw new ConsumptionPersistenceException("monthly consumption creation", e);
        }
    }

    @Transactional
    public boolean deleteConsumptionPerMonth(String id) {
        if (!consumptionPerMonthRepository.existsById(id)) {
            return false;
        }
        try {
            consumptionPerMonthRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            throw new ConsumptionPersistenceException("monthly consumption deletion", e);
        }
    }

    public ConsumptionPerMonth getConsumptionPerMonthById(String id) {
        return consumptionPerMonthRepository.findById(id)
                .orElseThrow(() -> new ConsumptionNotFoundException("monthly consumption with id: " + id));
    }

    public List<ConsumptionPerMonth> getAllConsumptionsPerMonth() {
        try {
            return consumptionPerMonthRepository.findAll();
        } catch (Exception e) {
            throw new ConsumptionPersistenceException("monthly consumptions retrieval", e);
        }
    }

    @Transactional
    public ConsumptionPerMonth updateConsumptionPerMonth(String id, ConsumptionPerMonth updatedConsumptionPerMonth) {
        return consumptionPerMonthRepository.findById(id)
                .map(existing -> {
                    try {
                        // Actualizar campos necesarios
                        existing.setMonthlyConsumption(updatedConsumptionPerMonth.getMonthlyConsumption());
                        existing.setPreviousConsumptionValue(updatedConsumptionPerMonth.getPreviousConsumptionValue());
                        // ... otros campos que se puedan actualizar

                        return consumptionPerMonthRepository.save(existing);
                    } catch (Exception e) {
                        throw new ConsumptionPersistenceException("monthly consumption update", e);
                    }
                })
                .orElseThrow(() -> new ConsumptionNotFoundException("monthly consumption with id: " + id));
    }

    @Transactional
    public void saveOrUpdateConsumptionPerMonth(String devEui, String serial, LocalDateTime dateConsumption,
            String model, String diameter, BigDecimal dailyConsumption) {
        try {
            LocalDateTime startOfMonth = dateConsumption.withDayOfMonth(1).toLocalDate().atStartOfDay();

            ConsumptionPerMonth existingRecord = consumptionPerMonthRepository.findByDevEuiAndSerialAndMonth(devEui,
                    serial,
                    startOfMonth);

            if (existingRecord != null) {
                existingRecord.setMonthlyConsumption(dailyConsumption.add(existingRecord.getMonthlyConsumption()));
                existingRecord.setPreviousConsumptionValue(dailyConsumption);
                this.saveConsumptionPerMonth(existingRecord, dailyConsumption);
            } else {
                ConsumptionPerMonth newRecord = new ConsumptionPerMonth();
                newRecord.setDevEui(devEui);
                newRecord.setSerial(serial);
                newRecord.setModel(model);
                newRecord.setDiameter(diameter);
                newRecord.setDateConsumption(startOfMonth);
                newRecord.setPreviousConsumptionValue(dailyConsumption);
                newRecord.setMonthlyConsumption(BigDecimal.ZERO);
                this.saveConsumptionPerMonth(newRecord, BigDecimal.ZERO);
            }
        } catch (Exception e) {
            throw new ConsumptionPersistenceException("monthly consumption saveOrUpdate operation", e);
        }
    }

    @Transactional(readOnly = true)
    public List<ConsumptionPerMonth> getConsumptionsByDevEui(String devEui) {
        try {
            List<ConsumptionPerMonth> records = consumptionPerMonthRepository.findByDevEui(devEui);
            if (records.isEmpty()) {
                throw new ConsumptionNotFoundException("monthly consumptions for devEui: " + devEui);
            }
            return records;
        } catch (ConsumptionNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ConsumptionPersistenceException("monthly consumptions retrieval by devEui", e);
        }
    }

    public List<ConsumptionPerMonth> getConsumptionsByCompany(String companyUniqueKey) {
        List<String> devEuis = meterRepository.findByCompanyUniqueKey(companyUniqueKey)
                .stream()
                .map(com.m3verificaciones.appweb.consumption.model.Meter::getDevEui)
                .filter(devEui -> devEui != null && !devEui.isBlank())
                .distinct()
                .toList();

        if (devEuis.isEmpty()) {
            throw new ConsumptionNoResultsException(
                    "No meters found for company: " + companyUniqueKey);
        }

        try {
            List<ConsumptionPerMonth> results = consumptionPerMonthRepository.findByDevEuisOrderByDateDesc(devEuis);

            if (results.isEmpty()) {
                throw new ConsumptionNoResultsException(
                        "No consumption records found for the meters of company: " + companyUniqueKey);
            }

            return results;
        } catch (ConsumptionNoResultsException e) {
            throw e;
        } catch (Exception e) {
            throw new ConsumptionPersistenceException("retrieval by company", e);
        }
    }
}