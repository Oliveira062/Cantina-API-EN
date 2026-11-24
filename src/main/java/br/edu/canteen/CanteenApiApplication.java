package br.edu.canteen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CanteenApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CanteenApiApplication.class, args);
    }
}
