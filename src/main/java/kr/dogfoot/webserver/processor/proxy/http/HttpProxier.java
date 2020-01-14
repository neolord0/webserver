package kr.dogfoot.webserver.processor.proxy.http;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.context.ContextState;
import kr.dogfoot.webserver.context.connection.http.parserstatus.BodyParsingType;
import kr.dogfoot.webserver.context.connection.http.parserstatus.ChunkParsingState;
import kr.dogfoot.webserver.context.connection.http.parserstatus.ParsingState;
import kr.dogfoot.webserver.context.connection.http.proxy.HttpProxyConnection;
import kr.dogfoot.webserver.context.connection.http.proxy.HttpProxyState;
import kr.dogfoot.webserver.context.connection.http.senderstatus.ChunkedBodySendState;
import kr.dogfoot.webserver.httpMessage.response.Response;
import kr.dogfoot.webserver.httpMessage.response.StatusCode;
import kr.dogfoot.webserver.parser.HttpResponseParser;
import kr.dogfoot.webserver.processor.AsyncSocketProcessor;
import kr.dogfoot.webserver.processor.util.HttpBodyConveyor;
import kr.dogfoot.webserver.processor.util.RequestSaver;
import kr.dogfoot.webserver.processor.util.ToClientCommon;
import kr.dogfoot.webserver.processor.util.ToHttpServer;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.util.Message;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class HttpProxier extends AsyncSocketProcessor {
    private static int HttpProxierID = 0;
    private RequestSaver requestSaver;

    public HttpProxier(Server server) {
        super(server, HttpProxierID++);
        requestSaver = new RequestSaver();
    }

    @Override
    protected void onNewContext(Context context) {
        context.changeState(ContextState.ProxyingHttp);

        HttpProxyConnection connection = context.httpProxy();
        switch (connection.state()) {
            case Idle:
                onIdle(connection, context);
                break;
            case ReceivingResponse:
                onResponse(connection, context, AfterProcess.Register);
                break;
            case ReceivingResponseBody:
                onResponseBody(connection, context, AfterProcess.Register);
                break;
        }
    }

    private void onIdle(HttpProxyConnection connection, Context context) {
        context.response(new Response());

        Message.debug(connection, "send request to http proxy server");
        ToHttpServer.sendRequest(context, server);

        requestSaver.save(connection.channel(), context.request());

        if (context.request().hasBody() && context.request().hasExpect100Continue() == false)  {
            sendRequestBodyByReceiver(context);
        } else {
            connection.changeState(HttpProxyState.ReceivingResponse);

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
        sendErrorResponseToClient(context);
        bufferSender().sendCloseSignalForHttpServer(context);
    }

    private void sendErrorResponseToClient(Context context) {
        context.response(responseMaker().get_500DisconnectWS());
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
        return interval > context.httpProxy().backendServerInfo().keepAlive_timeout() * 1000;
    }

    @Override
    protected void closeConnectionForKeepAlive(Context context, boolean willUnRegister) {
        if (willUnRegister == true) {
            unregister(context.httpProxy().selectionKey());
        }
        bufferSender().sendCloseSignalForHttpServer(context);
    }

    @Override
    protected void onReceive(SocketChannel channel, Context context, long currentTime) {
        server.objects().executorForHttpProxing()
                .execute(() -> {
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
                        bufferSender().sendCloseSignalForHttpServer(context);
                        return;
                    }

                    if (numRead > 0) {
                        setLastAccessTime(context, currentTime);
                    }

                    switch (connection.state()) {
                        case ReceivingResponse:
                            onResponse(connection, context, AfterProcess.GotoSelf);
                            break;
                        case ReceivingResponseBody:
                            onResponseBody(connection, context, AfterProcess.GotoSelf);
                            break;
                    }
                });
    }

    private void onResponse(HttpProxyConnection connection, Context context, AfterProcess afterProcess) {
        connection.prepareReading();
        if (connection.readBuffer().hasRemaining()) {
            parseResponse(context);
        }
        connection.prepareReceiving();

        if (connection.parserStatus().state() == ParsingState.BodyStart) {
            ToClientCommon.sendStatusLine_Headers(context, server);
            context.response().request(requestSaver.get(connection.channel()));

            if (context.response().code() == StatusCode.Code100 && context.request().hasBody()) {
                sendRequestBodyByReceiver(context);
                connection.resetForNextRequest();
                return;
            }

            connection.senderStatus().reset();

            if (context.response().hasBody()) {
                connection.changeState(HttpProxyState.ReceivingResponseBody);

                context.clientConnection().senderStatus()
                        .changeChunkedBodySendState(ChunkedBodySendState.ChunkSize);
                connection.parserStatus()
                        .prepareBodyParsing(BodyParsingType.ForHttpProxy, context.response().contentLength());

                gotoSelf(context);
            } else {
                onResponseEnd(connection, context);
            }
            } else {
            if (afterProcess == AfterProcess.Register) {
                register(connection.channel(), context, SelectionKey.OP_READ);
            } else if (afterProcess == AfterProcess.GotoSelf) {
                gotoSelf(context);
            }
        }
    }

    private void parseResponse(Context context) {
        HttpProxyConnection connection = context.httpProxy();

        while (connection.reader().hasData() &&
                connection.parserStatus().state() != ParsingState.BodyStart) {
            HttpResponseParser.parse(connection);
        }
    }

    private void onResponseBody(HttpProxyConnection connection, Context context, AfterProcess afterProcess) {
        boolean continueSend = false;
        boolean hasParsed = false;

        connection.prepareReading();
        if (connection.readBuffer().hasRemaining()) {
            hasParsed = true;
            continueSend = parseAndSendResponseBody(connection, context);
        }
        connection.prepareReceiving();

        if (hasParsed && continueSend == false) {
            onResponseEnd(connection, context);
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

    private boolean parseAndSendResponseBody(HttpProxyConnection connection, Context context) {
        if (context.response().hasContentLength()) {
            HttpBodyConveyor.conveyAsMuchContentLength(context.httpProxy(), context.clientConnection(), server);
            return connection.parserStatus().hasRemainingReadBodySize();
        } else if (context.response().isChunked()) {
            HttpBodyConveyor.conveyUtilChunkEnd(context.httpProxy(), context.clientConnection(), server);
            return connection.parserStatus().chunkState() != ChunkParsingState.ChunkEnd;
        }
        return false;
    }

    private void onResponseEnd(HttpProxyConnection connection, Context context) {
        Message.debug(connection, "complete receive response_body from http proxy server and send to client");

        context.response().setResponseTimeToNow();
        System.out.println("response delay : " + context.response().response_delay());

        if (context.response().code().isError()) {
            bufferSender().sendCloseSignalForHttpServer(context);
            bufferSender().sendCloseSignalForClient(context);
        } else {
            if (context.response().hasKeepAlive()) {
                Message.debug(context, "Persistent Connection");

                connection.changeState(HttpProxyState.Idle);
                connection.resetForNextRequest();
            } else {
                connection.changeState(HttpProxyState.Close);
                
                bufferSender().sendCloseSignalForHttpServer(context);
            }

            context.resetForNextRequest();
            server.gotoRequestReceiver(context);
        }
    }
}
