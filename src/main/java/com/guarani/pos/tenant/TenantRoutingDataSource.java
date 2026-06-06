package com.guarani.pos.tenant;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class TenantRoutingDataSource extends AbstractRoutingDataSource {

    private final String defaultDatasourceKey;

    public TenantRoutingDataSource(String defaultDatasourceKey) {
        this.defaultDatasourceKey = defaultDatasourceKey;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String current = TenantDataSourceContext.getCurrentDatasourceKey();
        if (current == null || current.isBlank()) {
            return defaultDatasourceKey;
        }
        return current;
    }
}
