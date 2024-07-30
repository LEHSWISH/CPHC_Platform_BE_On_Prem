package org.wishfoundation.superadmin.config;

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
 * Configuration class for the Super Admin Database.
 * This class sets up the DataSource, EntityManagerFactory, TransactionManager, and SessionFactory for the Super Admin Database.
 * It also includes the necessary properties for Hibernate.
 *
 * @author Sandeep Kumar
 */

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = {
        SuperAdminDbConfig.SUPER_ADMIN_ENTITY_PACKAGE}, entityManagerFactoryRef = SuperAdminDbConfig.SUPER_ADMIN_ENTITY_MANAGER_FACTORY, transactionManagerRef = SuperAdminDbConfig.SUPER_ADMIN_TRANSACTION_MANAGER)
public class SuperAdminDbConfig {

    public static final String SUPER_ADMIN_ENTITY_PACKAGE = "org.wishfoundation.superadmin.entity";
    public static final String SUPER_ADMIN_DATA_SOURCE = "userServiceDataSource";
    private static final String POOL_NAME = "superAdminDBPool";
    public static final String SUPER_ADMIN_ENTITY_MANAGER_FACTORY = "superAdminEntityManagerFactory";
    public static final String SUPER_ADMIN_TRANSACTION_MANAGER = "superAdminTransactionManager";

    private ApplicationProperties applicationProperties;

    /**
     * Constructor for SuperAdminDbConfig.
     *
     * @param applicationProperties The application properties for database configuration.
     */
    public SuperAdminDbConfig(ApplicationProperties applicationProperties) {
        super();
        this.applicationProperties = applicationProperties;
    }


    /**
     * Creates and configures a DataSource for the Super Admin Database.
     *
     * @return The configured DataSource.
     */
    @Bean(name = SUPER_ADMIN_DATA_SOURCE)
    public DataSource iamDataSource() {

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
     * Creates and configures a LocalContainerEntityManagerFactoryBean for the Super Admin Database.
     *
     * @param iamDataSource The DataSource for the Super Admin Database.
     * @return The configured LocalContainerEntityManagerFactoryBean.
     */
    @Primary
    @Bean(name = SUPER_ADMIN_ENTITY_MANAGER_FACTORY)
    public LocalContainerEntityManagerFactoryBean superAdminEntityManagerFactory(
            @Qualifier(SUPER_ADMIN_DATA_SOURCE) DataSource iamDataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(iamDataSource);
        em.setPackagesToScan(SUPER_ADMIN_ENTITY_PACKAGE);
        em.setPersistenceUnitName("super-admin-db-persistence-unit");
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(hibernateProperties());

        return em;
    }

    /**
     * Creates and configures a JpaTransactionManager for the Super Admin Database.
     *
     * @param emf The LocalContainerEntityManagerFactoryBean for the Super Admin Database.
     * @return The configured JpaTransactionManager.
     */
    @Primary
    @Bean(name = SUPER_ADMIN_TRANSACTION_MANAGER)
    public JpaTransactionManager superAdminTransactionManager(
            @Qualifier(SUPER_ADMIN_ENTITY_MANAGER_FACTORY) LocalContainerEntityManagerFactoryBean emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf.getObject());
        return transactionManager;
    }

    /**
     * Creates a PersistenceExceptionTranslationPostProcessor for the Super Admin Database.
     *
     * @return The configured PersistenceExceptionTranslationPostProcessor.
     */
    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    /**
     * Creates and configures Hibernate properties for the Super Admin Database.
     *
     * @return The configured Hibernate properties.
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
     * Creates and configures a SessionFactory for the Super Admin Database.
     *
     * @param entityManagerFactory The EntityManagerFactory for the Super Admin Database.
     * @return The configured SessionFactory.
     */
    @Bean
    public SessionFactory sessionFactory(
            @Qualifier(SUPER_ADMIN_ENTITY_MANAGER_FACTORY) EntityManagerFactory entityManagerFactory) {

        return entityManagerFactory.unwrap(SessionFactory.class);

    }
}
