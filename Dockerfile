FROM openjdk:17-jdk-alpine
MAINTAINER baeldung.com
COPY DeliveryApiApplication.jar DeliveryApiApplication.jar
ENTRYPOINT ["java","-jar","/DeliveryApiApplication.jar"]