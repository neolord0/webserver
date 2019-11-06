package kr.dogfoot.webserver.server.buffersender;

import kr.dogfoot.webserver.context.Context;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SendBufferStorage {
    private ConcurrentHashMap<SocketChannel, ConcurrentLinkedQueue<SendBufferInfo>> storage;

    public SendBufferStorage() {
        storage = new ConcurrentHashMap<SocketChannel, ConcurrentLinkedQueue<SendBufferInfo>>();
    }

    public void addForClient(Context context, ByteBuffer buffer, boolean willRelease) {
        add(context.clientConnection().channel(), new SendBufferInfo().client(context, buffer, willRelease));
    }

    public void addForClientClose(Context context) {
        add(context.clientConnection().channel(), new SendBufferInfo().clientClose(context));
    }


    public void addForClientRelease(Context context) {
        add(context.clientConnection().channel(), new SendBufferInfo().clientRelease(context));

    }


    public void addForAjpServer(Context context, ByteBuffer buffer, boolean willRelease) {
        add(context.ajpProxy().channel(), new SendBufferInfo().ajpProxy(context, buffer, willRelease));
    }

    public void addForAjpServerClose(Context context) {
        add(context.ajpProxy().channel(), new SendBufferInfo().ajpProxyClose(context));
    }

    public void addForHttpServer(Context context, ByteBuffer buffer, boolean willRelease) {
        add(context.httpProxy().channel(), new SendBufferInfo().httpProxy(context, buffer, willRelease));
    }

    public void addForHttpServerClose(Context context) {
        add(context.httpProxy().channel(), new SendBufferInfo().httpProxyClose(context));
    }

    private synchronized void add(SocketChannel channel, SendBufferInfo bufferInfo) {
        ConcurrentLinkedQueue<SendBufferInfo> queue = storage.get(channel);
        if (queue == null) {
            queue = new ConcurrentLinkedQueue<SendBufferInfo>();
            storage.put(channel, queue);
        }
        queue.add(bufferInfo);
    }

    public synchronized SendBufferInfo nextSendBuffer(SocketChannel channel) {
        ConcurrentLinkedQueue<SendBufferInfo> queue = storage.get(channel);
        if (queue != null) {
            SendBufferInfo bufferInfo = queue.poll();
            if (bufferInfo != null && queue.isEmpty()) {
                storage.remove(channel);
            }
            return bufferInfo;
        }
        return null;
    }

    public synchronized boolean noBuffer(SocketChannel channel) {
        ConcurrentLinkedQueue<SendBufferInfo> queue = storage.get(channel);
        if (queue != null) {
            return queue.size() == 0;
        }
        return true;
    }

    public synchronized void removeBuffer(SocketChannel channel) {
        ConcurrentLinkedQueue<SendBufferInfo> queue = storage.get(channel);
        if (queue != null) {
            queue.clear();
            storage.remove(channel);
        }
    }
}
