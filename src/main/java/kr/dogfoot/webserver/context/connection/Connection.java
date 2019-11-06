package kr.dogfoot.webserver.context.connection;

import kr.dogfoot.webserver.context.Context;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public abstract class Connection {
    protected int id;

    protected Context context;

    protected SocketChannel channel;
    protected SelectionKey selectionKey;
    protected ByteBuffer receiveBuffer;
    protected InetSocketAddress remoteAddress;
    protected volatile long lastAccessTime;

    protected ByteBuffer bodyBuffer;

    public Connection(int id) {
        this.id = id;
        context = null;

        channel = null;
        selectionKey = null;
        receiveBuffer = ByteBuffer.allocate(receiveBufferSize());
        remoteAddress = null;

        bodyBuffer = null;
    }

    public abstract ConnectionSort sort();

    public boolean isClientConnection() {
        return sort() == ConnectionSort.HttpClientConnection || sort() == ConnectionSort.HttpsClientConnection;
    }

    public boolean isHttpProxyConnection() {
        return sort() == ConnectionSort.HttpProxyConnection || sort() == ConnectionSort.HttpsProxyConnection;
    }

    public void resetForPooled() {
        context = null;

        channel = null;
        selectionKey = null;
        receiveBuffer.clear();
        remoteAddress = null;

        bodyBuffer = null;
    }

    public abstract int receiveBufferSize();

    public int id() {
        return id;
    }

    public Context context() {
        return context;
    }

    public void context(Context context) {
        this.context = context;
    }

    public SocketChannel channel() {
        return channel;
    }

    public void channel(SocketChannel channel) {
        this.channel = channel;
        if (channel != null) {
            try {
                remoteAddress = (InetSocketAddress) channel.getRemoteAddress();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public SelectionKey selectionKey() {
        return selectionKey;
    }

    public void selectionKey(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
    }

    public ByteBuffer receiveBuffer() {
        return receiveBuffer;
    }

    public InetSocketAddress remoteAddress() {
        return remoteAddress;
    }

    public long lastAccessTime() {
        return lastAccessTime;
    }

    public void lastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public ByteBuffer bodyBuffer() {
        return bodyBuffer;
    }

    public void bodyBuffer(ByteBuffer bodyBuffer) {
        this.bodyBuffer = bodyBuffer;
    }
}
