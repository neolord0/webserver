package kr.dogfoot.webserver.processor.client;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.context.connection.http.client.HandshakeState;
import kr.dogfoot.webserver.context.connection.http.client.HttpsClientConnection;
import kr.dogfoot.webserver.processor.Processor;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.server.host.Host;
import kr.dogfoot.webserver.util.Message;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SSLHandshaker extends Processor {
    private static int SSLHandshakerID = 0;
    private volatile boolean running;

    private Thread handshakingThread;

    private ConcurrentLinkedQueue<Context> waitingQueueForSocket;
    private ConcurrentHashMap<SocketChannel, Context> contextMap;
    private Selector nioSelector;
    private Thread socketThread;

    public SSLHandshaker(Server server) {
        super(server, SSLHandshakerID++);

        waitingQueueForSocket = new ConcurrentLinkedQueue<Context>();
        contextMap = new ConcurrentHashMap<SocketChannel, Context>();
    }

    @Override
    public void start() throws Exception {
        nioSelector = Selector.open();

        running = true;

        create_startHandshakingThread();
        create_startSocketThread();
    }

    private void create_startHandshakingThread() {
        handshakingThread = new Thread(() -> {
            while (running) {
                synchronized (handshakingThread) {
                    while (waitingContextQueue.peek() == null) {
                        try {
                            handshakingThread.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                Context context = waitingContextQueue.poll();
                HttpsClientConnection conn = (HttpsClientConnection) context.clientConnection();
                switch (conn.handshakeState()) {
                    case NotBegin:
                        beginHandShaking(context, conn);
                        break;
                    case Handshaking:
                        handshake(context, conn);
                        break;
                    case Success:
                        onSuccessHandshake(context);
                        break;
                    case Fail:
                        onFailHandshake(context);
                        break;
                }
            }
        });
        handshakingThread.start();
    }

    private void create_startSocketThread() {
        socketThread = new Thread(() -> {
            while (running) {
                checkNewContexts();

                try {
                    if (nioSelector.select(1000) == 0) {
                        continue;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Iterator<SelectionKey> keys = nioSelector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    SocketChannel channel = (SocketChannel) key.channel();
                    Context context = contextMap.get(channel);

                    if (key.isValid() && key.isReadable()) {
                        onReceive(channel, context);
                    }
                    if (key.isValid() && key.isWritable()) {
                        onSend(channel, context);
                    }
                }
            }
        });
        socketThread.start();
    }

    private void beginHandShaking(Context context, HttpsClientConnection connection) {
        boolean error = false;

        try {
            if (createSSLEngine(connection)) {
                connection.sslEngine().beginHandshake();
            } else {
                error = true;
            }
        } catch (SSLException e) {
            e.printStackTrace();
        }

        if (error == false) {
            Message.debug(context, "begin ssl handshake");

            connection.handshakeState(HandshakeState.Handshaking);
            gotoSelf(context);
        } else {
            Message.debug(context, "error in begin handshake");

            clientConnectionManager().releaseAndClose(context);
            contextManager().release(context);
        }
    }

    private boolean createSSLEngine(HttpsClientConnection connection) {
        Host host = getHostBySocket(connection.channel());
        if (host != null) {
            connection.sslEngine(host.createSSLEngine());
            Message.debug(connection, "Create SSL Engine");
            return true;
        }
        return false;
    }

    private Host getHostBySocket(SocketChannel channel) {
        InetSocketAddress addr = null;
        try {
            addr = (InetSocketAddress) channel.getLocalAddress();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addr != null) {
            return server.findHost(addr.getHostName(), addr.getPort());
        }
        return null;
    }


    private void handshake(Context context, HttpsClientConnection connection) {
        SSLEngine sslEngine = connection.sslEngine();
        boolean continueHandshake = false;

        switch (sslEngine.getHandshakeStatus()) {
            case NEED_WRAP:
                continueHandshake = wrap(context, connection);
                break;

            case NEED_UNWRAP:
                continueHandshake = unwrap(context, connection);
                break;

            case NEED_TASK:
                task(sslEngine);
                continueHandshake = true;
                break;

            case FINISHED:
                continueHandshake = false;
                break;
        }

        if (continueHandshake) {
            gotoSelf(context);
        } else {
            if (connection.handshakeState() == HandshakeState.Fail) {
                onFailHandshake(context);
            }
        }
    }

    private void onSuccessHandshake(Context context) {
        Message.debug(context, "success handshake");

        server.gotoRequestReceiver(context);
    }

    private void onFailHandshake(Context context) {
        Message.debug(context, "fail handshake");

        clientConnectionManager().releaseAndClose(context);
        contextManager().release(context);
    }


    private boolean wrap(Context context, HttpsClientConnection connection) {
        SSLEngine sslEngine = connection.sslEngine();
        ByteBuffer buffer = bufferManager().pooledBuffer(sslEngine.getSession().getPacketBufferSize());
        boolean retry = false;
        SSLEngineResult wrapResult = null;

        do {
            try {
                wrapResult = sslEngine.wrap(buffer, buffer);
            } catch (SSLException e) {
                e.printStackTrace();
                bufferManager().release(buffer);
                connection.handshakeState(HandshakeState.Fail);
                return false;
            }

            switch (wrapResult.getStatus()) {
                case OK:
                    buffer.flip();
                    connection
                            .handshakeWrappedBuffer(buffer);
                    if (wrapResult.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED) {
                        connection.handshakeState(HandshakeState.SendDataAtLast);
                    } else {
                        connection.handshakeState(HandshakeState.SendData);
                    }
                    gotoSocketThread(context);
                    return false;

                case BUFFER_OVERFLOW:
                    bufferManager().release(buffer);
                    buffer = bufferManager().pooledBuffer(buffer.capacity() * 2);
                    retry = true;
                    break;

                case CLOSED:
                    Message.debug(connection, "close in wrapping");
                    connection.handshakeState(HandshakeState.Fail);
                    return false;
            }
        } while (retry);
        return true;
    }

    private void gotoSocketThread(Context context) {
        waitingQueueForSocket.add(context);
        nioSelector.wakeup();
    }

    private void task(SSLEngine sslEngine) {
        Runnable r = null;
        while ((r = sslEngine.getDelegatedTask()) != null) {
            r.run();
        }
    }

    private boolean unwrap(Context context, HttpsClientConnection connection) {
        SSLEngineResult unwrapResult = null;
        connection.receiveBuffer().flip();
        try {
            unwrapResult = connection.sslEngine().unwrap(connection.receiveBuffer(), connection.receiveBuffer());
        } catch (SSLException e) {
            e.printStackTrace();

            connection.handshakeState(HandshakeState.Fail);
            return false;
        }
        connection.receiveBuffer().compact();

        switch (unwrapResult.getStatus()) {
            case OK:
                if (unwrapResult.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED) {
                    connection.handshakeState(HandshakeState.Success);
                    return true;
                }
                break;

            case BUFFER_UNDERFLOW:
                connection.handshakeState(HandshakeState.ReceiveData);
                gotoSocketThread(context);
                return false;

            case CLOSED:
                Message.debug(connection, "close in unwrapping");
                connection.handshakeState(HandshakeState.Fail);
                return false;
        }
        return true;
    }

    private void checkNewContexts() {
        if (waitingQueueForSocket.peek() == null) {
            return;
        }

        try {
            nioSelector.selectNow();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Context context;
        while ((context = waitingQueueForSocket.poll()) != null) {
            HttpsClientConnection conn = (HttpsClientConnection) context.clientConnection();
            if (conn.handshakeStateIsReceiving()) {
                register(conn.channel(), context, SelectionKey.OP_READ);
            } else if (conn.handshakeStateIsSending()) {
                register(conn.channel(), context, SelectionKey.OP_WRITE);
            }
        }
    }

    private boolean register(SocketChannel channel, Context context, int ops) {
        contextMap.put(channel, context);

        try {
            channel.register(nioSelector, ops);
            return true;
        } catch (Exception e) {
            e.printStackTrace();

            contextMap.remove(channel);

            clientConnectionManager().releaseAndClose(context);
            contextManager().release(context);
        }
        return false;
    }


    private void onReceive(SocketChannel channel, Context context) {
        HttpsClientConnection conn = (HttpsClientConnection) context.clientConnection();

        int numRead = -2;
        try {
            numRead = channel.read(conn.receiveBuffer());
        } catch (Exception e) {
            e.printStackTrace();
            numRead = -2;
        }

        if (numRead == -2) {
            conn.handshakeState(HandshakeState.Fail);
            gotoHandshakeThread(context, conn);
            return;
        }

        if (numRead > 0) {
            gotoHandshakeThread(context, conn);
        }
    }

    private void gotoHandshakeThread(Context context, HttpsClientConnection conn) {
        if (conn.handshakeState() == HandshakeState.SendDataAtLast) {
            conn.handshakeState(HandshakeState.Success);
        } else {
            conn.handshakeState(HandshakeState.Handshaking);
        }

        unregister(conn.channel());
        gotoSelf(context);
    }

    private void unregister(SocketChannel channel) {
        SelectionKey key = channel.keyFor(nioSelector);
        if (key != null) {
            key.cancel();
        }
        contextMap.remove(channel);
    }

    private void onSend(SocketChannel channel, Context context) {
        HttpsClientConnection conn = (HttpsClientConnection) context.clientConnection();
        ByteBuffer buffer = conn.handshakeWrappedBuffer();

        if (buffer.hasRemaining()) {
            try {
                channel.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();

                conn.handshakeState(HandshakeState.Fail);
                gotoHandshakeThread(context, conn);
            }
        }

        if (buffer.hasRemaining() == false) {
            bufferManager().release(buffer);

            gotoHandshakeThread(context, conn);
        }
    }

    @Override
    protected void wakeup() {
        synchronized (handshakingThread) {
            handshakingThread.notify();
        }
    }

    @Override
    public void terminate() throws Exception {
        running = false;
        wakeup();
    }
}
