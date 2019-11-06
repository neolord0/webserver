package kr.dogfoot.webserver.server.host.proxy_info;

public class BackendServerManagerForLeastLoad extends BackendServerManager {

    @Override
    public BalanceMethod balanceMethod() {
        return BalanceMethod.LeastLoad;
    }

    @Override
    public synchronized BackendServerInfo appropriateBackendServer() {
        // not complete
        return null;
    }
}
