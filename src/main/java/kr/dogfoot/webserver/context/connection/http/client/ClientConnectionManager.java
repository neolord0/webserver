package kr.dogfoot.webserver.context.connection.http.client;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.server.timer.Timer;
import kr.dogfoot.webserver.util.Message;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientConnectionManager {
    private ConcurrentLinkedQueue<HttpClientConnection> poolForHttp;
    private ConcurrentLinkedQueue<HttpsClientConnection> poolForHttps;

    private ConcurrentLinkedQueue<HttpClientConnection> usedConnections;

    private volatile int staticId;

    public ClientConnectionManager() {
        poolForHttp = new ConcurrentLinkedQueue<HttpClientConnection>();
        poolForHttps = new ConcurrentLinkedQueue<HttpsClientConnection>();
    }

    public HttpClientConnection pooledObject(SocketChannel channel, boolean ssl) {
        if (ssl) {
            return pooledHttpsConnection(channel);
        } else {
            return pooledHttpConnection(channel);
        }
    }

    private HttpClientConnection pooledHttpConnection(SocketChannel channel) {
        HttpClientConnection hc = poolForHttp.poll();
        if (hc == null) {
            hc = new HttpClientConnection(staticId++);
        }
        hc.resetForPooled();
        hc.channel(channel);
        return hc;
    }

    private HttpsClientConnection pooledHttpsConnection(SocketChannel channel) {
        HttpsClientConnection hc = poolForHttps.poll();
        if (hc == null) {
            hc = new HttpsClientConnection(staticId++);
        }
        hc.resetForPooled();
        hc.channel(channel);
        return hc;
    }

    public void releaseAndClose(Context context) {
        Message.debug(context, "release and close http connection.");
        if (context.clientConnection() != null) {
            HttpClientConnection conn = context.clientConnection();
            if (conn.channel() != null) {
                try {
                    conn.channel().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                conn.channel(null);
            }

            if (conn.adjustSSL()) {
                HttpsClientConnection conn2 = (HttpsClientConnection) conn;
                if (conn2.sslEngine() != null) {
                    conn2.sslEngine(null);
                }
                poolForHttps.add(conn2);
            } else {
                poolForHttp.add(conn);
            }
            context.clientConnection(null);
        }
    }

    public void release(Context context) {
        Message.debug(context, "release and close http connection.");
        if (context.clientConnection() != null) {
            HttpClientConnection conn = context.clientConnection();

            if (conn.adjustSSL()) {
                HttpsClientConnection conn2 = (HttpsClientConnection) conn;
                if (conn2.sslEngine() != null) {
                    conn2.sslEngine(null);
                }
                poolForHttps.add(conn2);
            } else {
                poolForHttp.add(conn);
            }
            context.clientConnection(null);
        }
    }
}
