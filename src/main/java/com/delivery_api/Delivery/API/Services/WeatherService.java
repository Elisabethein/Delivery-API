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
    private static final String URL = "https://www.ilmateenistus.ee/ilma_andmed/xml/observations.php";

    @Autowired
    public WeatherService(WeatherRepository weatherRepository, AtefRepository atefRepository, WsefRepository wsefRepository, WpefRepository wpefRepository, RbfRepository rbfRepository, RestTemplate restTemplate) {
        this.weatherRepository = weatherRepository;
        this.atefRepository = atefRepository;
        this.wsefRepository = wsefRepository;
        this.wpefRepository = wpefRepository;
        this.rbfRepository = rbfRepository;
        this.restTemplate = restTemplate;
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
        double rbf = getBaseRbf(city, vehicleType);
        if (rbf == -1) {
            throw new IllegalArgumentException("Invalid vehicle type");
        }

        // Use assistant method to calculate the extra fees based on temperature and weather phenomenon
        if (vehicleType.equals("Scooter") || vehicleType.equals("Bike")) {
            rbf = getExtraFeesForBikeOrScooter(temperature, rbf, weatherPhenomenon);
        }

        // Use assistant method to calculate the extra fees based on wind speed
        if (vehicleType.equals("Bike")) {
            rbf = getExtraFeesForBike(windSpeed, rbf);
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
    private double getExtraFeesForBike(double windSpeed, double rbf) {
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
     * Get extra fees for bike or scooter based on temperature and weather phenomenon
     *
     * @param temperature       - current temperature
     * @param rbf               - current RBF value
     * @param weatherPhenomenon - current weather phenomenon
     * @return RBF value with extra fees
     */
    private double getExtraFeesForBikeOrScooter(double temperature, double rbf, String weatherPhenomenon) {
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
     * Get the base fee for the given city and vehicle type
     *
     * @param city        - city name
     * @param vehicleType - vehicle type
     * @return base regional fee value
     */
    private double getBaseRbf(String city, String vehicleType) {
        // Create a list for fees in one city and search for the vehicle type
        List<Rbf> rbfList = rbfRepository.findByCity(city);
        Optional<Rbf> rbfOptional = rbfList.stream()
                .filter(rbf -> rbf.getVehicle().equals(vehicleType))
                .findFirst();
        return rbfOptional.map(Rbf::getFee).orElse(-1.0);
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
     * @param value - value
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
                }
                else {
                    throw new IllegalArgumentException("Value not found");
                }
            case "Wsef":
                Wsef wsef = wsefRepository.findByBorders(value);
                if (wsef != null) {
                    wsefRepository.delete(wsef);
                }
                else {
                    throw new IllegalArgumentException("Value not found");
                }
            case "Wpef":
                Wpef wpef = wpefRepository.findByContaining(value);
                if (wpef != null) {
                    wpefRepository.delete(wpef);
                }
                else {
                    throw new IllegalArgumentException("Value not found");
                }
            default:
                throw new IllegalArgumentException("Invalid table name");
        }
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
