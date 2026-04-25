package com.rajashekar.familyleague;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FamilyLeagueApplication {

    public static void main(String[] args) {
        SpringApplication.run(FamilyLeagueApplication.class, args);
    }
}
