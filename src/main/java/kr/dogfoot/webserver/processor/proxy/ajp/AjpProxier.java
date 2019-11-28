package kr.dogfoot.webserver.processor.proxy.ajp;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.context.ContextState;
import kr.dogfoot.webserver.context.connection.ajp.AjpProxyConnection;
import kr.dogfoot.webserver.context.connection.ajp.AjpProxyState;
import kr.dogfoot.webserver.context.connection.http.parserstatus.BodyParsingType;
import kr.dogfoot.webserver.context.connection.http.parserstatus.ChunkParsingState;
import kr.dogfoot.webserver.httpMessage.reply.Reply;
import kr.dogfoot.webserver.parser.AjpReplyParser;
import kr.dogfoot.webserver.processor.AsyncSocketProcessor;
import kr.dogfoot.webserver.processor.util.ToAjpServer;
import kr.dogfoot.webserver.processor.util.ToClient;
import kr.dogfoot.webserver.processor.util.ToClientCommon;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.util.Message;
import kr.dogfoot.webserver.util.http.HttpString;
import kr.dogfoot.webserver.util.message.ajp.AjpPacketType;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class AjpProxier extends AsyncSocketProcessor {
    private static int AjpProxierID = 0;
    public AjpProxier(Server server) {
        super(server, AjpProxierID++);
    }

    @Override
    protected void onNewContext(Context context) {
        context.changeState(ContextState.ProxyingAjp);
        AjpProxyConnection connection = context.ajpProxy();
        switch (connection.state()) {
            case Idle:
                onIdle(connection, context);
                break;
            case SendBody:
                onSendBody(connection, context);
                break;
            case ReceivePacketHeader:
            case ReceivePacketBody:
                register(connection.channel(), context, SelectionKey.OP_READ);
                break;
        }
    }

    private void onIdle(AjpProxyConnection connection, Context context) {
        Message.debug(connection, "send FORWARD_REQUEST message");
        ToAjpServer.sendForwardRequest(context, server);

        if (context.request().hasBody()) {
            context.clientConnection().parserStatus()
                    .prepareBodyParsing(BodyParsingType.ForAjpProxy, context.request().contentLength());
        }

        if (context.request().hasBody() && context.request().isChunked() == false) {
            sendRequestBodyChunkByReceiver(context);
        } else {
            connection.changeState(AjpProxyState.ReceivePacketHeader);

            register(connection.channel(), context, SelectionKey.OP_READ);
        }
    }

    private void sendRequestBodyChunkByReceiver(Context context) {
        context.ajpProxy().changeState(AjpProxyState.SendBody);
        server.gotoBodyReceiver(context);
    }

    private void onSendBody(AjpProxyConnection connection, Context context) {
        connection.changeState(AjpProxyState.ReceivePacketHeader);
        register(connection.channel(), context, SelectionKey.OP_READ);
    }

    @Override
    protected void setSelectionKey(SelectionKey key, Context context) {
        context.ajpProxy().selectionKey(key);
    }

    @Override
    protected void onErrorInRegister(SocketChannel channel, Context context) {
        sendErrorReplyToClient(context);
        bufferSender().sendCloseSignalForAjpServer(context);
    }

    private void sendErrorReplyToClient(Context context) {
        context.reply(replyMaker().get_500DisconnectWAS());
        context.clientConnection().senderStatus().reset();
        server.gotoSender(context);
    }

    @Override
    protected void setLastAccessTime(Context context, long currentTime) {
        context.ajpProxy().lastAccessTime(currentTime);
    }

    @Override
    protected boolean isOverTimeoutForKeepAlive(Context context, long currentTime) {
        long interval = currentTime - context.ajpProxy().lastAccessTime();
        return interval > context.ajpProxy().backendServerInfo().keepAlive_timeout() * 1000;
    }

    @Override
    protected void closeConnectionForKeepAlive(Context context, boolean willUnregister) {
        if (willUnregister == true) {
            unregister(context.ajpProxy().selectionKey());
        }
        bufferSender().sendCloseSignalForAjpServer(context);
    }

    @Override
    protected void onReceive(SocketChannel channel, Context context, long currentTime) {
        server.objects().executorForAjpProxing()
                .execute(() -> {
                    AjpProxyConnection connection = context.ajpProxy();

                    int numRead = -2;
                    try {
                        numRead = channel.read(connection.receiveBuffer());
                    } catch (Exception e) {
                        e.printStackTrace();

                        numRead = -2;
                    }

                    if (numRead == -2) {
                        Message.debug(connection, "read error from ajp proxy server.");

                        sendErrorReplyToClient(context);
                        bufferSender().sendCloseSignalForAjpServer(context);
                        return;
                    }

                    if (numRead > 0) {
                        setLastAccessTime(context, currentTime);
                    }

                    connection.receiveBuffer().flip();
                    ProcessResult result = parseAndProcessAjpMessage(context);
                    connection.receiveBuffer().compact();

                    switch (result) {
                        case MoreReceive:
                            gotoSelf(context);
                            break;
                        case SendRequestBodyChunkByReceiver:
                            sendRequestBodyChunkByReceiver(context);
                            break;
                        case EndResponse:
                            nextRequest(context, false);
                            break;
                        case EndResponseAndReuse:
                            nextRequest(context, true);
                            break;
                    }
                });
    }

    private ProcessResult parseAndProcessAjpMessage(Context context) {
        ProcessResult result = null;
        AjpProxyConnection connection = context.ajpProxy();
        ByteBuffer buffer = connection.receiveBuffer();

        boolean breaking = false;
        while (breaking == false) {
            if (connection.state() == AjpProxyState.ReceivePacketHeader) {
                breaking = readPacketHeader(connection, buffer);
            }

            AjpPacketType type;

            if (breaking == false &&
                    connection.state() == AjpProxyState.ReceivePacketBody &&
                    buffer.remaining() >= connection.packetSize()) {
                byte code = buffer.get();
                type = AjpPacketType.fromCode(code);
                switch (type) {
                    case SendHeaders:
                        Message.debug(connection, "received SEND_HEADERS message");
                        onSendHeaders(context);
                        break;
                    case SendBodyChunk:
                        Message.debug(connection, "received SEND_BODY_CHUNK message");
                        onSendBodyChunk(context);
                        break;
                    case GetBodyChunk:
                        Message.debug(connection, "received GET_BODY_CHUNK message");
                        result = onGetBodyChunk(context);
                        if (result == ProcessResult.SendRequestBodyChunkByReceiver) {
                            breaking = true;
                        }
                        break;
                    case EndResponse:
                        Message.debug(connection, "received END_RESPONSE message");
                        result = onEndResponse(context);
                        breaking = true;
                        break;
                }
            } else {
                breaking = true;
                result = ProcessResult.MoreReceive;
            }
        }
        return result;
    }

    private boolean readPacketHeader(AjpProxyConnection connection, ByteBuffer buffer) {
        if (buffer.remaining() >= 4) {
            if (buffer.get() != HttpString.ReceivePacketHeaderA) {
                return true;
            }
            if (buffer.get() != HttpString.ReceivePacketHeaderB) {
                buffer.position(buffer.position() - 1);
                return true;
            }
            connection.packetSize(buffer.getShort());
            connection.changeState(AjpProxyState.ReceivePacketBody);
            return false;
        } else {
            return true;
        }
    }

    private void onSendHeaders(Context context) {
        AjpProxyConnection connection = context.ajpProxy();

        Reply reply = AjpReplyParser.sendHeadersToReply(connection.receiveBuffer(), replyMaker());
        connection.replyHasContentLength(reply.hasContentLength());

        ToClientCommon.sendStatusLine_Headers(context, reply, server);

        connection.changeState(AjpProxyState.ReceivePacketHeader);
    }

    private void onSendBodyChunk(Context context) {
        AjpProxyConnection connection = context.ajpProxy();

        short chunkSize = AjpReplyParser.readInt(connection.receiveBuffer());
        if (connection.replyHasContentLength() == false) {
            ToClient.sendBodyChunkSize(context, chunkSize, server);
        }

        ToClient.sendBodyChunk(context, chunkSize, server);
        connection.changeState(AjpProxyState.ReceivePacketHeader);
    }

    private ProcessResult onGetBodyChunk(Context context) {
        AjpProxyConnection connection = context.ajpProxy();

        short requestedLength = AjpReplyParser.readInt(connection.receiveBuffer());

        if (isOverBody(context)) {
            ToAjpServer.sendEmptyBodyChunk(context, server);
            connection.changeState(AjpProxyState.ReceivePacketHeader);
            return null;
        } else {
            return ProcessResult.SendRequestBodyChunkByReceiver;
        }
    }

    private ProcessResult onEndResponse(Context context) {
        AjpProxyConnection connection = context.ajpProxy();

        if (connection.replyHasContentLength() == false) {
            ToClient.sendLastBodyChunk(context, server);
        }

        boolean reuse = AjpReplyParser.readBool(connection.receiveBuffer());
        if (reuse) {
            return ProcessResult.EndResponseAndReuse;
        } else {
            return ProcessResult.EndResponse;
        }
    }

    private boolean isOverBody(Context context) {
        if (context.request().isChunked()) {
            return context.clientConnection().parserStatus().chunkState() == ChunkParsingState.ChunkEnd;
        } else {
            return context.clientConnection().parserStatus().hasRemainingReadBodySize() == false;
        }
    }

    private void nextRequest(Context context, boolean reuse) {
        if (reuse == true) {
            ajpProxyConnectionManager().idle(context);
        } else {
            bufferSender().sendCloseSignalForAjpServer(context);
        }

        if (context.request().isPersistentConnection() == true) {
            Message.debug(context, "Persistent Connection");

            context.resetForNextRequest();
            server.gotoRequestReceiver(context);
        } else {
            bufferSender().sendCloseSignalForClient(context);
        }

    }

    private enum ProcessResult {
        MoreReceive,
        SendRequestBodyChunkByReceiver,
        EndResponse,
        EndResponseAndReuse,
    }
}
