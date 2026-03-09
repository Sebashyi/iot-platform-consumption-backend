package com.m3verificaciones.appweb.consumption.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;

import com.m3verificaciones.appweb.consumption.service.ConsumptionPerYearService;
import com.m3verificaciones.appweb.consumption.util.excell.ExcelStyleUtil;
import com.m3verificaciones.appweb.consumption.dto.ExcelExportRequestDTO;
import com.m3verificaciones.appweb.consumption.model.ConsumptionPerYear;

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
@RequestMapping("/m3verificaciones/api/v1/consumption-year")
@Tag(name = "Yearly Consumption", description = "Yearly consumption management API")
public class ConsumptionPerYearController {
    private final ConsumptionPerYearService consumptionPerYearService;

    public ConsumptionPerYearController(ConsumptionPerYearService consumptionPerYearService) {
        this.consumptionPerYearService = consumptionPerYearService;
    }

    @Operation(summary = "Get all yearly consumptions", description = "Returns a list of all yearly consumptions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved yearly consumptions", content = @Content(schema = @Schema(implementation = ConsumptionPerYear.class))),
            @ApiResponse(responseCode = "204", description = "No yearly consumptions found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<?> getAllConsumptionsPerYear() {
        try {
            List<ConsumptionPerYear> consumptions = consumptionPerYearService.getAllConsumptionsPerYear();
            if (consumptions == null || consumptions.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            return ResponseEntity.ok(consumptions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving yearly consumptions: " + e.getMessage());
        }
    }

    @Operation(summary = "Get yearly consumption by ID", description = "Returns a yearly consumption by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved yearly consumption", content = @Content(schema = @Schema(implementation = ConsumptionPerYear.class))),
            @ApiResponse(responseCode = "404", description = "Yearly consumption not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getConsumptionPerYearById(@PathVariable String id) {
        try {
            ConsumptionPerYear consumption = consumptionPerYearService.getConsumptionPerYearById(id);
            if (consumption == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Yearly consumption with ID " + id + " not found");
            }
            return ResponseEntity.ok(consumption);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving yearly consumption: " + e.getMessage());
        }
    }

    @Operation(summary = "Create yearly consumption", description = "Creates a new yearly consumption record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Yearly consumption successfully created", content = @Content(schema = @Schema(implementation = ConsumptionPerYear.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<?> createConsumptionPerYear(@RequestBody ConsumptionPerYear consumptionPerYear) {
        try {
            if (consumptionPerYear == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Yearly consumption data cannot be null");
            }
            ConsumptionPerYear createdConsumption = consumptionPerYearService
                    .saveConsumptionPerYear(consumptionPerYear);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdConsumption);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating yearly consumption: " + e.getMessage());
        }
    }

    @Operation(summary = "Update yearly consumption", description = "Updates a yearly consumption by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Yearly consumption successfully updated", content = @Content(schema = @Schema(implementation = ConsumptionPerYear.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Yearly consumption not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateConsumptionPerYear(@PathVariable String id,
            @RequestBody ConsumptionPerYear consumptionPerYear) {
        try {
            if (consumptionPerYear == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Yearly consumption data cannot be null");
            }

            ConsumptionPerYear updatedConsumption = consumptionPerYearService.updateConsumptionPerYear(id,
                    consumptionPerYear);

            if (updatedConsumption == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Yearly consumption with ID " + id + " not found");
            }

            return ResponseEntity.ok(updatedConsumption);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating yearly consumption: " + e.getMessage());
        }
    }

    @Operation(summary = "Delete yearly consumption", description = "Deletes a yearly consumption by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Yearly consumption successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Yearly consumption not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteConsumptionPerYear(@PathVariable String id) {
        try {
            boolean deleted = consumptionPerYearService.deleteConsumptionPerYear(id);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Yearly consumption with ID " + id + " not found");
            }
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting yearly consumption: " + e.getMessage());
        }
    }

    @Operation(summary = "Get yearly consumptions by DevEUI", description = "Returns yearly consumptions for a specific device")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved yearly consumptions", content = @Content(schema = @Schema(implementation = ConsumptionPerYear.class))),
            @ApiResponse(responseCode = "400", description = "Invalid DevEUI parameter"),
            @ApiResponse(responseCode = "404", description = "No yearly consumptions found for the specified DevEUI"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/by-devEui")
    public ResponseEntity<?> getConsumptionsPerYearByDevEui(@RequestParam String devEui) {
        try {
            if (devEui == null || devEui.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("DevEUI parameter is required");
            }

            List<ConsumptionPerYear> results = consumptionPerYearService.getConsumptionsByDevEui(devEui);

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

    @Operation(summary = "Export yearly consumptions to Excel", description = "Exports selected yearly consumption data to an Excel file")
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
            Sheet sheet = workbook.createSheet("ConsumptionPerYear");

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

            List<ConsumptionPerYear> consumptions = consumptionPerYearService.getAllConsumptionsPerYear();

            int rowIndex = 1;
            for (ConsumptionPerYear consumption : consumptions) {
                Row row = sheet.createRow(rowIndex++);
                colIndex = 0;

                for (String field : selectedFields) {
                    Cell cell = row.createCell(colIndex++);
                    fillCellWithData(field, consumption, cell, dateStyle, dataStyle);
                }
            }

            for (int i = 0; i < selectedFields.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            responseHeaders.setContentDispositionFormData("attachment", "yearly_consumption.xlsx");

            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating Excel file: " + e.getMessage()).getBytes());
        }
    }

    private void fillCellWithData(String field, ConsumptionPerYear consumption, Cell cell, CellStyle dateStyle,
            CellStyle dataStyle) {
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
                cell.setCellValue(consumption.getYearlyConsumption().doubleValue());
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