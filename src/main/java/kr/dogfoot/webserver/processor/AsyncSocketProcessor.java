package kr.dogfoot.webserver.processor;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.server.Server;

import java.io.IOException;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AsyncSocketProcessor extends Processor {
    protected ConcurrentHashMap<SocketChannel, Context> contextMap;
    protected Selector nioSelector;
    protected volatile boolean running;
    protected Thread thread;
    private int selectDelayTime = 1000;

    protected AsyncSocketProcessor(Server server, int id) {
        super(server, id);
        selectDelayTime = 1000;

        contextMap = new ConcurrentHashMap<SocketChannel, Context>();
    }

    protected AsyncSocketProcessor(Server server, int id, int selectDelayTime) {
        this(server, id);

        this.selectDelayTime = selectDelayTime;
    }

    public void start() throws Exception {
        nioSelector = Selector.open();
        running = true;
        create_startSocketThread();
    }

    protected void create_startSocketThread() throws Exception {
        thread = new Thread(() -> {
            while (running) {
                try {
                    if (selectDelayTime == 0) {
                        nioSelector.select();
                    } else {
                        nioSelector.select(selectDelayTime);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                long currentTime = System.currentTimeMillis();
                checkNewContexts(currentTime);
                checkKeepAliveTimeout(currentTime);

                Iterator<SelectionKey> keys = nioSelector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isValid() && key.isAcceptable()) {
                        onAccept((ServerSocketChannel) key.channel(), currentTime);
                    }
                    if (key.isValid() && key.isConnectable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        Context context = contextMap.get(channel);

                        unregister(key);
                        onConnect(channel, context, currentTime);
                    }
                    if (key.isValid() && key.isReadable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        Context context = contextMap.get(channel);

                        unregister(key);
                        onReceive(channel, context, currentTime);
                    }
                }
            }
        });
        thread.start();
    }

    protected void setLastAccessTime(Context context, long currentTime) {
    }

    protected void checkKeepAliveTimeout(long currentTime) {
        for (Context context : contextMap.values()) {
            if (isOverTimeoutForKeepAlive(context, currentTime)) {
                closeConnectionForKeepAlive(context, true);
            }
        }
    }

    protected boolean isOverTimeoutForKeepAlive(Context context, long currentTime) {
        return false;
    }

    protected void closeConnectionForKeepAlive(Context context, boolean willUnregister) {
    }

    protected void onAccept(ServerSocketChannel serverChannel, long currentTime) {
    }

    protected void onReceive(SocketChannel channel, Context context, long currentTime) {
    }

    protected void onConnect(SocketChannel channel, Context context, long currentTime) {
    }

    protected void checkNewContexts(long currentTime) {
        nioSelector.wakeup();

        Context context;
        while ((context = waitingContextQueue.poll()) != null) {
            if (isOverTimeoutForKeepAlive(context, currentTime)) {
                closeConnectionForKeepAlive(context, false);
            } else {
                onNewContext(context);
            }
        }
    }

    protected abstract void onNewContext(Context context);

    protected boolean register(SocketChannel channel, Context context, int ops) {
        contextMap.put(channel, context);

        try {
            SelectionKey key = channel.register(nioSelector, ops);
            setSelectionKey(key, context);
            return true;
        } catch (ClosedChannelException e) {
            e.printStackTrace();

            contextMap.remove(channel);
            onErrorInRegister(channel, context);
        }
        return false;
    }

    protected abstract void setSelectionKey(SelectionKey key, Context context);

    protected abstract void onErrorInRegister(SocketChannel channel, Context context);

    protected void unregister(SelectionKey key) {
        if (key != null) {
            contextMap.remove(key.channel());
            key.cancel();
        }
    }

    @Override
    protected void wakeup() {
        nioSelector.wakeup();
    }

    @Override
    public void terminate() throws Exception {
        running = false;
        wakeup();
        nioSelector.close();
    }

    protected enum AfterProcess {
        Register,
        GotoSelf
    }
}
