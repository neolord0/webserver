package kr.dogfoot.webserver.server.host.proxy_info;

public class BackendServerManagerForLeastConnection extends BackendServerManager {
    public BackendServerManagerForLeastConnection(ProxyInfo proxyInfo) {
        super(proxyInfo);

    }

    @Override
    public BalanceMethod balanceMethod() {
        return BalanceMethod.LeastConnection;
    }

    @Override
    public synchronized BackendServerInfo appropriateBackendServer() {
        BackendServerInfo result = null;
        for (BackendServerInfo bsi : backendServers) {
            if (bsi != null && bsi.connectCount() == 0) {
                return bsi;
            }
            if (result == null ||
                    (bsi != null && result.connectCount() > bsi.connectCount())) {
                result = bsi;
            }
        }
        return result;
    }
}
