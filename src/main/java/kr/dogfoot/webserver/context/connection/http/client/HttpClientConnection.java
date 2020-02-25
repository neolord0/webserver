package kr.dogfoot.webserver.context.connection.http.client;

import kr.dogfoot.webserver.context.connection.ConnectionSort;
import kr.dogfoot.webserver.context.connection.http.HttpConnection;

public class HttpClientConnection extends HttpConnection {
    public HttpClientConnection(int id) {
        super(id);
    }

    @Override
    public ConnectionSort sort() {
        return ConnectionSort.HttpClientConnection;
    }

    @Override
    public void resetForPooled() {
        super.resetForPooled();
    }

    public void resetForNextRequest() {
        parserStatus.reset();
        senderStatus.reset();
    }

    @Override
    public boolean isAdjustSSL() {
        return false;
    }
}
