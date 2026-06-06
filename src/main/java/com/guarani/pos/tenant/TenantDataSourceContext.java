package com.guarani.pos.tenant;

public final class TenantDataSourceContext {

    private static final ThreadLocal<String> CURRENT_DATASOURCE_KEY = new ThreadLocal<>();

    private TenantDataSourceContext() {
    }

    public static void setCurrentDatasourceKey(String datasourceKey) {
        if (datasourceKey == null || datasourceKey.trim().isEmpty()) {
            CURRENT_DATASOURCE_KEY.remove();
            return;
        }

        CURRENT_DATASOURCE_KEY.set(datasourceKey.trim());
    }

    public static String getCurrentDatasourceKey() {
        return CURRENT_DATASOURCE_KEY.get();
    }

    public static void clear() {
        CURRENT_DATASOURCE_KEY.remove();
    }
}
