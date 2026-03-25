package com.m3verificaciones.appweb.consumption.service;

import com.m3verificaciones.appweb.consumption.exception.*;
import com.m3verificaciones.appweb.consumption.repository.ConsumptionPerHourRepository;
import com.m3verificaciones.appweb.consumption.repository.MeterRepository;
import com.m3verificaciones.appweb.consumption.model.ConsumptionPerDay;
import com.m3verificaciones.appweb.consumption.model.ConsumptionPerHour;

import java.util.List;
import java.math.BigDecimal;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ConsumptionPerHourService {
    private final ConsumptionPerHourRepository consumptionPerHourRepository;
    private final ConsumptionPerDayService consumptionPerDayService;
    private final MeterRepository meterRepository;

    public ConsumptionPerHourService(ConsumptionPerHourRepository consumptionPerHourRepository,
            ConsumptionPerDayService consumptionPerDayService,
            MeterRepository meterRepository) {
        this.consumptionPerDayService = consumptionPerDayService;
        this.consumptionPerHourRepository = consumptionPerHourRepository;
        this.meterRepository = meterRepository;
    }

    @Transactional
    public ConsumptionPerHour saveConsumptionPerHour(ConsumptionPerHour consumptionPerHour,
            BigDecimal hourlyConsumption) {
        try {
            if (consumptionPerHour.getDevEui() == null || consumptionPerHour.getSerial() == null) {
                throw new InvalidConsumptionDataException("devEui or serial");
            }

            ConsumptionPerHour savedHourlyRecord = consumptionPerHourRepository.save(consumptionPerHour);
            consumptionPerDayService.saveOrUpdateConsumptionPerDay(
                    consumptionPerHour.getDevEui(),
                    consumptionPerHour.getSerial(),
                    consumptionPerHour.getDateConsumption(),
                    consumptionPerHour.getModel(),
                    consumptionPerHour.getDiameter(),
                    hourlyConsumption);
            return savedHourlyRecord;
        } catch (Exception e) {
            throw new ConsumptionPersistenceException("hourly consumption creation", e);
        }
    }

    @Transactional
    public boolean deleteConsumptionPerHour(String id) {
        if (!consumptionPerHourRepository.existsById(id)) {
            return false;
        }
        try {
            consumptionPerHourRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            throw new ConsumptionPersistenceException("hourly consumption deletion", e);
        }
    }

    public ConsumptionPerHour getConsumptionPerHourById(String id) {
        return consumptionPerHourRepository.findById(id)
                .orElseThrow(() -> new ConsumptionNotFoundException("hourly consumption with id: " + id));
    }

    public List<ConsumptionPerHour> getAllConsumptionsPerHour() {
        try {
            return consumptionPerHourRepository.findAll();
        } catch (Exception e) {
            throw new ConsumptionPersistenceException("hourly consumptions retrieval", e);
        }
    }

    @Transactional
    public ConsumptionPerHour updateConsumptionPerHour(String id, ConsumptionPerHour updatedConsumptionPerHour) {
        return consumptionPerHourRepository.findById(id)
                .map(existing -> {
                    try {
                        // Actualizar campos necesarios
                        existing.setHourlyConsumption(updatedConsumptionPerHour.getHourlyConsumption());
                        // ... otros campos que se puedan actualizar

                        return consumptionPerHourRepository.save(existing);
                    } catch (Exception e) {
                        throw new ConsumptionPersistenceException("hourly consumption update", e);
                    }
                })
                .orElseThrow(() -> new ConsumptionNotFoundException("hourly consumption with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<ConsumptionPerHour> getConsumptionsByDevEui(String devEui) {
        try {
            List<ConsumptionPerHour> records = consumptionPerHourRepository.findByDevEui(devEui);
            if (records.isEmpty()) {
                throw new ConsumptionNotFoundException("hourly consumptions for devEui: " + devEui);
            }
            return records;
        } catch (Exception e) {
            throw new ConsumptionPersistenceException("hourly consumptions retrieval by devEui", e);
        }
    }

    public List<ConsumptionPerHour> getConsumptionsByCompany(String companyUniqueKey) {
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
            List<ConsumptionPerHour> results = consumptionPerHourRepository.findByDevEuisOrderByDateDesc(devEuis);

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