package org.wishfoundation.abhaservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties
@Configuration
@Getter
@Setter
@ConfigurationProperties("")
public class ApplicationProperties {
	private GlobalDataSource database;
	
	
}
