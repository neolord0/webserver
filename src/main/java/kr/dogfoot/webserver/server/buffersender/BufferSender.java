package kr.dogfoot.webserver.server.buffersender;

import kr.dogfoot.webserver.context.connection.http.client.HttpsClientConnection;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.server.Startable;
import kr.dogfoot.webserver.server.object.BufferManager;
import kr.dogfoot.webserver.util.Message;

import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class BufferSender implements Startable {
    private static int BufferSenderID = 0;
    private Server server;
    private int id;

    private volatile boolean running;

    private Thread observerThread;
    private LinkedBlockingQueue<ObserveJob> waitingJobQueueForObserver;
    private ConcurrentHashMap<SocketChannel, SendBufferInfo> sendingBufferMap;

    private Selector nioSelector;
    private Thread socketThread;
    private ConcurrentLinkedQueue<SendJob> waitingJobQueueForSocket;

    public BufferSender(Server server) {
        this.server = server;
        id = BufferSenderID++;

        waitingJobQueueForObserver = new LinkedBlockingQueue<ObserveJob>();
        sendingBufferMap = new ConcurrentHashMap<SocketChannel, SendBufferInfo>();
        waitingJobQueueForSocket = new ConcurrentLinkedQueue<SendJob>();
    }

    public int id() {
        return id;
    }

    public void notifyStoring(SocketChannel channel) {
        waitingJobQueueForObserver.add(new ObserveJob(JobType.NotifyStoring, channel));
    }

    @Override
    public void start() throws Exception {
        running = true;

        create_startObserverThread();
        create_startSocketThread();
    }

    private void create_startObserverThread() {
        observerThread = new Thread(() -> {
            while (running) {
                ObserveJob job = null;
                try {
                    job = waitingJobQueueForObserver.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (job == null || job.type == JobType.None) {
                    continue;
                }
                switch (job.type) {
                    case NotifyStoring:
                        if (isSending(job.channel) == false) {
                            orderToSendNextBuffer(job.channel);
                        }
                        break;
                    case RetrySending:
                        orderToSendCurrentBuffer(job.channel);
                        break;
                    case EndSending:
                        releaseBuffer(job.channel);
                        setSendingFalse(job.channel);
                        orderToSendNextBuffer(job.channel);
                        break;
                }
            }
        });
        observerThread.start();
    }

    private boolean isSending(SocketChannel channel) {
        return sendingBufferMap.containsKey(channel);
    }

    private void orderToSendNextBuffer(SocketChannel channel) {
        SendBufferInfo nextBufferInfo = server.objects().sendBufferStorage().nextSendBuffer(channel);
        if (nextBufferInfo != null) {
            setSendingTrue(channel, nextBufferInfo);

            waitingJobQueueForSocket.add(new SendJob(channel, nextBufferInfo));
            nioSelector.wakeup();
        } else {
            setSendingFalse(channel);
        }
    }

    private void setSendingTrue(SocketChannel channel, SendBufferInfo bufferInfo) {
        sendingBufferMap.put(channel, bufferInfo);
    }

    private void setSendingFalse(SocketChannel channel) {
        sendingBufferMap.remove(channel);
    }

    private void orderToSendCurrentBuffer(SocketChannel channel) {
        SendBufferInfo currentBufferInfo = sendingBufferMap.get(channel);

        waitingJobQueueForSocket.add(new SendJob(channel, currentBufferInfo));
        nioSelector.wakeup();
    }

    private void releaseBuffer(SocketChannel channel) {
        SendBufferInfo bufferInfo = sendingBufferMap.get(channel);
        if (bufferInfo.willRelease()) {
            server.objects().bufferManager().release(bufferInfo.buffer());
        }
    }

    private void create_startSocketThread() {
        try {
            nioSelector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }

        socketThread = new Thread(() -> {
            while (running) {
                checkNewJobs();
                try {
                    if (nioSelector.select() == 0) {
                        continue;
                    }

                    Iterator<SelectionKey> keys = nioSelector.selectedKeys().iterator();

                    while (keys.hasNext()) {
                        SelectionKey key = keys.next();
                        keys.remove();

                        if (!key.isValid()) {
                            continue;
                        }

                        SocketChannel channel = (SocketChannel) key.channel();
                        SendBufferInfo bufferInfo = sendingBufferMap.get(channel);
                        if (key.isWritable()) {
                            key.cancel();
                            switch (bufferInfo.jobType()) {
                                case SendBuffer:
                                    onSend(channel, bufferInfo);
                                    break;
                                case Close:
                                    onClose(channel, bufferInfo);
                                    break;
                                case Release:
                                    onRelease(channel, bufferInfo);
                                    break;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        socketThread.start();
    }

    private void checkNewJobs() {
        if (waitingJobQueueForSocket.peek() == null) {
            return;
        }

        try {
            nioSelector.selectNow();
        } catch (IOException e) {
            e.printStackTrace();
        }

        SendJob job;
        while ((job = waitingJobQueueForSocket.poll()) != null) {
            register(job.channel);
        }
    }

    private void register(SocketChannel channel) {
        try {
            channel.register(nioSelector, SelectionKey.OP_WRITE);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
    }

    private void onSend(SocketChannel channel, SendBufferInfo bufferInfo) {
        if (bufferInfo.isHttps() && bufferInfo.wrapped() == false) {
            wrapBuffer(bufferInfo);
        }
        boolean error = false;
        try {
            channel.write(bufferInfo.buffer());
        } catch (IOException e) {
            e.printStackTrace();

            switch (bufferInfo.protocol()) {
                case Client:
                    Message.debug(bufferInfo.context().clientConnection(), "error in send : " + e.getMessage());
                    break;
                case AjpProxy:
                    Message.debug(bufferInfo.context().ajpProxy(), "error in send : " + e.getMessage());
                    break;
                case HttpProxy:
                    Message.debug(bufferInfo.context().httpProxy(), "error in send: " + e.getMessage());
                    break;
            }
            error = true;
        }

        if (error == true) {
            server.objects().sendBufferStorage().removeBuffer(channel);
        } else {
            if (bufferInfo.buffer().hasRemaining()) {
                waitingJobQueueForObserver.add(new ObserveJob(JobType.RetrySending, channel));
            } else {
                waitingJobQueueForObserver.add(new ObserveJob(JobType.EndSending, channel));
            }
        }
    }

    private void wrapBuffer(SendBufferInfo bufferInfo) {
        HttpsClientConnection conn = (HttpsClientConnection) bufferInfo.context().clientConnection();
        BufferManager bufferManager = server.objects().bufferManager();
        ByteBuffer wrappedBuffer = bufferManager.pooledBuffer(conn.sslEngine().getSession().getPacketBufferSize());

        boolean retry = true;
        do {
            SSLEngineResult wrapResult = null;
            try {
                wrapResult = conn.sslEngine().wrap(bufferInfo.buffer(), wrappedBuffer);
            } catch (SSLException e) {
                e.printStackTrace();
            }

            if (wrapResult == null) {
                return;
            }

            switch (wrapResult.getStatus()) {
                case OK: {
                    if (bufferInfo.willRelease()) {
                        bufferManager.release(bufferInfo.buffer());
                    }
                    wrappedBuffer.flip();
                    bufferInfo.buffer(wrappedBuffer);
                    bufferInfo.wrapped(true);
                    retry = false;
                }
                break;

                case BUFFER_OVERFLOW:
                    Message.debug(conn, "wrap data in BUFFER_OVERFLOW");
                    bufferManager.release(wrappedBuffer);
                    wrappedBuffer = bufferManager.pooledBuffer(wrappedBuffer.capacity() * 2);
                    retry = true;
                    break;

                case CLOSED:
                    Message.debug(conn, "close in wrapping");
                    retry = false;
                    break;
            }
        } while (retry);
    }

    private void onClose(SocketChannel channel, SendBufferInfo bufferInfo) {
        if (bufferInfo.protocol() == SendBufferInfo.Protocol.Client) {
            server.objects().clientConnectionManager().releaseAndClose(bufferInfo.context());
            server.objects().contextManager().release(bufferInfo.context());
        } else if (bufferInfo.protocol() == SendBufferInfo.Protocol.AjpProxy) {
            server.objects().ajpProxyConnectionManager().releaseAndClose(bufferInfo.context());
        } else if (bufferInfo.protocol() == SendBufferInfo.Protocol.HttpProxy) {
            server.objects().httpProxyConnectionManager().releaseAndClose(bufferInfo.context());
        }

        /*
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (bufferInfo.protocol() == SendBufferInfo.Protocol.Client) {
                    server.objects().clientConnectionManager().releaseAndClose(bufferInfo.context());
                    server.objects().contextManager().release(bufferInfo.context());
                } else if (bufferInfo.protocol() == SendBufferInfo.Protocol.AjpProxy) {
                    server.objects().ajpProxyConnectionManager().releaseAndClose(bufferInfo.context());
                } else if (bufferInfo.protocol() == SendBufferInfo.Protocol.HttpProxy) {
                    server.objects().httpProxyConnectionManager().releaseAndClose(bufferInfo.context());
                }
            }
        };
        server.objects().ioExecutorService().execute(r);
         */
    }


    private void onRelease(SocketChannel channel, SendBufferInfo bufferInfo) {
        if (bufferInfo.protocol() == SendBufferInfo.Protocol.Client) {
            server.objects().clientConnectionManager().release(bufferInfo.context());
            server.objects().contextManager().release(bufferInfo.context());
        }
    }


    @Override
    public void terminate() throws Exception {
        running = false;

        waitingJobQueueForObserver.add(new ObserveJob(JobType.None, null));
        nioSelector.wakeup();
    }

    private enum JobType {
        None,
        NotifyStoring,
        RetrySending,
        EndSending,
    }

    private class ObserveJob {
        JobType type;
        SocketChannel channel;

        public ObserveJob(JobType type, SocketChannel channel) {
            this.type = type;
            this.channel = channel;
        }
    }

    private class SendJob {
        SocketChannel channel;
        SendBufferInfo bufferInfo;

        public SendJob(SocketChannel channel, SendBufferInfo bufferInfo) {
            this.channel = channel;
            this.bufferInfo = bufferInfo;
        }
    }
}
