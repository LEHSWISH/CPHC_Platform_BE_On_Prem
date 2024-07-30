package org.wishfoundation.chardhamcore.config;

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
 * Configuration class for CharDhamCore database.
 * This class sets up the data source, entity manager factory, transaction manager,
 * and other necessary components for database operations.
 *
 * @author Sandeep Kumar
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = {
        CharDhamCoreDbConfig.CHAR_DHAM_CORE_ENTITY_PACKAGE}, entityManagerFactoryRef = CharDhamCoreDbConfig.CHAR_DHAM_CORE_ENTITY_MANAGER_FACTORY, transactionManagerRef = CharDhamCoreDbConfig.CHAR_DHAM_CORE_TRANSACTION_MANAGER)
public class CharDhamCoreDbConfig {

    public static final String CHAR_DHAM_CORE_ENTITY_PACKAGE = "org.wishfoundation.chardhamcore.entity";
    public static final String CHAR_DHAM_CORE_DATA_SOURCE = "charDhamCoreDataSource";
    private static final String POOL_NAME = "charDhamUserServiceDBPool";

    public static final String CHAR_DHAM_CORE_ENTITY_MANAGER_FACTORY = "charDhamEntityManagerFactory";
    public static final String CHAR_DHAM_CORE_TRANSACTION_MANAGER = "charDhamCoreTransactionManager";

    private CharDhamCoreApplicationProperties applicationProperties;

    /**
     * Constructor for CharDhamCoreDbConfig.
     *
     * @param applicationProperties The application properties for database configuration.
     */
    public CharDhamCoreDbConfig(CharDhamCoreApplicationProperties applicationProperties) {
        super();
        this.applicationProperties = applicationProperties;
    }

    /**
     * Method to create a data source for CharDhamCore database.
     *
     * @return The configured data source.
     */
    @Bean(name = CHAR_DHAM_CORE_DATA_SOURCE)
    public DataSource charDhamCoreDataSource() {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setUsername(applicationProperties.getUserDatabase().getUsername());
        hikariDataSource.setPassword(applicationProperties.getUserDatabase().getPassword());
        hikariDataSource.setJdbcUrl(applicationProperties.getUserDatabase().getUrl());
        hikariDataSource.setDriverClassName(applicationProperties.getUserDatabase().getDriverClassName());
        hikariDataSource.setPoolName(POOL_NAME);

        hikariDataSource.setMaximumPoolSize(100);
        hikariDataSource.setMinimumIdle(10);
        hikariDataSource.setConnectionTimeout(120000);
        hikariDataSource.setIdleTimeout(180000);
        hikariDataSource.setMaxLifetime(300000);
        return hikariDataSource;
    }

    /**
     * Method to create an entity manager factory for CharDhamCore database.
     *
     * @param charDhamCoreDataSource The data source for the database.
     * @return The configured entity manager factory.
     */
    @Primary
    @Bean(name = CHAR_DHAM_CORE_ENTITY_MANAGER_FACTORY)
    public LocalContainerEntityManagerFactoryBean charDhamCoreEntityManagerFactory(
            @Qualifier(CHAR_DHAM_CORE_DATA_SOURCE) DataSource charDhamCoreDataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(charDhamCoreDataSource);
        em.setPackagesToScan(CHAR_DHAM_CORE_ENTITY_PACKAGE);
        em.setPersistenceUnitName("char-dham-core-db-persistence-unit");
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(hibernateProperties());

        return em;
    }

    /**
     * Method to create a transaction manager for CharDhamCore database.
     *
     * @param emf The entity manager factory for the database.
     * @return The configured transaction manager.
     */
    @Primary
    @Bean(name = CHAR_DHAM_CORE_TRANSACTION_MANAGER)
    public JpaTransactionManager charDhamCoreTransactionManager(
            @Qualifier(CHAR_DHAM_CORE_ENTITY_MANAGER_FACTORY) LocalContainerEntityManagerFactoryBean emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf.getObject());
        return transactionManager;
    }

    /**
     * Method to create a persistence exception translation post processor.
     *
     * @return The configured persistence exception translation post processor.
     */
    @Bean
    public PersistenceExceptionTranslationPostProcessor charDhamExceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    /**
     * Method to create Hibernate properties for the database configuration.
     *
     * @return The configured Hibernate properties.
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
     * Method to create a SessionFactory for CharDhamCore database.
     *
     * @param entityManagerFactory The entity manager factory for the database.
     * @return The configured SessionFactory.
     */
    @Bean
    public SessionFactory charDhamSessionFactory(
            @Qualifier(CHAR_DHAM_CORE_ENTITY_MANAGER_FACTORY) EntityManagerFactory entityManagerFactory) {
        return entityManagerFactory.unwrap(SessionFactory.class);
    }
}
