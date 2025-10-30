package ru.sber.cb.ekp.avtokflekp.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
//@PropertySources({
//        @PropertySource("classpath:datasources.properties"),
//                @PropertySource("classpath:secret.properties")
//})
@EntityScan(
        basePackages = "ru.sber.cb.ekp.avtokflekp.subsystem.loans")
@EnableJpaRepositories(
        basePackages = "ru.sber.cb.ekp.avtokflekp.subsystem.loans.repository",
        entityManagerFactoryRef = "loansEntityManager",
        transactionManagerRef = "loansTransactionManager")
public class PersistenceLoansAutoConfiguration {


//    @Autowired
//    private Environment env;
//
//    public PersistenceLoansAutoConfiguration() {
//        super();
//    }

    @Bean
    public DataSource loansDataSource() {
        System.out.println("=== Creating DataSource with explicit configuration ===");

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:postgresql://localhost:5433/postgres");
        dataSource.setUsername("user");
        dataSource.setPassword("123");
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(2);
        dataSource.setConnectionTimeout(30000);
        dataSource.setIdleTimeout(300000);
        dataSource.setMaxLifetime(1200000);

        System.out.println("=== DataSource configured ===");
        System.out.println("URL: " + dataSource.getJdbcUrl());
        System.out.println("Username: " + dataSource.getUsername());

        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean loansEntityManager() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(loansDataSource());
        em.setPackagesToScan("ru.sber.cb.ekp.avtokflekp.subsystem.loans");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "validate");
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.show_sql", "true");

        em.setJpaPropertyMap(properties);
        return em;
    }

    @Bean
    public PlatformTransactionManager loansTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(loansEntityManager().getObject());
        return transactionManager;
    }

}
