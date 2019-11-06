package kr.dogfoot.webserver.server.host.proxy_info;

public class BackendServerManagerForRoundRobin extends BackendServerManager {
    private volatile int roundingIndex;

    public BackendServerManagerForRoundRobin() {
        roundingIndex = 0;
    }

    @Override
    public BalanceMethod balanceMethod() {
        return BalanceMethod.RoundRobin;
    }

    @Override
    public synchronized BackendServerInfo appropriateBackendServer() {
        BackendServerInfo backendServer = backendServers[roundingIndex];
        roundingIndex = (roundingIndex + 1 >= backendServerCount) ? 0 : roundingIndex + 1;
        return backendServer;
    }
}
