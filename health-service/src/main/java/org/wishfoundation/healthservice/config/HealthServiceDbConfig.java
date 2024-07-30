package org.wishfoundation.healthservice.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import com.zaxxer.hikari.HikariDataSource;

import jakarta.persistence.EntityManagerFactory;

/**
 * Configuration class for the Health Service Database.
 * This class sets up the data source, entity manager factory, transaction manager,
 * and other necessary components for database operations.
 */
@Configuration
@EnableJpaRepositories(basePackages = {
		HealthServiceDbConfig.HEALTH_SERVICE_ENTITY_PACKAGE}, entityManagerFactoryRef = HealthServiceDbConfig.HEALTH_SERVICE_ENTITY_MANAGER_FACTORY, transactionManagerRef = HealthServiceDbConfig.HEALTH_SERVICE_TRANSACTION_MANAGER)
public class HealthServiceDbConfig {

	public static final String HEALTH_SERVICE_ENTITY_PACKAGE = "org.wishfoundation.healthservice.entity";
	public static final String HEALTH_SERVICE_DATA_SOURCE = "healthServiceDataSource";
	private static final String POOL_NAME = "healthServiceDBPool";
	public static final String HEALTH_SERVICE_ENTITY_MANAGER_FACTORY = "healthServiceEntityManagerFactory";
	public static final String HEALTH_SERVICE_TRANSACTION_MANAGER = "healthServiceTransactionManager";

	private ApplicationProperties applicationProperties;

    /**
     * Constructor for HealthServiceDbConfig.
     *
     * @param applicationProperties The application properties for database configuration.
     */
	public HealthServiceDbConfig(ApplicationProperties applicationProperties) {
		super();
		this.applicationProperties = applicationProperties;
	}

    /**
     * Creates and configures a HikariCP data source for the Health Service Database.
     *
     * @return The configured HikariCP data source.
     */
	@Bean(name = HEALTH_SERVICE_DATA_SOURCE)
	public DataSource healthServiceDataSource() {

		HikariDataSource hikariDataSource = new HikariDataSource();
		hikariDataSource.setUsername(applicationProperties.getDatabase().getUsername());
		hikariDataSource.setPassword(applicationProperties.getDatabase().getPassword());
		hikariDataSource.setJdbcUrl(applicationProperties.getDatabase().getUrl());
		hikariDataSource.setDriverClassName(applicationProperties.getDatabase().getDriverClassName());
		hikariDataSource.setPoolName(POOL_NAME);

		// hikariDataSource.setMaximumPoolSize(100);
		hikariDataSource.setMinimumIdle(20);
		hikariDataSource.setConnectionTimeout(120000);
		hikariDataSource.setIdleTimeout(180000);
		hikariDataSource.setMaxLifetime(300000);
		return hikariDataSource;
	}

    /**
     * Creates and configures a LocalContainerEntityManagerFactoryBean for the Health Service Database.
     *
     * @param healthServiceDataSource The data source for the Health Service Database.
     * @return The configured LocalContainerEntityManagerFactoryBean.
     */
	@Primary
	@Bean(name = HEALTH_SERVICE_ENTITY_MANAGER_FACTORY)
	public LocalContainerEntityManagerFactoryBean healthServiceEntityManagerFactory(
			@Qualifier(HEALTH_SERVICE_DATA_SOURCE) DataSource healthServiceDataSource) {
		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(healthServiceDataSource);
		em.setPackagesToScan(HEALTH_SERVICE_ENTITY_PACKAGE);
		em.setPersistenceUnitName("health-service-db-persistence-unit");
		JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		em.setJpaVendorAdapter(vendorAdapter);
		em.setJpaProperties(hibernateProperties());

		return em;
	}

    /**
     * Creates and configures a JpaTransactionManager for the Health Service Database.
     *
     * @param emf The LocalContainerEntityManagerFactoryBean for the Health Service Database.
     * @return The configured JpaTransactionManager.
     */
	@Primary
	@Bean(name = HEALTH_SERVICE_TRANSACTION_MANAGER)
	public JpaTransactionManager healthServiceTransactionManager(
			@Qualifier(HEALTH_SERVICE_ENTITY_MANAGER_FACTORY) LocalContainerEntityManagerFactoryBean emf) {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(emf.getObject());
		return transactionManager;
	}

    /**
     * Creates a PersistenceExceptionTranslationPostProcessor for the Health Service Database.
     *
     * @return The configured PersistenceExceptionTranslationPostProcessor.
     */
	@Bean
	public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
		return new PersistenceExceptionTranslationPostProcessor();
	}

    /**
     * Creates a Properties object with Hibernate configuration properties.
     *
     * @return The configured Properties object.
     */
	private Properties hibernateProperties() {
		Properties properties = new Properties();

		properties.put(org.hibernate.cfg.Environment.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
		properties.put(org.hibernate.cfg.Environment.HBM2DDL_AUTO, "none");
		properties.put(org.hibernate.cfg.Environment.SHOW_SQL, true);
		properties.put(org.hibernate.cfg.Environment.DEFAULT_SCHEMA, "public");
		properties.put(org.hibernate.cfg.Environment.JDBC_TIME_ZONE, "UTC");
		properties.put(org.hibernate.cfg.Environment.ENABLE_LAZY_LOAD_NO_TRANS, true);

		return properties;
	}

    /**
     * Creates a SessionFactory from the EntityManagerFactory for the Health Service Database.
     *
     * @param entityManagerFactory The EntityManagerFactory for the Health Service Database.
     * @return The configured SessionFactory.
     */
	@Bean
	public SessionFactory sessionFactory(
			@Qualifier(HEALTH_SERVICE_ENTITY_MANAGER_FACTORY) EntityManagerFactory entityManagerFactory) {

		return entityManagerFactory.unwrap(SessionFactory.class);

	}
}
