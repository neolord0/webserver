package kr.dogfoot.webserver.processor.proxy.http;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.context.ContextState;
import kr.dogfoot.webserver.context.connection.http.parserstatus.BodyParsingType;
import kr.dogfoot.webserver.context.connection.http.parserstatus.ChunkParsingState;
import kr.dogfoot.webserver.context.connection.http.parserstatus.ParsingState;
import kr.dogfoot.webserver.context.connection.http.proxy.HttpProxyConnection;
import kr.dogfoot.webserver.context.connection.http.proxy.HttpProxyState;
import kr.dogfoot.webserver.context.connection.http.senderstatus.ChunkedBodySendState;
import kr.dogfoot.webserver.httpMessage.reply.Reply;
import kr.dogfoot.webserver.httpMessage.reply.ReplyCode;
import kr.dogfoot.webserver.parser.HttpReplyParser;
import kr.dogfoot.webserver.processor.AsyncSocketProcessor;
import kr.dogfoot.webserver.processor.util.HttpBodyConveyor;
import kr.dogfoot.webserver.processor.util.ToClientCommon;
import kr.dogfoot.webserver.processor.util.ToHttpServer;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.util.Message;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class HttpProxier extends AsyncSocketProcessor {
    private static int HttpProxierID = 0;

    public HttpProxier(Server server) {
        super(server, HttpProxierID++);
    }

    @Override
    protected void onNewContext(Context context) {
        context.changeState(ContextState.ProxyingHttp);

        HttpProxyConnection connection = context.httpProxy();
        switch (connection.state()) {
            case Idle:
                onIdle(connection, context);
                break;
            case ReceivingReply:
                onReply(connection, context, AfterProcess.Register);
                break;
            case ReceivingReplyBody:
                onReplyBody(connection, context, AfterProcess.Register);
                break;
        }
    }

    private void onIdle(HttpProxyConnection connection, Context context) {
        context.reply(new Reply());

        Message.debug(connection, "send request to http proxy server");
        ToHttpServer.sendRequest(context, server);

        if (context.request().hasBody() && context.request().hasExpect100Continue() == false)  {
            sendRequestBodyByReceiver(context);
        } else {
            connection.changeState(HttpProxyState.ReceivingReply);

            register(connection.channel(), context, SelectionKey.OP_READ);
        }
    }

    private void sendRequestBodyByReceiver(Context context) {
        context.clientConnection().parserStatus()
                .prepareBodyParsing(BodyParsingType.ForHttpProxy, context.request().contentLength());

        server.gotoBodyReceiver(context);
    }

    @Override
    protected void setSelectionKey(SelectionKey key, Context context) {
        context.httpProxy().selectionKey(key);
    }

    @Override
    protected void onErrorInRegister(SocketChannel channel, Context context) {
        sendErrorReplyToClient(context);
        context.bufferSender().sendCloseSignalForHttpServer(context);
    }

    private void sendErrorReplyToClient(Context context) {
        context.reply(replyMaker().get_500DisconnectWS());
        context.clientConnection().senderStatus().reset();
        server.gotoSender(context);
    }

    @Override
    protected void setLastAccessTime(Context context, long currentTime) {
        context.httpProxy().lastAccessTime(currentTime);
    }

    @Override
    protected boolean isOverTimeoutForKeepAlive(Context context, long currentTime) {
        long interval = currentTime - context.httpProxy().lastAccessTime();
        return interval > context.backendServerInfo().keepAlive_timeout() * 1000;
    }

    @Override
    protected void closeConnectionForKeepAlive(Context context, boolean willUnRegister) {
        if (willUnRegister == true) {
            unregister(context.httpProxy().selectionKey());
        }
        context.bufferSender().sendCloseSignalForHttpServer(context);
    }

    @Override
    protected void onReceive(SocketChannel channel, Context context, long currentTime) {
        HttpProxyConnection connection = context.httpProxy();
        int numRead = -2;
        try {
            numRead = channel.read(connection.receiveBuffer());
        } catch (Exception e) {
            e.printStackTrace();

            numRead = -2;
        }

        if (numRead == -2) {
            Message.debug(connection, "read error from http proxy server.");

            connection.changeState(HttpProxyState.Close);
            context.bufferSender().sendCloseSignalForHttpServer(context);
            return;
        }

        if (numRead > 0) {
            setLastAccessTime(context, currentTime);
        }

        switch (connection.state()) {
            case ReceivingReply:
                onReply(connection, context, AfterProcess.GotoSelf);
                break;
            case ReceivingReplyBody:
                onReplyBody(connection, context, AfterProcess.GotoSelf);
                break;
        }
    }

    private void onReply(HttpProxyConnection connection, Context context, AfterProcess afterProcess) {
        connection.prepareReading();
        if (connection.readBuffer().hasRemaining()) {
            parseReply(context);
        }
        connection.prepareReceiving();

        if (connection.parserStatus().state() == ParsingState.BodyStart) {
            ToClientCommon.sendStatusLine_Headers(context, context.reply(), server);

            if (context.reply().code() == ReplyCode.Code100 && context.request().hasBody()) {
                sendRequestBodyByReceiver(context);
                connection.resetForNextRequest();
                return;
            }

            connection.senderStatus().reset();

            if (context.reply().hasBody()) {
                connection.changeState(HttpProxyState.ReceivingReplyBody);

                context.clientConnection().senderStatus()
                        .changeChunkedBodySendState(ChunkedBodySendState.ChunkSize);
                connection.parserStatus()
                        .prepareBodyParsing(BodyParsingType.ForHttpProxy, context.reply().contentLength());

                gotoSelf(context);
            } else {
                onReplyEnd(connection, context);
            }
            } else {
            if (afterProcess == AfterProcess.Register) {
                register(connection.channel(), context, SelectionKey.OP_READ);
            } else if (afterProcess == AfterProcess.GotoSelf) {
                gotoSelf(context);
            }
        }
    }

    private void parseReply(Context context) {
        HttpProxyConnection connection = context.httpProxy();

        while (connection.reader().hasData() &&
                connection.parserStatus().state() != ParsingState.BodyStart) {
            HttpReplyParser.parse(connection);
        }
    }

    private void onReplyBody(HttpProxyConnection connection, Context context, AfterProcess afterProcess) {
        boolean continueSend = false;
        boolean hasParsed = false;

        connection.prepareReading();
        if (connection.readBuffer().hasRemaining()) {
            hasParsed = true;
            continueSend = parseAndSendReplyBody(connection, context);
        }
        connection.prepareReceiving();

        if (hasParsed && continueSend == false) {
            onReplyEnd(connection, context);
        } else {
            if (hasParsed == false || continueSend == true) {
                if (afterProcess == AfterProcess.Register) {
                    register(connection.channel(), context, SelectionKey.OP_READ);
                } else if (afterProcess == AfterProcess.GotoSelf) {
                    gotoSelf(context);
                }
            }
        }
    }

    private boolean parseAndSendReplyBody(HttpProxyConnection connection, Context context) {
        if (context.reply().hasContentLength()) {
            HttpBodyConveyor.conveyAsMuchContentLength(context.httpProxy(), context.clientConnection(), server);
            return connection.parserStatus().hasRemainingReadBodySize();
        } else if (context.reply().isChunked()) {
            HttpBodyConveyor.conveyUtilChunkEnd(context.httpProxy(), context.clientConnection(), server);
            return connection.parserStatus().chunkState() != ChunkParsingState.ChunkEnd;
        }
        return false;
    }

    private void onReplyEnd(HttpProxyConnection connection, Context context) {
        Message.debug(connection, "complete receive reply_body from http proxy server and send to client");
        if (context.reply().code().isError()) {
            context.bufferSender().sendCloseSignalForHttpServer(context);
            context.bufferSender().sendCloseSignalForClient(context);
        } else {
            if (context.reply().hasKeepAlive()) {
                Message.debug(context, "Persistent Connection");

                connection.changeState(HttpProxyState.Idle);
                connection.resetForNextRequest();
            } else {
                connection.changeState(HttpProxyState.Close);
                context.proxyId(-1)
                        .backendServerInfo(null);

                context.bufferSender().sendCloseSignalForClient(context);
            }

            context.resetForNextRequest();
            server.gotoRequestReceiver(context);
        }
    }
}
