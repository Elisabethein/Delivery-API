package com.delivery_api.Delivery.API.Services;

import com.delivery_api.Delivery.API.Entities.Rbf;
import com.delivery_api.Delivery.API.Repository.RbfRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BaseFeeService {
    private final RbfRepository rbfRepository;

    @Autowired
    public BaseFeeService(RbfRepository rbfRepository) {
        this.rbfRepository = rbfRepository;
    }

    /**
     * Get the base fee for the given city and vehicle type
     *
     * @param city        - city name
     * @param vehicleType - vehicle type
     * @return base regional fee value
     */
    double getBaseRbf(String city, String vehicleType) {
        // Create a list for fees in one city and search for the vehicle type
        List<Rbf> rbfList = rbfRepository.findByCity(city);
        Optional<Rbf> rbfOptional = rbfList.stream()
                .filter(rbf -> rbf.getVehicle().equals(vehicleType))
                .findFirst();
        return rbfOptional.map(Rbf::getFee).orElse(-1.0);
    }

    /**
     * Change the base fee for a specific city and vehicle
     *
     * @param forWhichCity    - city name
     * @param forWhichVehicle - vehicle type
     * @param fee             - new fee
     */
    public void changeBaseFeeRules(String forWhichCity, String forWhichVehicle, String fee) {
        // Find the object based on city and vehicle type and update the fee
        Rbf rbf = rbfRepository.findByCityAndVehicle(forWhichCity, forWhichVehicle);
        if (rbf != null) {
            rbf.setFee(Double.parseDouble(fee));
            rbfRepository.save(rbf);
        }
        else {
            throw new IllegalArgumentException("City or vehicle not found");
        }
    }
}
