package com.m3verificaciones.appweb.consumption.repository;

import com.m3verificaciones.appweb.consumption.model.Meter;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MeterRepository extends CrudRepository<Meter, String> {
    @Query("SELECT m FROM Meter m WHERE m.companyUniqueKey = :companyUniqueKey")
    List<Meter> findByCompanyUniqueKey(@Param("companyUniqueKey") String companyUniqueKey);
}
