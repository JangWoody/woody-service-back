package com.restapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = "com.restapi") 
public class RestApiTest02Application {
    
    private static final String DEFAULT_HOLIDAY_STATUS = "N";
    private static volatile String holiday = DEFAULT_HOLIDAY_STATUS;

    public static void main(String[] args) {
        SpringApplication.run(RestApiTest02Application.class, args);
    }

    public static String getHoliday() {
        return holiday;
    }

    public static void setHoliday(String holidayStatus) {
        holiday = holidayStatus;
    }
}