package kr.dogfoot.webserver.server.host.proxy_info;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;

public class ProxyInfo {
    private static final int DEFAULT_BACKEND_SERVER_COUNT = 3;

    private int id;
    private String appliedURLPattern;
    private BackendServerManager backendServerManager;

    public ProxyInfo(int id) {
        this.id = id;
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
}
