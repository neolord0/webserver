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

    public AjpProxyConnection pooledbject(Context context, BackendServerInfo backendServerInfo) {
        AjpProxyConnection conn = getIdleConnection(backendServerInfo);
        if (conn == null) {
            conn = connectionPool.poll();
            if (conn == null) {
                conn = new AjpProxyConnection(staticId++);
            }
            conn.resetForPooled();
            conn.backendServerInfo(backendServerInfo);
        } else {
            conn.killTimerForIdle(timer);
            conn.resetForIdled();
        }

        addAssignedConnection(conn);
        return conn;
    }

    private AjpProxyConnection getIdleConnection(BackendServerInfo backendServer) {
        ConcurrentLinkedQueue<AjpProxyConnection> queue
                = getIdleConnectionQueue(backendServer);
        return queue.poll();
    }

    private synchronized ConcurrentLinkedQueue<AjpProxyConnection> getIdleConnectionQueue(BackendServerInfo backendServer) {
        String wasAddress = backendServer.address();
        ConcurrentLinkedQueue<AjpProxyConnection> queue = idleConnectionMaps.get(wasAddress);
        if (queue == null) {
            queue = new ConcurrentLinkedQueue<AjpProxyConnection>();
            idleConnectionMaps.put(wasAddress, queue);
        }
        return queue;
    }

    private void addAssignedConnection(AjpProxyConnection connection) {
        ConcurrentLinkedQueue<AjpProxyConnection> queue
                = getAssignedConnectionQueue(connection.backendServerInfo());
        queue.offer(connection);
    }

    private synchronized ConcurrentLinkedQueue<AjpProxyConnection> getAssignedConnectionQueue(BackendServerInfo backendServer) {
        String wasAddress = backendServer.address();
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

            close(conn);
            addToPool(conn);

            removeFromAssigned(conn);
            context.ajpProxy(null);
        }
    }

    public void close(AjpProxyConnection conn) {
        if (conn.channel() != null) {
            try {
                conn.channel().close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            conn.killTimerForIdle(timer);
            conn.backendServerInfo().decreaseConnectCount();
            ;
            conn.channel(null);
        }
    }

    private void addToPool(AjpProxyConnection connection) {
        connectionPool.offer(connection);
    }

    private void removeFromAssigned(AjpProxyConnection connection) {
        ConcurrentLinkedQueue<AjpProxyConnection> assignedConnectionQueue
                = assignedConnectionMaps.get(connection.backendServerInfo().socketAddress().toString());
        if (assignedConnectionQueue != null) {
            assignedConnectionQueue.remove(connection);
        }
    }

    public void idle(Context context) {
        Message.debug(context.ajpProxy(), "idle ajp proxy connection");

        removeFromAssigned(context.ajpProxy());
        addToIdle(context.ajpProxy());
        context.ajpProxy().setTimerForIdle(timer, context.ajpProxy().backendServerInfo().idle_timeout(), this);

        context.ajpProxy(null);
    }


    private void addToIdle(AjpProxyConnection connection) {
        ConcurrentLinkedQueue<AjpProxyConnection> queue
                = getIdleConnectionQueue(connection.backendServerInfo());
        queue.offer(connection);
    }

    @Override
    public void HandleTimerEvent(Object data, long time) {
        AjpProxyConnection conn = (AjpProxyConnection) data;
        if (conn != null) {
            close(conn);
            addToPool(conn);
        }
    }
}
