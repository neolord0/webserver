package kr.dogfoot.webserver.context.connection.http.proxy;

import kr.dogfoot.webserver.context.connection.ConnectionSort;
import kr.dogfoot.webserver.context.connection.http.HttpConnection;
import kr.dogfoot.webserver.server.host.proxy_info.BackendServerInfo;
import kr.dogfoot.webserver.server.host.proxy_info.ProxyInfo;

public class HttpProxyConnection extends HttpConnection {
    private BackendServerInfo backendServerInfo;

    private HttpProxyState state;

    public HttpProxyConnection(int id) {
        super(id);
    }

    @Override
    public ConnectionSort sort() {
        return ConnectionSort.HttpProxyConnection;
    }

    @Override
    public void resetForPooled() {
        super.resetForPooled();

        backendServerInfo = null;

        state = HttpProxyState.Idle;
    }

    public BackendServerInfo backendServerInfo() {
        return backendServerInfo;
    }

    public void backendServerInfo(BackendServerInfo backendServerInfo) {
        this.backendServerInfo = backendServerInfo;
    }

    @Override
    public boolean adjustSSL() {
        return false;
    }

    public HttpProxyState state() {
        return state;
    }

    public HttpProxyConnection changeState(HttpProxyState state) {
        this.state = state;
        return this;
    }

    public void resetForNextRequest() {
        parserStatus.reset();
        senderStatus.reset();
    }
}
