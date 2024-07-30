package org.wishfoundation.userservice.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GlobalDataSource {
	private String password;
	private String username;
	private String host;
	private String awsRegion;

	private final String dbConnectionPrefix = "jdbc:postgresql://";
	private final String driverClassName = "org.postgresql.Driver";

	public String getUrl() {
		return dbConnectionPrefix + host;

	}

}
