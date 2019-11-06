package kr.dogfoot.webserver.server.host.proxy_info;

public class BackendServerManagerForLeastConnection extends BackendServerManager {
    @Override
    public BalanceMethod balanceMethod() {
        return BalanceMethod.LeastConnection;
    }

    @Override
    public synchronized BackendServerInfo appropriateBackendServer() {
        // not complete
        return null;
    }
}
