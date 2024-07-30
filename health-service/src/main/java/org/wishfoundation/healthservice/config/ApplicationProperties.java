package org.wishfoundation.healthservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

/**
 * This class represents the application's global configuration properties.
 * It uses Spring Boot's {@code @ConfigurationProperties} annotation to bind properties from the application's configuration file.
 * The properties are then automatically injected into the fields of this class.
 *
 */
@EnableConfigurationProperties
@Configuration
@Getter
@Setter
@ConfigurationProperties("")
public class ApplicationProperties {
    /**
     * The global data source configuration.
     * This property is bound to the 'database' section in the application's configuration file.
     */
	private GlobalDataSource database;
	
	
}
