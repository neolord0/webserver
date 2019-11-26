package kr.dogfoot.webserver.server.host.proxy_info;

import kr.dogfoot.webserver.server.host.proxy_info.filter.ProxyFilter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;

public class ProxyInfo {
    private static final int Default_Filter_Array_Length = 5;

    private int id;
    private String appliedURLPattern;
    private BackendServerManager backendServerManager;
    private ProxyFilter[] filters;
    private int filterCount;

    public ProxyInfo(int id) {
        this.id = id;

        filters = new ProxyFilter[Default_Filter_Array_Length];
        filterCount = 0;
    }

    public int id() {
        return id;
    }

    public String appliedURLPattern() {
        return appliedURLPattern;
    }

    public void appliedURLPattern(String appliedURLPattern) {
        this.appliedURLPattern = appliedURLPattern;
    }

    public boolean isMatchURL(String url) {
        return FilenameUtils.wildcardMatch(url, appliedURLPattern, IOCase.INSENSITIVE);
    }

    public BackendServerManager backendServerManager() {
        return backendServerManager;
    }

    public void backendServerManager(BackendServerManager backendServerManager) {
        this.backendServerManager = backendServerManager;
    }

    public ProxyFilter[] filters() {
        return filters;
    }

    public void addFilter(ProxyFilter filter) {
        if (filters.length <= filterCount) {
            ProxyFilter[] newArray = new ProxyFilter[filters.length * 2];
            System.arraycopy(filters, 0, newArray, 0, filters.length);
            filters = newArray;
        }

        filters[filterCount++] = filter;
    }
}
