package com.m3verificaciones.appweb.consumption.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.m3verificaciones.appweb.consumption.service.ConsumptionPerDayService;
import com.m3verificaciones.appweb.consumption.util.excell.ExcelStyleUtil;
import com.m3verificaciones.appweb.consumption.model.ConsumptionPerDay;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.io.ByteArrayOutputStream;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.m3verificaciones.appweb.consumption.dto.ExcelExportRequestDTO;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@CrossOrigin(origins = "*", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST,
        RequestMethod.PUT, RequestMethod.DELETE })
@RestController
@RequestMapping("/m3verificaciones/api/v1/consumption-day")
@Tag(name = "Daily Consumption", description = "Daily consumption management API")
public class ConsumptionPerDayController {
    private final ConsumptionPerDayService consumptionPerDayService;

    public ConsumptionPerDayController(ConsumptionPerDayService consumptionPerDayService) {
        this.consumptionPerDayService = consumptionPerDayService;
    }

    @Operation(summary = "Get all daily consumptions", description = "Returns a list of all daily consumptions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved daily consumptions", content = @Content(schema = @Schema(implementation = ConsumptionPerDay.class))),
            @ApiResponse(responseCode = "204", description = "No daily consumptions found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<?> getAllConsumptionsPerDay() {
        try {
            List<ConsumptionPerDay> consumptions = consumptionPerDayService.getAllConsumptionsPerDay();
            if (consumptions == null || consumptions.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            return ResponseEntity.ok(consumptions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving daily consumptions: " + e.getMessage());
        }
    }

    @Operation(summary = "Get daily consumption by ID", description = "Returns a daily consumption by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved daily consumption", content = @Content(schema = @Schema(implementation = ConsumptionPerDay.class))),
            @ApiResponse(responseCode = "404", description = "Daily consumption not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getConsumptionPerDayById(@PathVariable String id) {
        try {
            ConsumptionPerDay consumption = consumptionPerDayService.getConsumptionPerDayById(id);
            if (consumption == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Daily consumption with ID " + id + " not found");
            }
            return ResponseEntity.ok(consumption);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving daily consumption: " + e.getMessage());
        }
    }

    @Operation(summary = "Update daily consumption", description = "Updates a daily consumption by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Daily consumption successfully updated", content = @Content(schema = @Schema(implementation = ConsumptionPerDay.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Daily consumption not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateConsumptionPerDay(@PathVariable String id,
            @RequestBody ConsumptionPerDay consumptionPerDay) {
        try {
            if (consumptionPerDay == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Daily consumption data cannot be null");
            }
            ConsumptionPerDay updatedConsumption = consumptionPerDayService.updateConsumptionPerDay(id,
                    consumptionPerDay);
            if (updatedConsumption == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Daily consumption with ID " + id + " not found");
            }
            return ResponseEntity.ok(updatedConsumption);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating daily consumption: " + e.getMessage());
        }
    }

    @Operation(summary = "Get daily consumptions by DevEUI", description = "Returns daily consumptions for a specific device")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved daily consumptions", content = @Content(schema = @Schema(implementation = ConsumptionPerDay.class))),
            @ApiResponse(responseCode = "400", description = "Invalid DevEUI parameter"),
            @ApiResponse(responseCode = "404", description = "No daily consumptions found for the specified DevEUI"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/by-devEui")
    public ResponseEntity<?> getConsumptionsPerDayByDevEui(@RequestParam String devEui) {
        try {
            if (devEui == null || devEui.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("DevEUI parameter is required");
            }

            List<ConsumptionPerDay> results = consumptionPerDayService.getConsumptionsByDevEui(devEui);

            if (results.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No records found for the provided DevEUI");
            }

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing request: " + e.getMessage());
        }
    }

    @Operation(summary = "Export daily consumptions to Excel", description = "Exports selected daily consumption data to an Excel file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Excel file successfully generated"),
            @ApiResponse(responseCode = "400", description = "Invalid request or no fields selected"),
            @ApiResponse(responseCode = "500", description = "Error generating Excel file")
    })
    @PostMapping("/export-excel")
    public ResponseEntity<byte[]> exportToExcel(@RequestBody ExcelExportRequestDTO request) {
        List<String> selectedFields = request.getSelectedFields();

        if (selectedFields == null || selectedFields.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("You must select at least one field to export".getBytes());
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("ConsumptionPerDay");

            CellStyle headerStyle = ExcelStyleUtil.createHeaderStyle(workbook);
            CellStyle dataStyle = ExcelStyleUtil.createDataStyle(workbook);

            // Create date style with explicit format
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("dd/MM/yyyy"));

            Map<String, String> fieldTitles = Map.of(
                    "serial", "Serial",
                    "date", "Date",
                    "consumption", "Consumption",
                    "diameter", "Diameter",
                    "model", "Model",
                    "devEui", "Dev EUI");

            Row headerRow = sheet.createRow(0);
            int colIndex = 0;
            for (String field : selectedFields) {
                if (fieldTitles.containsKey(field)) {
                    Cell cell = headerRow.createCell(colIndex++);
                    cell.setCellValue(fieldTitles.get(field));
                    cell.setCellStyle(headerStyle);
                }
            }

            List<ConsumptionPerDay> consumptions = consumptionPerDayService.getAllConsumptionsPerDay();

            int rowIndex = 1;
            for (ConsumptionPerDay consumption : consumptions) {
                Row row = sheet.createRow(rowIndex++);
                colIndex = 0;

                for (String field : selectedFields) {
                    Cell cell = row.createCell(colIndex++);
                    switch (field) {
                        case "serial":
                            cell.setCellValue(consumption.getSerial());
                            cell.setCellStyle(dataStyle);
                            break;
                        case "date":
                            if (consumption.getDateConsumption() != null) {
                                cell.setCellValue(consumption.getDateConsumption());
                                cell.setCellStyle(dateStyle);
                            }
                            break;
                        case "consumption":
                            cell.setCellValue(consumption.getDailyConsumption().doubleValue());
                            cell.setCellStyle(dataStyle);
                            break;
                        case "diameter":
                            cell.setCellValue(consumption.getDiameter());
                            cell.setCellStyle(dataStyle);
                            break;
                        case "model":
                            cell.setCellValue(consumption.getModel());
                            cell.setCellStyle(dataStyle);
                            break;
                        case "devEui":
                            cell.setCellValue(consumption.getDevEui());
                            cell.setCellStyle(dataStyle);
                            break;
                    }
                }
            }

            for (int i = 0; i < selectedFields.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            responseHeaders.setContentDispositionFormData("attachment", "daily_consumption.xlsx");

            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating Excel file: " + e.getMessage()).getBytes());
        }
    }

    @Operation(summary = "Get daily consumptions by company", description = "Returns daily consumptions for all meters belonging to a company, ordered by date DESC")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved daily consumptions", content = @Content(schema = @Schema(implementation = ConsumptionPerDay.class))),
            @ApiResponse(responseCode = "404", description = "No meters or consumptions found for the company"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/by-company/{companyUniqueKey}")
    public ResponseEntity<?> getConsumptionsPerDayByCompany(@PathVariable String companyUniqueKey) {
        List<ConsumptionPerDay> results = consumptionPerDayService.getConsumptionsByCompany(companyUniqueKey);
        return ResponseEntity.ok(results);
    }
}