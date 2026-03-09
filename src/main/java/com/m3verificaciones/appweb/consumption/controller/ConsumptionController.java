package com.m3verificaciones.appweb.consumption.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.m3verificaciones.appweb.consumption.service.ConsumptionService;
import com.m3verificaciones.appweb.consumption.model.Consumption;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
        RequestMethod.DELETE })
@RestController
@RequestMapping("/m3verificaciones/api/v1/consumption")
@Tag(name = "Consumption", description = "Consumption management API")
public class ConsumptionController {
    private final ConsumptionService consumptionService;

    public ConsumptionController(ConsumptionService consumptionService) {
        this.consumptionService = consumptionService;
    }

    @Operation(summary = "Get all consumptions", description = "Returns a list of all consumptions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved consumptions", content = @Content(schema = @Schema(implementation = Consumption.class))),
            @ApiResponse(responseCode = "204", description = "No consumptions found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<?> getAllConsumptions() {
        try {
            List<Consumption> consumptions = consumptionService.getAllConsumptions();
            if (consumptions.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            return ResponseEntity.ok(consumptions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving consumptions: " + e.getMessage());
        }
    }

    @Operation(summary = "Get consumption by ID", description = "Returns a consumption by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved consumption", content = @Content(schema = @Schema(implementation = Consumption.class))),
            @ApiResponse(responseCode = "404", description = "Consumption not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getConsumptionById(@PathVariable String id) {
        try {
            Consumption consumption = consumptionService.getConsumptionById(id);
            if (consumption == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Consumption with ID " + id + " not found");
            }
            return ResponseEntity.ok(consumption);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving consumption: " + e.getMessage());
        }
    }

    @Operation(summary = "Create a new consumption", description = "Creates a new consumption record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Consumption successfully created", content = @Content(schema = @Schema(implementation = Consumption.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<?> createConsumption(@RequestBody Consumption consumption) {
        try {
            if (consumption == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Consumption data cannot be null");
            }
            Consumption createdConsumption = consumptionService.saveConsumption(consumption);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdConsumption);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating consumption: " + e.getMessage());
        }
    }

    @Operation(summary = "Delete consumption by ID", description = "Deletes a consumption by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Consumption successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Consumption not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteConsumption(@PathVariable String id) {
        try {
            boolean isDeleted = consumptionService.deleteConsumption(id);
            if (!isDeleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Consumption with ID " + id + " not found");
            }
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting consumption: " + e.getMessage());
        }
    }

    @Operation(summary = "Update consumption by ID", description = "Updates a consumption by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consumption successfully updated", content = @Content(schema = @Schema(implementation = Consumption.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Consumption not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateConsumption(@PathVariable String id, @RequestBody Consumption consumption) {
        try {
            if (consumption == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Consumption data cannot be null");
            }
            Consumption updatedConsumption = consumptionService.updateConsumption(id, consumption);
            if (updatedConsumption == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Consumption with ID " + id + " not found");
            }
            return ResponseEntity.ok(updatedConsumption);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating consumption: " + e.getMessage());
        }
    }
}