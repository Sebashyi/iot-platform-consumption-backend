package com.m3verificaciones.appweb.consumption.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;

import com.m3verificaciones.appweb.consumption.service.ConsumptionPerMonthService;
import com.m3verificaciones.appweb.consumption.util.excell.ExcelStyleUtil;
import com.m3verificaciones.appweb.consumption.dto.ExcelExportRequestDTO;
import com.m3verificaciones.appweb.consumption.model.ConsumptionPerDay;
import com.m3verificaciones.appweb.consumption.model.ConsumptionPerMonth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.*;

@CrossOrigin(origins = "*", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST,
        RequestMethod.PUT, RequestMethod.DELETE })
@RestController
@RequestMapping("/m3verificaciones/api/v1/consumption-month")
@Tag(name = "Monthly Consumption", description = "Monthly consumption management API")
public class ConsumptionPerMonthController {
    private final ConsumptionPerMonthService consumptionPerMonthService;

    public ConsumptionPerMonthController(ConsumptionPerMonthService consumptionPerMonthService) {
        this.consumptionPerMonthService = consumptionPerMonthService;
    }

    @Operation(summary = "Get all monthly consumptions", description = "Returns a list of all monthly consumptions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved monthly consumptions", content = @Content(schema = @Schema(implementation = ConsumptionPerMonth.class))),
            @ApiResponse(responseCode = "204", description = "No monthly consumptions found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<?> getAllConsumptionsPerMonth() {
        try {
            List<ConsumptionPerMonth> consumptions = consumptionPerMonthService.getAllConsumptionsPerMonth();
            if (consumptions == null || consumptions.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            return ResponseEntity.ok(consumptions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving monthly consumptions: " + e.getMessage());
        }
    }

    @Operation(summary = "Get monthly consumption by ID", description = "Returns a monthly consumption by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved monthly consumption", content = @Content(schema = @Schema(implementation = ConsumptionPerMonth.class))),
            @ApiResponse(responseCode = "404", description = "Monthly consumption not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getConsumptionPerMonthById(@PathVariable String id) {
        try {
            ConsumptionPerMonth consumption = consumptionPerMonthService.getConsumptionPerMonthById(id);
            if (consumption == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Monthly consumption with ID " + id + " not found");
            }
            return ResponseEntity.ok(consumption);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving monthly consumption: " + e.getMessage());
        }
    }

    @Operation(summary = "Update monthly consumption", description = "Updates a monthly consumption by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monthly consumption successfully updated", content = @Content(schema = @Schema(implementation = ConsumptionPerMonth.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Monthly consumption not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateConsumptionPerMonth(@PathVariable String id,
            @RequestBody ConsumptionPerMonth consumptionPerMonth) {
        try {
            if (consumptionPerMonth == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Monthly consumption data cannot be null");
            }
            ConsumptionPerMonth updatedConsumption = consumptionPerMonthService.updateConsumptionPerMonth(id,
                    consumptionPerMonth);
            if (updatedConsumption == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Monthly consumption with ID " + id + " not found");
            }
            return ResponseEntity.ok(updatedConsumption);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating monthly consumption: " + e.getMessage());
        }
    }

    @Operation(summary = "Delete monthly consumption", description = "Deletes a monthly consumption by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Monthly consumption successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Monthly consumption not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteConsumptionPerMonth(@PathVariable String id) {
        try {
            boolean deleted = consumptionPerMonthService.deleteConsumptionPerMonth(id);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Monthly consumption with ID " + id + " not found");
            }
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting monthly consumption: " + e.getMessage());
        }
    }

    @Operation(summary = "Get monthly consumptions by DevEUI", description = "Returns monthly consumptions for a specific device")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved monthly consumptions", content = @Content(schema = @Schema(implementation = ConsumptionPerMonth.class))),
            @ApiResponse(responseCode = "400", description = "Invalid DevEUI parameter"),
            @ApiResponse(responseCode = "404", description = "No monthly consumptions found for the specified DevEUI"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/by-devEui")
    public ResponseEntity<?> getConsumptionsPerMonthByDevEui(@RequestParam String devEui) {
        try {
            if (devEui == null || devEui.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("DevEUI parameter is required");
            }

            List<ConsumptionPerMonth> results = consumptionPerMonthService.getConsumptionsByDevEui(devEui);

            if (results.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No records found for the provided DevEUI: " + devEui);
            }

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing request: " + e.getMessage());
        }
    }

    @Operation(summary = "Export monthly consumptions to Excel", description = "Exports selected monthly consumption data to an Excel file")
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
            Sheet sheet = workbook.createSheet("ConsumptionPerMonth");

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

            List<ConsumptionPerMonth> consumptions = consumptionPerMonthService.getAllConsumptionsPerMonth();

            int rowIndex = 1;
            for (ConsumptionPerMonth consumption : consumptions) {
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
                                cell.setCellStyle(dateStyle); // Apply date style
                            }
                            break;
                        case "consumption":
                            cell.setCellValue(consumption.getMonthlyConsumption().doubleValue());
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
            responseHeaders.setContentDispositionFormData("attachment", "monthly_consumption.xlsx");

            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating Excel file: " + e.getMessage()).getBytes());
        }
    }

    @Operation(summary = "Get monthly consumptions by company", description = "Returns monthly consumptions for all meters belonging to a company, ordered by date DESC")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved daily consumptions", content = @Content(schema = @Schema(implementation = ConsumptionPerDay.class))),
            @ApiResponse(responseCode = "404", description = "No meters or consumptions found for the company"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/by-company/{companyUniqueKey}")
    public ResponseEntity<?> getConsumptionsPerMonthByCompany(@PathVariable String companyUniqueKey) {
        List<ConsumptionPerMonth> results = consumptionPerMonthService.getConsumptionsByCompany(companyUniqueKey);
        return ResponseEntity.ok(results);
    }
}