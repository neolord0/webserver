package kr.dogfoot.webserver.processor.client;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.context.ContextState;
import kr.dogfoot.webserver.context.connection.http.client.HttpClientConnection;
import kr.dogfoot.webserver.context.connection.http.parserstatus.ParsingState;
import kr.dogfoot.webserver.parser.HttpRequestParser;
import kr.dogfoot.webserver.processor.AsyncSocketProcessor;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.util.Message;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class RequestReceiver extends AsyncSocketProcessor {
    private int closeCount = 0;

    public RequestReceiver(Server server) {
        super(server);
    }

    public void start() throws Exception {
        Message.debug("start Request Receiver ...");

        super.start();
    }

    @Override
    protected void onNewContext(Context context) {
        context.changeState(ContextState.ReceivingRequest);

        process(context.clientConnection(), context, AfterProcess.Register);
    }

    @Override
    protected void setSelectionKey(SelectionKey key, Context context) {
        context.clientConnection().selectionKey(key);
    }

    @Override
    protected void onErrorInRegister(SocketChannel channel, Context context) {
        closeAllConnectionFor(context);
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
        closeCount++;

        unregister(context.clientConnection().selectionKey());
        closeAllConnectionFor(context);
    }

    @Override
    protected void onReceive(SocketChannel channel, Context context, long currentTime) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                HttpClientConnection connection = context.clientConnection();
                int numRead = -2;

                try {
                    numRead = channel.read(connection.receiveBuffer());
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

                process(connection, context, AfterProcess.GotoSelf);
            }
        };
        server.objects().ioExecutorService().execute(r);
    }

    private void process(HttpClientConnection connection, Context context, AfterProcess afterProcess) {
        connection.prepareReading();
        parse(connection);
        connection.prepareReceiving();

        if (connection.parserStatus().state() == ParsingState.BodyStart) {
            server.gotoPerformer(context);
        } else {
            if (afterProcess == AfterProcess.GotoSelf) {
                gotoSelf(context);
            } else {
                register(connection.channel(), context, SelectionKey.OP_READ);
            }
        }
    }

    private void parse(HttpClientConnection connection) {
        if (connection.readBuffer().hasRemaining() == false) {
            return;
        }

        while (connection.reader().hasData() &&
                connection.parserStatus().state() != ParsingState.BodyStart) {
            HttpRequestParser.parse(connection);
        }
    }

    @Override
    public void terminate() throws Exception {
        super.terminate();

        Message.debug("terminate Request Receiver ...");
    }
}
