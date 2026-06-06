package com.guarani.pos.tenant;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

@Configuration
@EnableConfigurationProperties(TenantRoutingProperties.class)
public class TenancyConfiguration {

    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties sharedDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties sharedDataSourceProperties,
                                 TenantRoutingProperties tenantRoutingProperties) {
        DataSource sharedDataSource = sharedDataSourceProperties.initializeDataSourceBuilder().build();

        Map<Object, Object> targetDataSources = new LinkedHashMap<>();
        targetDataSources.put(tenantRoutingProperties.getSharedDatasourceKey(), sharedDataSource);

        tenantRoutingProperties.getDatasources().forEach((key, config) -> {
            DataSource dedicatedDataSource = org.springframework.boot.jdbc.DataSourceBuilder.create()
                    .driverClassName(config.getDriverClassName())
                    .url(config.getUrl())
                    .username(config.getUsername())
                    .password(config.getPassword())
                    .build();
            targetDataSources.put(key, dedicatedDataSource);
        });

        AbstractRoutingDataSource routingDataSource =
                new TenantRoutingDataSource(tenantRoutingProperties.getSharedDatasourceKey());
        routingDataSource.setDefaultTargetDataSource(sharedDataSource);
        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.afterPropertiesSet();
        return routingDataSource;
    }
}
