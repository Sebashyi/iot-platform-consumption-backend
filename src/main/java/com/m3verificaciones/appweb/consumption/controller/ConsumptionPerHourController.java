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

import com.m3verificaciones.appweb.consumption.service.ConsumptionPerHourService;
import com.m3verificaciones.appweb.consumption.util.excell.ExcelStyleUtil;
import com.m3verificaciones.appweb.consumption.dto.ExcelExportRequestDTO;
import com.m3verificaciones.appweb.consumption.exception.ConsumptionNoResultsException;
import com.m3verificaciones.appweb.consumption.model.ConsumptionPerDay;
import com.m3verificaciones.appweb.consumption.model.ConsumptionPerHour;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;

import org.apache.poi.ss.usermodel.*;

@CrossOrigin(origins = "*", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST,
        RequestMethod.PUT, RequestMethod.DELETE })
@RestController
@RequestMapping("/m3verificaciones/api/v1/consumption-hour")
@Tag(name = "Hourly Consumption", description = "Hourly consumption management API")
public class ConsumptionPerHourController {
    private final ConsumptionPerHourService consumptionPerHourService;

    public ConsumptionPerHourController(ConsumptionPerHourService consumptionPerHourService) {
        this.consumptionPerHourService = consumptionPerHourService;
    }

    @Operation(summary = "Get all hourly consumptions", description = "Returns a list of all hourly consumption records")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved hourly consumptions", content = @Content(schema = @Schema(implementation = ConsumptionPerHour.class))),
            @ApiResponse(responseCode = "204", description = "No hourly consumptions found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<?> getAllConsumptionsPerHour() {
        try {
            List<ConsumptionPerHour> consumptions = consumptionPerHourService.getAllConsumptionsPerHour();
            if (consumptions == null || consumptions.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            return ResponseEntity.ok(consumptions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving hourly consumptions: " + e.getMessage());
        }
    }

    @Operation(summary = "Get hourly consumption by ID", description = "Returns an hourly consumption record by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved hourly consumption", content = @Content(schema = @Schema(implementation = ConsumptionPerHour.class))),
            @ApiResponse(responseCode = "404", description = "Hourly consumption not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getConsumptionPerHourById(@PathVariable String id) {
        try {
            ConsumptionPerHour consumption = consumptionPerHourService.getConsumptionPerHourById(id);
            if (consumption == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Hourly consumption with ID " + id + " not found");
            }
            return ResponseEntity.ok(consumption);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving hourly consumption: " + e.getMessage());
        }
    }

    @Operation(summary = "Update hourly consumption", description = "Updates an hourly consumption record by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hourly consumption successfully updated", content = @Content(schema = @Schema(implementation = ConsumptionPerHour.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Hourly consumption not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateConsumptionPerHour(@PathVariable String id,
            @RequestBody ConsumptionPerHour consumptionPerHour) {
        try {
            if (consumptionPerHour == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Hourly consumption data cannot be null");
            }

            ConsumptionPerHour updatedConsumption = consumptionPerHourService.updateConsumptionPerHour(id,
                    consumptionPerHour);

            if (updatedConsumption == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Hourly consumption with ID " + id + " not found");
            }

            return ResponseEntity.ok(updatedConsumption);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating hourly consumption: " + e.getMessage());
        }
    }

    @Operation(summary = "Delete hourly consumption", description = "Deletes an hourly consumption record by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Hourly consumption successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Hourly consumption not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteConsumptionPerHour(@PathVariable String id) {
        try {
            boolean deleted = consumptionPerHourService.deleteConsumptionPerHour(id);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Hourly consumption with ID " + id + " not found");
            }
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting hourly consumption: " + e.getMessage());
        }
    }

    @Operation(summary = "Get hourly consumptions by DevEUI", description = "Returns hourly consumptions for a specific device")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved hourly consumptions", content = @Content(schema = @Schema(implementation = ConsumptionPerHour.class))),
            @ApiResponse(responseCode = "400", description = "Invalid DevEUI parameter"),
            @ApiResponse(responseCode = "404", description = "No hourly consumptions found for the specified DevEUI"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/by-devEui")
    public ResponseEntity<?> getConsumptionsByDevEui(@RequestParam String devEui) {
        try {
            if (devEui == null || devEui.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("DevEUI parameter is required");
            }

            List<ConsumptionPerHour> results = consumptionPerHourService.getConsumptionsByDevEui(devEui);

            return ResponseEntity.ok(results);

        } catch (ConsumptionNoResultsException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid argument: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing request: " + e.getMessage());
        }
    }

    @Operation(summary = "Export hourly consumptions to Excel", description = "Exports selected hourly consumption data to an Excel file")
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
            Sheet sheet = workbook.createSheet("ConsumptionPerHour");

            CellStyle headerStyle = ExcelStyleUtil.createHeaderStyle(workbook);
            CellStyle dataStyle = ExcelStyleUtil.createDataStyle(workbook);

            // Create date-time style with explicit format
            CellStyle dateTimeStyle = workbook.createCellStyle();
            dateTimeStyle
                    .setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("dd/MM/yyyy HH:mm:ss"));

            Map<String, String> fieldTitles = Map.of(
                    "serial", "Serial",
                    "date", "Date and Time",
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

            List<ConsumptionPerHour> consumptions = consumptionPerHourService.getAllConsumptionsPerHour();

            int rowIndex = 1;
            for (ConsumptionPerHour consumption : consumptions) {
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
                                cell.setCellStyle(dateTimeStyle);
                            }
                            break;
                        case "consumption":
                            cell.setCellValue(consumption.getHourlyConsumption().doubleValue());
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
            responseHeaders.setContentDispositionFormData("attachment", "hourly_consumption.xlsx");

            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(outputStream.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating Excel file: " + e.getMessage()).getBytes());
        }
    }

    @Operation(summary = "Get hourly consumptions by company", description = "Returns hourly consumptions for all meters belonging to a company, ordered by date DESC")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved daily consumptions", content = @Content(schema = @Schema(implementation = ConsumptionPerDay.class))),
            @ApiResponse(responseCode = "404", description = "No meters or consumptions found for the company"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/by-company/{companyUniqueKey}")
    public ResponseEntity<?> getConsumptionsPerDayByCompany(@PathVariable String companyUniqueKey) {
        List<ConsumptionPerHour> results = consumptionPerHourService.getConsumptionsByCompany(companyUniqueKey);
        return ResponseEntity.ok(results);
    }
}