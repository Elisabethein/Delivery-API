package com.delivery_api.Delivery.API.Components;

import com.delivery_api.Delivery.API.Entities.*;
import com.delivery_api.Delivery.API.Repository.*;
import com.delivery_api.Delivery.API.Services.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

@Component
public class ExtraFeesInitializer implements ApplicationRunner {
    private final AtefRepository atefRepository;
    private final WsefRepository wsefRepository;
    private final WpefRepository wpefRepository;
    @Autowired
    public ExtraFeesInitializer(AtefRepository atefRepository, WsefRepository wsefRepository, WpefRepository wpefRepository) {
        this.atefRepository = atefRepository;
        this.wsefRepository = wsefRepository;
        this.wpefRepository = wpefRepository;
    }

    /**
     * Adding initial extra fees to the database
     *
     * @param args incoming application arguments
     * @throws Exception if an error occurred
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Add air temperature extra fees to the database
        List<Atef> atefList = Arrays.asList(
                new Atef("<-10", 1.0),
                new Atef("-10-0", 0.5)
        );
        atefRepository.saveAll(atefList);
        // Add wind speed extra fees to the database
        List<Wsef> wsefList = Arrays.asList(
                new Wsef("10-20", 0.5),
                new Wsef(">20", -1)
        );
        wsefRepository.saveAll(wsefList);
        // Add weather phenomenon extra fees to the database
        List<Wpef> wpefList = Arrays.asList(
                new Wpef("rain", 0.5),
                new Wpef("snow", 1),
                new Wpef("sleet", 1),
                new Wpef("glaze", -1),
                new Wpef("thunder", -1),
                new Wpef("hail", -1)
        );
        wpefRepository.saveAll(wpefList);
    }
}
