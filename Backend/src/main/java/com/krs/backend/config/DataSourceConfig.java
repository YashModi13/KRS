package com.krs.backend.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DataSourceConfig {

    @Value("${islocaldbconnect:false}")
    private boolean isLocalDbConnect;

    @Value("${db.local.url:jdbc:postgresql://localhost:5432/krs_db?currentSchema=krs_schema}")
    private String localUrl;

    @Value("${db.local.username:krd_postgress}")
    private String localUsername;

    @Value("${db.local.password:Krs@Prod!Db#2026}")
    private String localPassword;

    @Value("${db.neon.url:jdbc:postgresql://ep-young-rain-b3ynvfjr-pooler.c-4.ap-southeast-1.aws.neon.tech:5432/krs_db?currentSchema=krs_schema&sslmode=require}")
    private String neonUrl;

    @Value("${db.neon.username:neondb_owner}")
    private String neonUsername;

    @Value("${db.neon.password:npg_Su0eYVn8JxAG}")
    private String neonPassword;

    @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
    private String driverClassName;

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public HikariDataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(driverClassName);
        if (isLocalDbConnect) {
            dataSource.setJdbcUrl(localUrl);
            dataSource.setUsername(localUsername);
            dataSource.setPassword(localPassword);
        } else {
            dataSource.setJdbcUrl(neonUrl);
            dataSource.setUsername(neonUsername);
            dataSource.setPassword(neonPassword);
        }
        return dataSource;
    }
}
