package kr.dogfoot.webserver.server.cache;

import kr.dogfoot.webserver.server.host.proxy_info.BackendServerInfo;
import kr.dogfoot.webserver.server.resource.look.LookState;

public class CacheHost {
    private String ipOrDomain;
    private int port;

    private CacheEntry rootEntry;

    public CacheHost(CacheManagerImp manager, BackendServerInfo backendServerInfo) {
        ipOrDomain = backendServerInfo.ipOrDomain();
        port = backendServerInfo.port();

        rootEntry = new CacheEntry(manager)
                .openPath(manager.storagePathFile(),ipOrDomain + "_" + port);
    }

    public CacheHost(CacheManagerImp manager, String ipOrDomain, int port) {
        this.ipOrDomain = ipOrDomain;
        this.port = port;

        rootEntry = new CacheEntry(manager)
                .openPath(manager.storagePathFile(), ipOrDomain + "_" + port);
    }


    public boolean isMatch(BackendServerInfo backendServerInfo) {
        if (ipOrDomain.equalsIgnoreCase(backendServerInfo.ipOrDomain())
                && port == backendServerInfo.port()) {
            return true;
        }
        return false;
    }

    public CacheEntry getEntry(LookState ls, boolean createChildItem) {
        return rootEntry.find(ls, createChildItem);
    }

    public String ipOrDomain() {
        return ipOrDomain;
    }

    public void ipOrDomain(String ipOrDomain) {
        this.ipOrDomain = ipOrDomain;
    }

    public int port() {
        return port;
    }

    public void port(int port) {
        this.port = port;
    }

    public CacheEntry rootEntry() {
        return rootEntry;
    }
}
