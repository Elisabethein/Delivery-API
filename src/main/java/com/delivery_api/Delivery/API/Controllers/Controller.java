package com.delivery_api.Delivery.API.Controllers;

import com.delivery_api.Delivery.API.Services.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE}, produces = MediaType.APPLICATION_JSON_VALUE)
public class Controller {
    @Autowired
    private WeatherService weatherService; // Service class

    /**
     * Get RBF value for a specific city, vehicle type and datetime
     *
     * @param city        - city name
     * @param vehicleType - vehicle type
     * @param datetime    - datetime in format "yyyy-MM-dd HH:mm:ss", can be null
     * @return RBF value, or -1.0 if an error occurred
     */
    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping("/calculateRbf")
    public ResponseEntity<String> calculateRbfByTime(
            @RequestParam("city") String city,
            @RequestParam("vehicleType") String vehicleType,
            @RequestParam(value = "datetime", required = false) String datetime
    ) {
        try {
            System.out.println("Calculating RBF");
            double price = weatherService.calculateRbf(city, vehicleType, datetime);
            System.out.println("RBF calculated");
            return ResponseEntity.ok(String.valueOf(price));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred: " + e.getMessage());
        }
    }

    /**
     * Change base fee for a specific city and vehicle type
     *
     * @param forWhichCity    - city name
     * @param forWhichVehicle - vehicle type
     * @param fee             - new fee
     * @return "Business rules updated" if successful, or an error message if an error occurred
     */
    @PutMapping("/changeBaseFeeRules/{forWhichCity}/{forWhichVehicle}/{fee}")
    public ResponseEntity<String> changeBaseFeeRules(
            @PathVariable("forWhichCity") String forWhichCity,
            @PathVariable("forWhichVehicle") String forWhichVehicle,
            @PathVariable("fee") String fee
    ) {
        try {
            weatherService.changeBaseFeeRules(forWhichCity, forWhichVehicle, fee);
            return ResponseEntity.ok("Business rules updated");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred: " + e.getMessage());
        }
    }

    /**
     * Change extra fee tables' values or fees.
     * Insert old value as newValue if you only want to change fee.
     * Insert old fee as fee if you only want to change value.
     *
     * @param table    - table name (Atef, Wsef, Wpef)
     * @param oldValue - old value
     * @param newValue - new value
     * @param fee      - new fee
     * @return "Business rules updated" if successful, or an error message if an error occurred
     */
    @PutMapping("/changeExtraFeeRules/{table}/{oldValue}/{newValue}/{fee}")
    public ResponseEntity<String> changeBusinessRules(
            @PathVariable("table") String table,
            @PathVariable("oldValue") String oldValue,
            @PathVariable("newValue") String newValue,
            @PathVariable("fee") String fee
    ) {
        try {
            weatherService.changeExtraFeeRules(table, oldValue, newValue, fee);
            return ResponseEntity.ok("Business rules updated");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred: " + e.getMessage());
        }
    }

    /**
     * Add new extra fee rule
     *
     * @param table - table name (Atef, Wsef, Wpef)
     * @param value - value
     * @param fee   - fee
     * @return "Business rules updated" if successful, or an error message if an error occurred
     */
    @PostMapping("/addExtraFeeRules/{table}/{value}/{fee}")
    public ResponseEntity<String> addExtraFeeRules(
            @PathVariable("table") String table,
            @PathVariable("value") String value,
            @PathVariable("fee") String fee
    ) {
        try {
            weatherService.addExtraFeeRules(table, value, fee);
            return ResponseEntity.ok("Business rules updated");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred: " + e.getMessage());
        }
    }

    /**
     * Delete extra fee rule
     *
     * @param table - table name (Atef, Wsef, Wpef)
     * @param value - value
     * @return "Business rules updated" if successful, or an error message if an error occurred
     */
    @DeleteMapping("/deleteExtraFeeRules/{table}/{value}")
    public ResponseEntity<String> deleteExtraFeeRules(
            @PathVariable("table") String table,
            @PathVariable("value") String value
    ) {
        try {
            weatherService.deleteExtraFeeRules(table, value);
            return ResponseEntity.ok("Business rules updated");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred: " + e.getMessage());
        }
    }
}
