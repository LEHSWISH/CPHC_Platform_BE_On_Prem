package org.wishfoundation.userservice.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Configuration class for the User Service Database.
 * This class sets up the data source, entity manager factory, transaction manager,
 * and other necessary components for JPA with Hibernate.
 *
 * @author Sandeep Kumar
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = {
        UserServiceDbConfig.USER_SERVICE_ENTITY_PACKAGE}, entityManagerFactoryRef = UserServiceDbConfig.USER_SERVICE_ENTITY_MANAGER_FACTORY, transactionManagerRef = UserServiceDbConfig.USER_SERVICE_TRANSACTION_MANAGER)
public class UserServiceDbConfig {

    public static final String USER_SERVICE_ENTITY_PACKAGE = "org.wishfoundation.userservice.entity";
    public static final String USER_SERVICE_DATA_SOURCE = "userServiceDataSource";
    private static final String POOL_NAME = "userServiceDBPool";
    public static final String USER_SERVICE_ENTITY_MANAGER_FACTORY = "userServiceEntityManagerFactory";
    public static final String USER_SERVICE_TRANSACTION_MANAGER = "userServiceTransactionManager";

    private ApplicationProperties applicationProperties;

    /**
     * Constructor for UserServiceDbConfig.
     *
     * @param applicationProperties The application properties for database configuration.
     */
    public UserServiceDbConfig(ApplicationProperties applicationProperties) {
        super();
        this.applicationProperties = applicationProperties;
    }

    /**
     * Creates and configures a HikariCP data source for the User Service Database.
     *
     * @return The configured HikariCP data source.
     */
    @Bean(name = USER_SERVICE_DATA_SOURCE)
    public DataSource userServiceDataSource() {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setUsername(applicationProperties.getDatabase().getUsername());
        hikariDataSource.setPassword(applicationProperties.getDatabase().getPassword());
        hikariDataSource.setJdbcUrl(applicationProperties.getDatabase().getUrl());
        hikariDataSource.setDriverClassName(applicationProperties.getDatabase().getDriverClassName());
        hikariDataSource.setPoolName(POOL_NAME);

        hikariDataSource.setMaximumPoolSize(100);
        hikariDataSource.setMinimumIdle(10);
        hikariDataSource.setConnectionTimeout(120000);
        hikariDataSource.setIdleTimeout(180000);
        hikariDataSource.setMaxLifetime(300000);

        return hikariDataSource;
    }

    /**
     * Creates and configures a LocalContainerEntityManagerFactoryBean for the User Service Database.
     *
     * @param userServiceDataSource The data source for the User Service Database.
     * @return The configured LocalContainerEntityManagerFactoryBean.
     */
    @Primary
    @Bean(name = USER_SERVICE_ENTITY_MANAGER_FACTORY)
    public LocalContainerEntityManagerFactoryBean userServiceEntityManagerFactory(
            @Qualifier(USER_SERVICE_DATA_SOURCE) DataSource userServiceDataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(userServiceDataSource);
        em.setPackagesToScan(USER_SERVICE_ENTITY_PACKAGE);
        em.setPersistenceUnitName("user-service-db-persistence-unit");
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(hibernateProperties());

        return em;
    }

    /**
     * Creates and configures a JpaTransactionManager for the User Service Database.
     *
     * @param emf The LocalContainerEntityManagerFactoryBean for the User Service Database.
     * @return The configured JpaTransactionManager.
     */
    @Primary
    @Bean(name = USER_SERVICE_TRANSACTION_MANAGER)
    public JpaTransactionManager userServiceTransactionManager(
            @Qualifier(USER_SERVICE_ENTITY_MANAGER_FACTORY) LocalContainerEntityManagerFactoryBean emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf.getObject());
        return transactionManager;
    }

    /**
     * Creates a PersistenceExceptionTranslationPostProcessor for the User Service Database.
     *
     * @return The configured PersistenceExceptionTranslationPostProcessor.
     */
    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    /**
     * Creates and configures Hibernate properties for the User Service Database.
     *
     * @return The configured Hibernate properties.
     */
    private Properties hibernateProperties() {
        Properties properties = new Properties();

        properties.put(org.hibernate.cfg.Environment.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
        properties.put(org.hibernate.cfg.Environment.HBM2DDL_AUTO, "update");
        properties.put(org.hibernate.cfg.Environment.SHOW_SQL, false);
        properties.put(org.hibernate.cfg.Environment.DEFAULT_SCHEMA, "public");
        properties.put(org.hibernate.cfg.Environment.JDBC_TIME_ZONE, "UTC");
        properties.put(org.hibernate.cfg.Environment.ENABLE_LAZY_LOAD_NO_TRANS, true);

        return properties;
    }

    /**
     * Creates a SessionFactory for the User Service Database.
     *
     * @param entityManagerFactory The EntityManagerFactory for the User Service Database.
     * @return The configured SessionFactory.
     */
    @Bean
    public SessionFactory sessionFactory(
            @Qualifier(USER_SERVICE_ENTITY_MANAGER_FACTORY) EntityManagerFactory entityManagerFactory) {

        return entityManagerFactory.unwrap(SessionFactory.class);

    }
}
