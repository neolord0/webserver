package kr.dogfoot.webserver.processor.client;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.context.ContextState;
import kr.dogfoot.webserver.context.connection.http.client.HttpClientConnection;
import kr.dogfoot.webserver.context.connection.http.parserstatus.ChunkParsingState;
import kr.dogfoot.webserver.context.connection.http.proxy.HttpProxyState;
import kr.dogfoot.webserver.processor.AsyncSocketProcessor;
import kr.dogfoot.webserver.processor.util.HttpBodyConveyor;
import kr.dogfoot.webserver.processor.util.HttpBodySaver;
import kr.dogfoot.webserver.processor.util.ToAjpServer;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.util.Message;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class BodyReceiver extends AsyncSocketProcessor {
    public BodyReceiver(Server server) {
        super(server);
    }

    public void start() throws Exception {
        Message.debug("start Body Receiver ...");

        super.start();
    }

    @Override
    protected void onNewContext(Context context) {
        context.changeState(ContextState.ReceivingBody);

        process(context.clientConnection(), context, AfterProcess.Register);
    }

    @Override
    protected void setSelectionKey(SelectionKey key, Context context) {
        context.clientConnection().selectionKey(key);
    }

    @Override
    protected void onErrorInRegister(SocketChannel channel, Context context) {
        server.sendCloseSignalForClient(context);
    }

    @Override
    protected void setLastAccessTime(Context context, long currentTime) {
        context.clientConnection().lastAccessTime(currentTime);
    }

    @Override
    protected boolean isOverTimeoutForKeepAlive(Context context, long currentTime) {
        long interval = currentTime - context.clientConnection().lastAccessTime();
        return interval > serverProperties().keepAlive_timeout() * 1000;
    }

    @Override
    protected void closeConnectionForKeepAlive(Context context) {
        Message.debug(context, "Keep-Alive time-out event has occurred.");

        unregister(context.clientConnection().selectionKey());
        closeAllConnectionFor(context);
    }

    @Override
    protected void onReceive(SocketChannel channel, Context context, long currentTime) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                HttpClientConnection clientConn = context.clientConnection();
                ByteBuffer receiveBuffer = clientConn.receiveBuffer();
                int numRead = -2;

                try {
                    numRead = channel.read(receiveBuffer);
                } catch (Exception e) {
                    e.printStackTrace();
                    numRead = -2;
                }

                if (numRead == -2) {
                    Message.debug(context, "http read error");
                    closeAllConnectionFor(context);
                    return;
                }

                if (numRead > 0) {
                    setLastAccessTime(context, currentTime);
                }

                process(clientConn, context, AfterProcess.GotoSelf);
            }
        };
        server.objects().ioExecutorService().execute(r);
    }

    private void process(HttpClientConnection clientConn, Context context, AfterProcess afterProcess) {
        switch (clientConn.parserStatus().bodyParsingType()) {
            case ForDefaultProcessing:
                processForDefaultProcessing(clientConn, context, afterProcess);
                break;
            case ForAjpProxy:
                processForAjpProxy(clientConn, context, afterProcess);
                break;
            case ForHttpProxy:
                processForHttpProxy(clientConn, context, afterProcess);
                break;
        }
    }

    private void processForDefaultProcessing(HttpClientConnection clientConn, Context context, AfterProcess afterProcess) {
        boolean continueSend = true;

        clientConn.prepareReading();
        if (clientConn.readBuffer().hasRemaining()) {
            continueSend = parseBodyAndSaveUntilEnd(clientConn, context);
        }
        clientConn.prepareReceiving();

        if (continueSend == true) {
            if (afterProcess == AfterProcess.Register) {
                register(clientConn.channel(), context, SelectionKey.OP_READ);
            } else if (afterProcess == AfterProcess.GotoSelf) {
                gotoSelf(context);
            }
        } else {
            server.gotoPerformer(context);
        }

    }

    private boolean parseBodyAndSaveUntilEnd(HttpClientConnection clientConn, Context context) {
        if (context.request().hasContentLength()) {
            HttpBodySaver.saveAsMuchContentLength(clientConn, context.request());
            return clientConn.parserStatus().hasRemainingReadBodySize();
        } else if (context.request().isChunked()) {
            HttpBodySaver.saveUtilChunkEnd(clientConn, context.request());
            return clientConn.parserStatus().chunkState() != ChunkParsingState.ChunkEnd;
        }
        return false;
    }

    private void processForAjpProxy(HttpClientConnection connection, Context context, AfterProcess afterProcess) {
        boolean sent = false;

        connection.prepareReading();
        if (connection.readBuffer().hasRemaining()) {
            sent = parseBodyAndMakeAjpBodyChunk(context);
        }
        connection.prepareReceiving();

        if (sent == true) {
            server.gotoAjpProxier(context);
        } else {
            if (afterProcess == AfterProcess.Register) {
                register(context.clientConnection().channel(), context, SelectionKey.OP_READ);
            } else if (afterProcess == AfterProcess.GotoSelf) {
                gotoSelf(context);
            }
        }
    }

    private boolean parseBodyAndMakeAjpBodyChunk(Context context) {
        if (context.clientConnection().readBuffer().hasRemaining() == false) {
            return false;
        }
        if (context.request().isChunked() == false) {
            return ToAjpServer.sendBodyChunkAsMuchContentLength(context, server);
        } else {
            return ToAjpServer.sendBodyChunkUntilChunkEnd(context, server);
        }
    }

    private void processForHttpProxy(HttpClientConnection clientConn, Context context, AfterProcess afterProcess) {
        boolean continueSend = true;

        clientConn.prepareReading();
        if (clientConn.readBuffer().hasRemaining()) {
            continueSend = parseBodyAndSendUntilEnd(clientConn, context);
        }
        clientConn.prepareReceiving();

        if (continueSend == true) {
            if (afterProcess == AfterProcess.Register) {
                register(clientConn.channel(), context, SelectionKey.OP_READ);
            } else if (afterProcess == AfterProcess.GotoSelf) {
                gotoSelf(context);
            }
        } else {
            context.httpProxy().changeState(HttpProxyState.ReceivingReply);
            server.gotoHttpProxier(context);
        }
    }

    private boolean parseBodyAndSendUntilEnd(HttpClientConnection clientConn, Context context) {
        if (context.request().hasContentLength()) {
            HttpBodyConveyor.conveyAsMuchContentLength(clientConn, context.httpProxy(), server);
            return clientConn.parserStatus().hasRemainingReadBodySize();
        } else if (context.request().isChunked()) {
            HttpBodyConveyor.conveyUtilChunkEnd(clientConn, context.httpProxy(), server);
            return clientConn.parserStatus().chunkState() != ChunkParsingState.ChunkEnd;
        }
        return false;
    }

    @Override
    public void terminate() throws Exception {
        super.terminate();

        Message.debug("terminate Body Receiver ...");
    }

}
