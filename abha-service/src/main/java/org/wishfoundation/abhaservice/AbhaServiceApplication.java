package org.wishfoundation.abhaservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "org.wishfoundation")
public class AbhaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AbhaServiceApplication.class, args);
    }

}
