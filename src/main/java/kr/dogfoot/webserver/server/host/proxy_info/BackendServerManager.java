package kr.dogfoot.webserver.server.host.proxy_info;

public abstract class BackendServerManager {
    private ProxyInfo proxyInfo;

    private static final int DEFAULT_BACKEND_SERVER_COUNT = 3;
    protected BackendServerInfo[] backendServers;
    protected int backendServerCount;

    protected BackendServerManager(ProxyInfo proxyInfo) {
        this.proxyInfo = proxyInfo;

        backendServers = new BackendServerInfo[DEFAULT_BACKEND_SERVER_COUNT];
        backendServerCount = 0;
    }

    public abstract BalanceMethod balanceMethod();

    public BackendServerInfo addNewBackendServer() {
        if (backendServers.length <= backendServerCount) {
            BackendServerInfo[] newArray = new BackendServerInfo[backendServers.length * 2];
            System.arraycopy(backendServers, 0, newArray, 0, backendServers.length);
            backendServers = newArray;
        }

        backendServers[backendServerCount++] = new BackendServerInfo(proxyInfo, backendServerCount);
        return backendServers[backendServerCount - 1];
    }

    public abstract BackendServerInfo appropriateBackendServer();
}
