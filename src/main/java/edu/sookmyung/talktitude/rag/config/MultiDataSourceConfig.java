package edu.sookmyung.talktitude.rag.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class MultiDataSourceConfig {

    // ===== 기본(MySQL) =====
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource dataSource() { // 이름을 dataSource로 두면 JPA가 자동 인식
        return primaryDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    // ===== RAG(Postgres) =====
    @Bean
    @ConfigurationProperties("spring.datasource-rag")
    public DataSourceProperties ragDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "ragDataSource")
    public DataSource ragDataSource() {
        return ragDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "ragJdbcTemplate")
    public org.springframework.jdbc.core.JdbcTemplate ragJdbcTemplate(
            @Qualifier("ragDataSource") DataSource ds) throws java.sql.SQLException {

        try (var c = ds.getConnection()) {
            com.pgvector.PGvector.addVectorType(c);
        }
        return new org.springframework.jdbc.core.JdbcTemplate(ds);
    }
}