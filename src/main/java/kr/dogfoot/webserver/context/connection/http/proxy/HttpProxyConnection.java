package kr.dogfoot.webserver.context.connection.http.proxy;

import kr.dogfoot.webserver.context.connection.ConnectionSort;
import kr.dogfoot.webserver.context.connection.http.HttpConnection;

public class HttpProxyConnection extends HttpConnection {
    private HttpProxyState state;

    public HttpProxyConnection(int id) {
        super(id);

        state = HttpProxyState.Idle;
    }

    @Override
    public ConnectionSort sort() {
        return ConnectionSort.HttpProxyConnection;
    }

    @Override
    public void resetForPooled() {
        super.resetForPooled();

        state = HttpProxyState.Idle;
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
