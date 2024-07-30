package org.wishfoundation.iomtdeviceinventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "org.wishfoundation")
public class IoMtDeviceInventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(IoMtDeviceInventoryApplication.class, args);
    }

}
