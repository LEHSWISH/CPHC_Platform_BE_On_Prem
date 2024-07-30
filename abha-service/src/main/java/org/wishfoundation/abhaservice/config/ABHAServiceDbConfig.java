package org.wishfoundation.abhaservice.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
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

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Configuration class for ABHA Service Database.
 * This class sets up the DataSource, EntityManagerFactory, TransactionManager, and SessionFactory for the ABHA Service Database.
 * It also includes a method to create Hibernate properties.
 *
 * @author Sandeep Kumar
 */
@Configuration
@EnableJpaRepositories(basePackages = {
        ABHAServiceDbConfig.ABHA_SERVICE_ENTITY_PACKAGE}, entityManagerFactoryRef = ABHAServiceDbConfig.ABHA_SERVICE_ENTITY_MANAGER_FACTORY, transactionManagerRef = ABHAServiceDbConfig.ABHA_SERVICE_TRANSACTION_MANAGER)
public class ABHAServiceDbConfig {
    public static final String ABHA_SERVICE_ENTITY_PACKAGE = "org.wishfoundation.abhaservice.entity";
    public static final String ABHA_SERVICE_DATA_SOURCE = "ABHAServiceDataSource";
    private static final String POOL_NAME = "ABHAServiceDBPool";
    public static final String ABHA_SERVICE_ENTITY_MANAGER_FACTORY = "ABHAServiceEntityManagerFactory";
    public static final String ABHA_SERVICE_TRANSACTION_MANAGER = "ABHAServiceTransactionManager";

    private ApplicationProperties applicationProperties;

    /**
     * Constructor for ABHAServiceDbConfig.
     *
     * @param applicationProperties The application properties for database configuration.
     */
    public ABHAServiceDbConfig(ApplicationProperties applicationProperties) {
        super();
        this.applicationProperties = applicationProperties;
    }

    /**
     * Creates a DataSource for the ABHA Service Database.
     *
     * @return The DataSource for the ABHA Service Database.
     */
    @Bean(name = ABHA_SERVICE_DATA_SOURCE)
    public DataSource ABHAServiceDataSource() {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setUsername(applicationProperties.getDatabase().getUsername());
        hikariDataSource.setPassword(applicationProperties.getDatabase().getPassword());
        hikariDataSource.setJdbcUrl(applicationProperties.getDatabase().getUrl());
        hikariDataSource.setDriverClassName(applicationProperties.getDatabase().getDriverClassName());
        hikariDataSource.setPoolName(POOL_NAME);
        hikariDataSource.setMaximumPoolSize(100);
        hikariDataSource.setMinimumIdle(20);
        hikariDataSource.setConnectionTimeout(120000);
        hikariDataSource.setIdleTimeout(180000);
        hikariDataSource.setMaxLifetime(300000);
        return hikariDataSource;
    }

    /**
     * Creates an EntityManagerFactory for the ABHA Service Database.
     *
     * @param ABHAServiceDataSource The DataSource for the ABHA Service Database.
     * @return The EntityManagerFactory for the ABHA Service Database.
     */
    @Primary
    @Bean(name = ABHA_SERVICE_ENTITY_MANAGER_FACTORY)
    public LocalContainerEntityManagerFactoryBean ABHAServiceEntityManagerFactory(
            @Qualifier(ABHA_SERVICE_DATA_SOURCE) DataSource ABHAServiceDataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(ABHAServiceDataSource);
        em.setPackagesToScan(ABHA_SERVICE_ENTITY_PACKAGE);
        em.setPersistenceUnitName("ABHA-service-db-persistence-unit");
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(hibernateProperties());
        return em;
    }

    /**
     * Creates a JpaTransactionManager for the ABHA Service Database.
     *
     * @param emf The EntityManagerFactory for the ABHA Service Database.
     * @return The JpaTransactionManager for the ABHA Service Database.
     */
    @Primary
    @Bean(name = ABHA_SERVICE_TRANSACTION_MANAGER)
    public JpaTransactionManager ABHAServiceTransactionManager(
            @Qualifier(ABHA_SERVICE_ENTITY_MANAGER_FACTORY) LocalContainerEntityManagerFactoryBean emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf.getObject());
        return transactionManager;
    }

    /**
     * Creates a PersistenceExceptionTranslationPostProcessor for the ABHA Service Database.
     *
     * @return The PersistenceExceptionTranslationPostProcessor for the ABHA Service Database.
     */
    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    /**
     * Creates Hibernate properties for the ABHA Service Database.
     *
     * @return The Hibernate properties for the ABHA Service Database.
     */
    private Properties hibernateProperties() {
        Properties properties = new Properties();
        properties.put(org.hibernate.cfg.Environment.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
        properties.put(org.hibernate.cfg.Environment.HBM2DDL_AUTO, "update");
        properties.put(org.hibernate.cfg.Environment.SHOW_SQL, true);
        properties.put(org.hibernate.cfg.Environment.DEFAULT_SCHEMA, "public");
        properties.put(org.hibernate.cfg.Environment.JDBC_TIME_ZONE, "UTC");
        properties.put(org.hibernate.cfg.Environment.ENABLE_LAZY_LOAD_NO_TRANS, true);
        return properties;
    }

    /**
     * Creates a SessionFactory for the ABHA Service Database.
     *
     * @param entityManagerFactory The EntityManagerFactory for the ABHA Service Database.
     * @return The SessionFactory for the ABHA Service Database.
     */
    @Bean
    public SessionFactory sessionFactory(
            @Qualifier(ABHA_SERVICE_ENTITY_MANAGER_FACTORY) EntityManagerFactory entityManagerFactory) {
        return entityManagerFactory.unwrap(SessionFactory.class);
    }
}
