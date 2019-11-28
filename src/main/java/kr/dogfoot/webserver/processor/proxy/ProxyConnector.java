package kr.dogfoot.webserver.processor.proxy;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.context.ContextState;
import kr.dogfoot.webserver.context.connection.Connection;
import kr.dogfoot.webserver.context.connection.ajp.AjpProxyState;
import kr.dogfoot.webserver.context.connection.http.parserstatus.ParsingState;
import kr.dogfoot.webserver.context.connection.http.proxy.HttpProxyState;
import kr.dogfoot.webserver.processor.AsyncSocketProcessor;
import kr.dogfoot.webserver.processor.Processor;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.server.host.proxy_info.BackendServerInfo;
import kr.dogfoot.webserver.server.host.proxy_info.Protocol;
import kr.dogfoot.webserver.util.Message;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Date;

public class ProxyConnector extends AsyncSocketProcessor {
    private static int ProxyConnectorID = 0;

    public ProxyConnector(Server server) {
        super(server, ProxyConnectorID++, 0);
    }

    @Override
    protected void onNewContext(Context context) {
        server.objects().executorForProxyConnecting()
                .execute(() -> {
                    context.changeState(ContextState.ConnectingProxier);
                    tryToConnect(context);
                });
    }

    private void tryToConnect(Context context) {
        SocketChannel channel = null;

        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.setOption(StandardSocketOptions.TCP_NODELAY, true)
                    .setOption(StandardSocketOptions.SO_LINGER, 0)
                    .setOption(StandardSocketOptions.SO_REUSEADDR, true);

            if (register(channel, context, SelectionKey.OP_CONNECT)) {
                BackendServerInfo proxyBackendServerInfo  = context.proxyBackendServerInfo();
                channel.connect(proxyBackendServerInfo.socketAddress());
            }
        } catch (IOException e) {
            e.printStackTrace();

            unregister(getConnection(context).selectionKey());
            releaseAndClose(context);
            sendErrorReplyToClient(context);
        }
    }

    private Connection getConnection(Context context) {
        if (context.proxyProtocol() == Protocol.Ajp13) {
            return context.ajpProxy();
        } else if (context.proxyProtocol() == Protocol.Http) {
            return context.httpProxy();
        }
        return null;
    }

    private void releaseAndClose(Context context) {
        if (context.proxyProtocol() == Protocol.Ajp13) {
            ajpProxyConnectionManager().releaseAndClose(context);
        } else if (context.proxyProtocol() == Protocol.Http) {
            httpProxyConnectionManager().releaseAndClose(context);
        }
    }

    @Override
    protected void setSelectionKey(SelectionKey key, Context context) {
        if (context.proxyProtocol() == Protocol.Ajp13) {
            context.ajpProxy().selectionKey(key);
        } else if (context.proxyProtocol() == Protocol.Http) {
            context.httpProxy().selectionKey(key);
        }
    }

    @Override
    protected void onErrorInRegister(SocketChannel channel, Context context) {
        releaseAndClose(context);
        sendErrorReplyToClient(context);
    }

    private void sendErrorReplyToClient(Context context) {
        if (context.proxyProtocol() == Protocol.Ajp13) {
            context.reply(replyMaker().get_500CannotConnectWAS());
        } else {
            context.reply(replyMaker().get_500CannotConnectWS());
        }
        context.clientConnection().senderStatus().reset();
        server.gotoSender(context);
    }

    @Override
    protected void setLastAccessTime(Context context, long currentTime) {
        if (context.proxyProtocol() == Protocol.Ajp13) {
            context.ajpProxy().lastAccessTime(currentTime);
        } else if (context.proxyProtocol() == Protocol.Http) {
            context.httpProxy().lastAccessTime(currentTime);
        }
    }

    @Override
    protected void onConnect(SocketChannel channel, Context context, long currentTime) {
        try {
            if (channel.finishConnect()) {
                setLastAccessTime(context, currentTime);

                context.proxyBackendServerInfo().increaseConnectCount();

                if (context.proxyProtocol() == Protocol.Ajp13) {
                    Message.debug(context.ajpProxy(), "connect Ajp Proxier.");

                    context.ajpProxy().channel(channel);
                    context.ajpProxy().changeState(AjpProxyState.Idle);

                    server.gotoAjpProxier(context);
                } else if (context.proxyProtocol() == Protocol.Http) {
                    Message.debug(context.httpProxy(), "connect Http Proxier.");

                    context.httpProxy().channel(channel);
                    context.httpProxy().changeState(HttpProxyState.Idle);

                    server.gotoHttpProxier(context);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();

            releaseAndClose(context);
            sendErrorReplyToClient(context);
        }
    }
}
