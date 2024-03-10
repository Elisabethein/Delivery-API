package com.delivery_api.Delivery.API.Services;

import com.delivery_api.Delivery.API.Entities.*;
import com.delivery_api.Delivery.API.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Comparator;

@org.springframework.stereotype.Service
public class WeatherService {
    private final WeatherRepository weatherRepository;
    private final AtefRepository atefRepository;
    private final WsefRepository wsefRepository;
    private final WpefRepository wpefRepository;
    private final RbfRepository rbfRepository;
    private final RestTemplate restTemplate;

    private final BaseFeeService baseFeeService;
    private final ExtraFeesService extraFeesService;
    private static final String URL = "https://www.ilmateenistus.ee/ilma_andmed/xml/observations.php";

    @Autowired
    public WeatherService(WeatherRepository weatherRepository, AtefRepository atefRepository, WsefRepository wsefRepository, WpefRepository wpefRepository, RbfRepository rbfRepository, RestTemplate restTemplate, BaseFeeService baseFeeService, ExtraFeesService extraFeesService) {
        this.weatherRepository = weatherRepository;
        this.atefRepository = atefRepository;
        this.wsefRepository = wsefRepository;
        this.wpefRepository = wpefRepository;
        this.rbfRepository = rbfRepository;
        this.restTemplate = restTemplate;
        this.baseFeeService = baseFeeService;
        this.extraFeesService = extraFeesService;
    }

    /**
     * Calculating the delivery fee, based on the given city, vehicle type and datetime
     *
     * @param city        - city name
     * @param vehicleType - vehicle type
     * @param datetime    - datetime in format "yyyy-MM-dd HH:mm:ss", can be null
     * @return RBF value
     */
    public double calculateRbf(String city, String vehicleType, String datetime) {
        //Create a list for all weather data or fetch it if it's empty
        List<Weather> weatherList = weatherRepository.findAll();
        if (weatherList.isEmpty()) {
            fetchWeatherData();
            weatherList = weatherRepository.findAll();
        }
        double temperature = 0;
        double windSpeed = 0;
        String weatherPhenomenon = "";
        String station;

        // Get the station name based on the city and throw an exception if the city is invalid
        station = getStation(city);
        if (station == null) throw new IllegalArgumentException("Invalid city name");

        // Get the latest weather data for the given station or find the weather data during the given datetime
        Optional<Weather> latestWeather;
        if (datetime == null) {
            latestWeather = getLatestWeather(weatherList, station);
        } else {
            Timestamp queryTimestamp = Timestamp.valueOf(datetime);
            // Find the closest observation time that is before the given datetime (or the same time)
            latestWeather = weatherList.stream()
                    .filter(weather -> weather.getStation().equals(station) &&
                            weather.getObservation().compareTo(queryTimestamp) <= 0)
                    .max(Comparator.comparing(Weather::getObservation));
        }

        // Get weather parameters or throw an exception if no weather data was found
        if (latestWeather.isPresent()) {
            Weather weather = latestWeather.get();
            temperature = weather.getAir();
            windSpeed = weather.getWind();
            weatherPhenomenon = weather.getWeather();
        } else {
            throw new IllegalArgumentException("No weather data available for the given time");
        }

        // Use assistant method to calculate the base fee or throw an exception if the vehicle type is invalid
        double rbf = baseFeeService.getBaseRbf(city, vehicleType);
        if (rbf == -1) {
            throw new IllegalArgumentException("Invalid vehicle type");
        }

        // Use assistant method to calculate the extra fees based on temperature and weather phenomenon
        if (vehicleType.equals("Scooter") || vehicleType.equals("Bike")) {
            rbf = extraFeesService.getExtraFeesForBikeOrScooter(temperature, rbf, weatherPhenomenon);
        }

        // Use assistant method to calculate the extra fees based on wind speed
        if (vehicleType.equals("Bike")) {
            rbf = extraFeesService.getExtraFeesForBike(windSpeed, rbf);
        }
        return rbf;
    }

    /**
     * Get the station name based on the city
     *
     * @param city - city name
     * @return station name
     */
    private static String getStation(String city) {
        return switch (city) {
            case "Tallinn" -> "Tallinn-Harku";
            case "Tartu" -> "Tartu-Tõravere";
            case "Pärnu" -> "Pärnu";
            default -> null;
        };
    }

    /**
     * Get the latest weather data for the given station
     *
     * @param weatherList - list of weather data
     * @param station     - station name
     * @return latest weather object
     */
    private Optional<Weather> getLatestWeather(List<Weather> weatherList, String station) {
        return weatherList.stream()
                .filter(weather -> weather.getStation().equals(station))
                .max(Comparator.comparing(Weather::getObservation));
    }

    /**
     * Fetch weather data from the given URL and save it to the database
     * Scheduled to run every hour, 15 minutes past the hour
     * Value for cron is set in application.properties and can be changed
     */
    @Scheduled(cron = "${scheduling.weather.cron}")
    public void fetchWeatherData() {
        String xmlData = restTemplate.getForObject(URL, String.class);
        try {
            // Parse the XML data
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlData)));
            Element root = doc.getDocumentElement();
            // Get the list of stations and iterate through them
            NodeList stationList = root.getElementsByTagName("station");
            for (int i = 0; i < stationList.getLength(); i++) {
                Element station = (Element) stationList.item(i);
                // Get the station name and check if it's one of the three stations
                String name = station.getElementsByTagName("name").item(0).getTextContent();
                if (name.equals("Tallinn-Harku") || name.equals("Tartu-Tõravere") || name.equals("Pärnu")) {
                    // Get the weather parameters and save them to the database
                    String wmocode = station.getElementsByTagName("wmocode").item(0).getTextContent();
                    String airtemperature = station.getElementsByTagName("airtemperature").item(0).getTextContent();
                    String phenomenon = station.getElementsByTagName("phenomenon").item(0).getTextContent();
                    String windspeed = station.getElementsByTagName("windspeed").item(0).getTextContent();
                    Timestamp timestamp = Timestamp.from(Instant.now());
                    Weather weather = new Weather(name, wmocode, Double.parseDouble(airtemperature), Double.parseDouble(windspeed), phenomenon, timestamp);
                    weatherRepository.save(weather);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
