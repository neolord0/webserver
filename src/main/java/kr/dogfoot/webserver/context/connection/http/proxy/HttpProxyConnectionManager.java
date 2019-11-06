package kr.dogfoot.webserver.context.connection.http.proxy;

import kr.dogfoot.webserver.context.Context;
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

    public HttpProxyConnection pooledObject(Context context) {
        HttpProxyConnection conn = connectionPool.poll();
        if (conn == null) {
            conn = new HttpProxyConnection(staticId++);
        } else {
            conn.resetForPooled();
        }
        return conn;
    }

    public void releaseAndClose(Context context) {
        Message.debug(context.httpProxy(), "release and close http proxy connection");
        _releaseAndClose(context);
    }

    private void _releaseAndClose(Context context) {
        HttpProxyConnection conn = context.httpProxy();
        if (conn != null) {
            if (conn.channel() != null) {
                try {
                    conn.channel().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                conn.channel(null);
            }
            context.httpProxy(null);

            addToPool(conn);
        }
    }

    private void addToPool(HttpProxyConnection connection) {
        connectionPool.offer(connection);
    }

    public void release(Context context) {
        HttpProxyConnection conn = context.httpProxy();
        if (conn != null) {
            context.httpProxy(null);
            addToPool(conn);
        }
    }

}
