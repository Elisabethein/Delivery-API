package com.delivery_api.Delivery.API.Services;

import com.delivery_api.Delivery.API.Entities.Atef;
import com.delivery_api.Delivery.API.Entities.Wpef;
import com.delivery_api.Delivery.API.Entities.Wsef;
import com.delivery_api.Delivery.API.Repository.AtefRepository;
import com.delivery_api.Delivery.API.Repository.WpefRepository;
import com.delivery_api.Delivery.API.Repository.WsefRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExtraFeesService {
    private final AtefRepository atefRepository;
    private final WsefRepository wsefRepository;
    private final WpefRepository wpefRepository;
    @Autowired
    public ExtraFeesService(AtefRepository atefRepository, WsefRepository wsefRepository, WpefRepository wpefRepository) {
        this.atefRepository = atefRepository;
        this.wsefRepository = wsefRepository;
        this.wpefRepository = wpefRepository;
    }

    /**
     * Get extra fees for bike or scooter based on temperature and weather phenomenon
     *
     * @param temperature       - current temperature
     * @param rbf               - current RBF value
     * @param weatherPhenomenon - current weather phenomenon
     * @return RBF value with extra fees
     */
    double getExtraFeesForBikeOrScooter(double temperature, double rbf, String weatherPhenomenon) {
        List<Atef> atefList = atefRepository.findAll();
        // iterate through the list of atef and add the fee if the temperature is within the borders
        for (Atef atef : atefList) {
            // Check if the temperature is below minimum or above maximum
            // Assuming the values are in format "<value", ">value" or "min-max"
            if (atef.getBorders().contains("<")) {
                if (temperature < Double.parseDouble(atef.getBorders().split("<")[1])) {
                    rbf += atef.getFee();
                }
            } else if (atef.getBorders().contains(">")) {
                if (temperature > Double.parseDouble(atef.getBorders().split(">")[1])) {
                    rbf += atef.getFee();
                }
            } else {
                //in case of intervals (accepts negative to negative, negative to positive, positive to positive)
                if (atef.getBorders().indexOf("-") == 0) {
                    //in case the first value is negative
                    int splitIndex = atef.getBorders().substring(atef.getBorders().indexOf("-") + 1).indexOf("-") + 1;
                    double min = Double.parseDouble(atef.getBorders().substring(0, splitIndex));
                    double max = Double.parseDouble(atef.getBorders().substring(splitIndex + 1));
                    if (temperature >= min && temperature <= max) {
                        rbf += atef.getFee();
                    }
                } else {
                    String[] interval = atef.getBorders().split("-");
                    if (temperature >= Double.parseDouble(interval[0]) && temperature <= Double.parseDouble(interval[1])) {
                        rbf += atef.getFee();
                    }
                }
            }
        }
        // iterate through the list of wpef if the current weather has any phenomenon and add the fee
        if (!weatherPhenomenon.isEmpty()) {
            List<Wpef> wpefList = wpefRepository.findAll();
            for (Wpef wpef : wpefList) {
                // Check if the weather phenomenon contains the value
                if (weatherPhenomenon.contains(wpef.getContaining())) {
                    // Database contains a value of -1 for forbidden usage
                    if (wpef.getFee() == -1) {
                        throw new IllegalArgumentException("Usage of selected vehicle type is forbidden");
                    }
                    rbf += wpef.getFee();
                }
            }
        }
        return rbf;
    }

    /**
     * Get extra fees for bike based on wind speed
     *
     * @param windSpeed - current wind speed
     * @param rbf       - current RBF value
     * @return RBF value with extra fees
     */
    double getExtraFeesForBike(double windSpeed, double rbf) {
        List<Wsef> wsefList = wsefRepository.findAll();
        // iterate through the list of wsef and add the fee if the wind speed is within the borders
        for (Wsef wsef : wsefList) {
            // Check if wind speed is above maximum, else check if it's within the interval (assuming there is no minimum wind speed)
            if (wsef.getBorders().contains(">")) {
                if (windSpeed > Double.parseDouble(wsef.getBorders().split(">")[1])) {
                    rbf += wsef.getFee();
                }
            } else {
                // assuming the values for wind speed are positive
                String[] interval = wsef.getBorders().split("-");
                if (windSpeed >= Double.parseDouble(interval[0]) && windSpeed <= Double.parseDouble(interval[1])) {
                    rbf += wsef.getFee();
                }
            }
        }
        return rbf;
    }

    /**
     * Change the extra fee rules for a specific table
     *
     * @param table    - table name (Atef, Wsef, Wpef)
     * @param oldValue - old value
     * @param newValue - new value (can be same as old value)
     * @param fee      - fee (can be same as existing fee)
     */
    public void changeExtraFeeRules(String table, String oldValue, String newValue, String fee) {
        // Update the value and fee for the given table
        switch (table) {
            case "Atef":
                Atef atef = atefRepository.findByBorders(oldValue);
                if (atef != null) {
                    atef.setBorders(newValue);
                    atef.setFee(Double.parseDouble(fee));
                    atefRepository.save(atef);
                    break;
                }
                else {
                    throw new IllegalArgumentException("Value not found");
                }
            case "Wsef":
                Wsef wsef = wsefRepository.findByBorders(oldValue);
                if (wsef != null) {
                    wsef.setBorders(newValue);
                    wsef.setFee(Double.parseDouble(fee));
                    wsefRepository.save(wsef);
                    break;
                }
                else {
                    throw new IllegalArgumentException("Value not found");
                }
            case "Wpef":
                Wpef wpef = wpefRepository.findByContaining(oldValue);
                if (wpef != null) {
                    wpef.setContaining(newValue);
                    wpef.setFee(Double.parseDouble(fee));
                    wpefRepository.save(wpef);
                    break;
                }
                else {
                    throw new IllegalArgumentException("Value not found");
                }
            default:
                throw new IllegalArgumentException("Invalid table name");
        }
    }

    /**
     * Add new extra fee rule
     *
     * @param table - table name (Atef, Wsef, Wpef)
     * @param value - value (string or interval(min-max, >value, <value, value))
     * @param fee   - fee
     */
    public void addExtraFeeRules(String table, String value, String fee) {
        // Create a new object and save it to the database
        switch (table) {
            case "Atef":
                Atef atef = new Atef(value, Double.parseDouble(fee));
                atefRepository.save(atef);
                break;
            case "Wsef":
                Wsef wsef = new Wsef(value, Double.parseDouble(fee));
                wsefRepository.save(wsef);
                break;
            case "Wpef":
                Wpef wpef = new Wpef(value, Double.parseDouble(fee));
                wpefRepository.save(wpef);
                break;
            default:
                throw new IllegalArgumentException("Invalid table name");
        }
    }

    /**
     * Delete extra fee rule
     *
     * @param table - table name (Atef, Wsef, Wpef)
     * @param value - existing value (if not found, changes not made)
     */
    public void deleteExtraFeeRules(String table, String value) {
        // Find the object based on value and delete it from the database
        switch (table) {
            case "Atef":
                Atef atef = atefRepository.findByBorders(value);
                if (atef != null) {
                    atefRepository.delete(atef);
                    break;
                }
                else {
                    throw new IllegalArgumentException("Value not found");
                }
            case "Wsef":
                Wsef wsef = wsefRepository.findByBorders(value);
                if (wsef != null) {
                    wsefRepository.delete(wsef);
                    break;
                }
                else {
                    throw new IllegalArgumentException("Value not found");
                }
            case "Wpef":
                Wpef wpef = wpefRepository.findByContaining(value);
                if (wpef != null) {
                    wpefRepository.delete(wpef);
                    break;
                }
                else {
                    throw new IllegalArgumentException("Value not found");
                }
            default:
                throw new IllegalArgumentException("Invalid table name");
        }
    }
}
