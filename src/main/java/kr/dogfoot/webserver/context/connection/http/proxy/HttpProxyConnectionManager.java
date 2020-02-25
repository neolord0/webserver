package kr.dogfoot.webserver.context.connection.http.proxy;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.server.host.proxy_info.BackendServerInfo;
import kr.dogfoot.webserver.server.timer.Timer;
import kr.dogfoot.webserver.util.Message;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HttpProxyConnectionManager {
    private Timer timer;

    private ConcurrentLinkedQueue<HttpProxyConnection> connectionPool;

    private volatile int staticId;

    public HttpProxyConnectionManager(Timer timer) {
        this.timer = timer;

        connectionPool = new ConcurrentLinkedQueue<HttpProxyConnection>();

        staticId = 0;
    }

    public HttpProxyConnection pooledObject(Context context, BackendServerInfo backendServerInfo) {
        HttpProxyConnection conn = connectionPool.poll();
        if (conn == null) {
            conn = new HttpProxyConnection(staticId++);
        }
        conn.resetForPooled();
        conn.backendServerInfo(backendServerInfo);

        return conn;
    }

    public void releaseAndClose(Context context) {
        HttpProxyConnection conn = context.httpProxy();
        if (conn != null) {
            Message.debug(context.httpProxy(), "release and close http proxy connection");

            close(conn);
            addToPool(conn);

            context.httpProxy(null);
        }
    }

    public void close(HttpProxyConnection conn) {
        if (conn.channel() != null) {
            try {
                conn.channel().close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            conn.backendServerInfo().decreaseConnectCount();
            conn.channel(null);
        }
    }

    private void addToPool(HttpProxyConnection connection) {
        connectionPool.offer(connection);
    }

}
