package kr.dogfoot.webserver.context.connection.ajp;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.server.host.proxy_info.BackendServerInfo;
import kr.dogfoot.webserver.server.timer.Timer;
import kr.dogfoot.webserver.server.timer.TimerEventHandler;
import kr.dogfoot.webserver.util.Message;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AjpProxyConnectionManager implements TimerEventHandler {
    private Timer timer;

    private ConcurrentLinkedQueue<AjpProxyConnection> connectionPool;
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<AjpProxyConnection>> idleConnectionMaps;
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<AjpProxyConnection>> assignedConnectionMaps;

    private volatile int staticId;

    public AjpProxyConnectionManager(Timer timer) {
        this.timer = timer;

        connectionPool = new ConcurrentLinkedQueue<AjpProxyConnection>();
        idleConnectionMaps = new ConcurrentHashMap<String, ConcurrentLinkedQueue<AjpProxyConnection>>();
        assignedConnectionMaps = new ConcurrentHashMap<String, ConcurrentLinkedQueue<AjpProxyConnection>>();

        staticId = 0;
    }

    public AjpProxyConnection pooledbject(Context context) {
        AjpProxyConnection conn = getIdleConnection(context.backendServerInfo());
        if (conn == null) {
            conn = connectionPool.poll();
            if (conn == null) {
                conn = new AjpProxyConnection(staticId++);
            }
            conn.resetForPooled();
        } else {
            conn.killTimerForIdle(timer);
            conn.resetForIdled();
        }

        addAssignedConnection(conn, context.backendServerInfo());
        return conn;
    }

    private AjpProxyConnection getIdleConnection(BackendServerInfo backendServer) {
        ConcurrentLinkedQueue<AjpProxyConnection> queue
                = getIdleConnectionQueue(backendServer);
        return queue.poll();
    }

    private synchronized ConcurrentLinkedQueue<AjpProxyConnection> getIdleConnectionQueue(BackendServerInfo backendServer) {
        String wasAddress2 = backendServer.socketAddress().toString();
        ConcurrentLinkedQueue<AjpProxyConnection> queue = idleConnectionMaps.get(wasAddress2);
        if (queue == null) {
            queue = new ConcurrentLinkedQueue<AjpProxyConnection>();
            idleConnectionMaps.put(wasAddress2, queue);
        }
        return queue;
    }

    private void addAssignedConnection(AjpProxyConnection connection, BackendServerInfo backendServer) {
        ConcurrentLinkedQueue<AjpProxyConnection> queue
                = getAssignedConnectionQueue(backendServer);
        queue.offer(connection);
    }

    private synchronized ConcurrentLinkedQueue<AjpProxyConnection> getAssignedConnectionQueue(BackendServerInfo backendServer) {
        String wasAddress = backendServer.socketAddress().toString();
        ConcurrentLinkedQueue<AjpProxyConnection> queue = assignedConnectionMaps.get(wasAddress);
        if (queue == null) {
            queue = new ConcurrentLinkedQueue<AjpProxyConnection>();
            assignedConnectionMaps.put(wasAddress, queue);
        }
        return queue;
    }

    public void releaseAndClose(Context context) {
        AjpProxyConnection conn = context.ajpProxy();
        if (conn != null) {
            Message.debug(conn, "release and close ajp proxy connection");

            conn.killTimerForIdle(timer);
            _releaseAndClose(conn);
            removeFromAssigned(conn, context.backendServerInfo());

            context.ajpProxy(null);
        }
        context
                .proxyInfo(null)
                .backendServerInfo(null);
    }

    private void _releaseAndClose(AjpProxyConnection conn) {
        if (conn.channel() != null) {
            try {
                conn.channel().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            conn.channel(null);
        }
        addToPool(conn);
    }

    private void addToPool(AjpProxyConnection connection) {
        connectionPool.offer(connection);
    }

    private void removeFromAssigned(AjpProxyConnection connection, BackendServerInfo backendServer) {
        ConcurrentLinkedQueue<AjpProxyConnection> assignedConnectionQueue
                = assignedConnectionMaps.get(backendServer.socketAddress().toString());
        if (assignedConnectionQueue != null) {
            assignedConnectionQueue.remove(connection);
        }
    }

    public void idle(Context context) {
        Message.debug(context.ajpProxy(), "idle ajp proxy connection");

        removeFromAssigned(context.ajpProxy(), context.backendServerInfo());
        addToIdle(context.ajpProxy(), context.backendServerInfo());
        context.ajpProxy().setTimerForIdle(timer, context.backendServerInfo().idle_timeout(), this);

        context.ajpProxy(null);
    }


    private void addToIdle(AjpProxyConnection connection, BackendServerInfo backendServer) {
        ConcurrentLinkedQueue<AjpProxyConnection> queue
                = getIdleConnectionQueue(backendServer);
        queue.offer(connection);
    }

    @Override
    public void HandleTimerEvent(Object data, long time) {
        AjpProxyConnection conn = (AjpProxyConnection) data;
        if (conn != null) {
            _releaseAndClose(conn);
        }
    }
}
