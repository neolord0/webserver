package kr.dogfoot.webserver.processor.proxy;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.context.ContextState;
import kr.dogfoot.webserver.context.connection.Connection;
import kr.dogfoot.webserver.context.connection.ajp.AjpProxyState;
import kr.dogfoot.webserver.context.connection.http.proxy.HttpProxyState;
import kr.dogfoot.webserver.processor.AsyncSocketProcessor;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.server.host.proxy_info.Protocol;
import kr.dogfoot.webserver.util.Message;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ProxyConnector extends AsyncSocketProcessor {
    public ProxyConnector(Server server) {
        super(server);
    }

    @Override
    public void start() throws Exception {
        Message.debug("start Proxy Connector ...");

        super.start();
    }

    @Override
    protected void onNewContext(Context context) {
        context.changeState(ContextState.ConnectingProxier);

        tryToConnect(context);
    }

    private void tryToConnect(Context context) {
        SocketChannel channel = null;

        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);

            if (register(channel, context, SelectionKey.OP_CONNECT)) {
                channel.connect(context.backendServerInfo().socketAddress());
            }
        } catch (IOException e) {
            e.printStackTrace();

            unregister(getConnection(context).selectionKey());
            releaseAndClose(context);
            sendErrorReplyToClient(context);
        }
    }

    private Connection getConnection(Context context) {
        if (context.backendServerInfo().isAjp()) {
            return context.ajpProxy();
        } else if (context.backendServerInfo().isHttp()) {
            return context.httpProxy();
        }
        return null;
    }


    private void releaseAndClose(Context context) {
        if (context.backendServerInfo().isAjp()) {
            ajpProxyConnectionManager().releaseAndClose(context);
        } else if (context.backendServerInfo().isHttp()) {
            httpProxyConnectionManager().releaseAndClose(context);
        }
    }

    @Override
    protected void setSelectionKey(SelectionKey key, Context context) {
        if (context.backendServerInfo().isAjp()) {
            context.ajpProxy().selectionKey(key);
        } else if (context.backendServerInfo().isHttp()) {
            context.httpProxy().selectionKey(key);
        }
    }

    @Override
    protected void onErrorInRegister(SocketChannel channel, Context context) {
        releaseAndClose(context);
        sendErrorReplyToClient(context);
    }

    private void sendErrorReplyToClient(Context context) {
        if (context.backendServerInfo().protocol() == Protocol.Ajp13) {
            context.reply(replyMaker().get_500CannotConnectWAS());
        } else {
            context.reply(replyMaker().get_500CannotConnectWS());
        }
        context.clientConnection().senderStatus().reset();
        server.gotoSender(context);
    }

    @Override
    protected void setLastAccessTime(Context context, long currentTime) {
        if (context.backendServerInfo().isAjp()) {
            context.ajpProxy().lastAccessTime(currentTime);
        } else {
            context.httpProxy().lastAccessTime(currentTime);
        }
    }

    @Override
    protected void onConnect(SocketChannel channel, Context context, long currentTime) {
        try {
            if (channel.finishConnect()) {
                setLastAccessTime(context, currentTime);

                if (context.backendServerInfo().isAjp()) {
                    Message.debug(context.ajpProxy(), "connect Ajp Proxier.");

                    context.ajpProxy().channel(channel);
                    context.ajpProxy().changeState(AjpProxyState.Idle);

                    server.gotoAjpProxier(context);
                } else {
                    Message.debug(context.httpProxy(), "connect Http Proxier.");

                    context.httpProxy().channel(channel);
                    context.httpProxy().changeState(HttpProxyState.Idle);

                    server.gotoHttpProxier(context);
                }
            }
        } catch (IOException e) {
            releaseAndClose(context);
            sendErrorReplyToClient(context);
        }
    }

    @Override
    public void terminate() throws Exception {
        super.terminate();

        Message.debug("terminate Proxy Connector ...");
    }
}
