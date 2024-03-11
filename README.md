# Delivery Application

This is a Spring Boot project written in Java. The project uses Gradle as a build tool.
This project is for internship trial work.

## Project Description
The goal of this project is to create a RESTful API.
The API should be able to handle the following:
1) Calculate the delivery fee based on chosen city and vehicle type.
2) Calculate the delivery time based on chosen city and vehicle type and chosen timestamp.
3) Let the user change regional base fees.
4) Let the user change extra fees for (air temperature, wind speed and weather phenomenons).
5) Let the user add extra fees.
6) Let the user delete extra fees.
7) Display error messages.

For integrity, the user will not be able to delete or add regional base fees, unless they change the code itself too.
The user who wants to change, add or delete fees should be aware of the data in database to avoid any logical errors.

Cronjob is used to update the weather data every 60 minutes, 15 minutes past the hour. The time can be changed in the application.properties file.

There is also a simple web interface for the API. The web interface is written in Vue.js and can be found in the following repository: [Delivery API Web Interface](https://github.com/Elisabethein/Delivery_application)
The web interface is not required for the API to work, but it is a nice addition to the project. The user can choose a city and vehicle to get the delivery fee.

## Project Structure

The project consists of the following packages:
* controllers - contains the RESTful API endpoints
* repositories - contains the JPA repositories
* services - contains the business logic
* entities - contains the JPA entities
* configurations - contains the configuration classes
* components - contains the data initialization classes

The controller, which takes requests from path /api, consists of the following endpoints:
* /calculateRbf - calculate the delivery fee based on chosen city and vehicle type
* 


The project also has a test class, which tests the business logic and error handling.
## Running the project

To run the project, you need to have Java 17 and Gradle installed on your machine.
The project can be started by running the DeliveryApiApplication class or by running the following command in the terminal:
```./gradlew bootRun```

* The application will start on port 8080.
* The database is an in-memory H2 database, which can be accessed at the following URL: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
* The username is "sa" and the password is empty.

## API Documentation

The controller, which takes requests from path /api, consists of the following endpoints:

* ##### /calculateRbf 
  - calculate the delivery fee based on chosen city and vehicle type
  - method: GET
  - parameters: city, vehicle, datetime (optional)
  - returns: delivery fee

* ##### /changeBaseFeeRules/{forWhichCity}/{forWhichVehicle}/{fee}

  - change regional base fees for chosen city and vehicle type
  - method: PUT
  - parameters: forWhichCity, forWhichVehicle, fee
  - returns: message if the fee was changed or not

* ##### /changeExtraFeeRules/{table}/{oldValue}/{newValue}/{fee} 
  - change extra fees for chosen table, add which value to change, the new value and the fee
  - method: PUT
  - parameters: table, oldValue, newValue, fee
  - returns: message if the fee was changed or not

* ##### /addExtraFeeRules/{table}/{value}/{fee} 
  - add extra fees for chosen table, value and the fee
  - method: POST
  - parameters: table, value, fee
  - returns: message if the fee was added or not

* ##### /deleteExtraFeeRules/{table}/{value}
  - delete extra fees for chosen table and value
  - method: DELETE
  - parameters: table, value
  - returns: message if the fee was deleted or not
