package com.guarani.pos.tenant;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.tenancy")
public class TenantRoutingProperties {

    private String sharedDatasourceKey = "shared";
    private Map<String, DedicatedTenantProperties> dedicated = new LinkedHashMap<>();
    private Map<String, DatasourceProperties> datasources = new LinkedHashMap<>();

    public String getSharedDatasourceKey() {
        return sharedDatasourceKey;
    }

    public void setSharedDatasourceKey(String sharedDatasourceKey) {
        this.sharedDatasourceKey = sharedDatasourceKey;
    }

    public Map<String, DedicatedTenantProperties> getDedicated() {
        return dedicated;
    }

    public void setDedicated(Map<String, DedicatedTenantProperties> dedicated) {
        this.dedicated = dedicated;
    }

    public Map<String, DatasourceProperties> getDatasources() {
        return datasources;
    }

    public void setDatasources(Map<String, DatasourceProperties> datasources) {
        this.datasources = datasources;
    }

    public static class DedicatedTenantProperties {
        private Long companyId;
        private String datasourceKey;
        private boolean enabled = true;

        public Long getCompanyId() {
            return companyId;
        }

        public void setCompanyId(Long companyId) {
            this.companyId = companyId;
        }

        public String getDatasourceKey() {
            return datasourceKey;
        }

        public void setDatasourceKey(String datasourceKey) {
            this.datasourceKey = datasourceKey;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class DatasourceProperties {
        private String url;
        private String username;
        private String password;
        private String driverClassName;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getDriverClassName() {
            return driverClassName;
        }

        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
        }
    }
}
