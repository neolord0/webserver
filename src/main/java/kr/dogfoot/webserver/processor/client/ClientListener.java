package kr.dogfoot.webserver.processor.client;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.context.connection.http.client.HttpClientConnection;
import kr.dogfoot.webserver.context.connection.http.parserstatus.ParsingState;
import kr.dogfoot.webserver.processor.AsyncSocketProcessor;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.server.host.Host;
import kr.dogfoot.webserver.util.Message;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class ClientListener extends AsyncSocketProcessor {
    private static int ClientListenerID = 0;

    private Vector<Integer> usedPorts;
    private HashMap<ServerSocketChannel, ServerSocketInfo> serverSocketInfos;
    private int acceptCount;


    public ClientListener(Server server) {
        super(server, ClientListenerID++);

        usedPorts = new Vector<Integer>();
        serverSocketInfos = new HashMap<ServerSocketChannel, ServerSocketInfo>();
        acceptCount = 0;
    }

    @Override
    public void start() throws Exception {
        nioSelector = Selector.open();
        running = true;

        listenAllHost();
        create_startSocketThread();
        acceptCount = 0;
    }

    private void listenAllHost() throws Exception {
        for (Host h : server.hosts()) {
            if (h != null) {
                listenServerSocket(h.port(), h.adjustSSL());
            }
        }
    }

    private void listenServerSocket(int port, boolean adjustSSL) throws Exception {
        if (usedPorts.contains(port)) {
            Message.log(port + " port already use");
            return;
        }

        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);

        ssc.socket().bind(new InetSocketAddress(port), 1024);
        ssc.register(nioSelector, SelectionKey.OP_ACCEPT);

        usedPorts.add(port);
        serverSocketInfos.put(ssc, new ServerSocketInfo(port, adjustSSL));

        Message.debug("Listen : " + port + " port");
    }

    @Override
    protected void onNewContext(Context context) {
        // anything
    }

    @Override
    protected void setSelectionKey(SelectionKey key, Context context) {
        // anything
    }

    @Override
    protected void onErrorInRegister(SocketChannel channel, Context context) {
        // anything
    }

    @Override
    protected void checkKeepAliveTimeout(long currentTime) {
        // anything
    }

    @Override
    protected void onAccept(ServerSocketChannel serverChannel, long currentTime) {
        SocketChannel channel = accept(serverChannel);
        if (channel != null) {
            ServerSocketInfo info = serverSocketInfos.get(serverChannel);

            HttpClientConnection conn = clientConnectionManager().pooledObject(channel, info.adjustSSL);
            conn.lastAccessTime(currentTime);

            Context context = contextManager().pooledObject();
            context.clientConnection(conn);

            if (info.adjustSSL) {
                server.gotoSSLHandshaker(context);
            } else {
                server.gotoRequestReceiver(context);
            }

            acceptCount++;
            Message.debug(context, "accept " + acceptCount);
        } else {
            System.out.println("fail to accept.");
        }
    }

    private synchronized SocketChannel accept(ServerSocketChannel serverChannel) {
        SocketChannel channel = null;
        try {
            channel = serverChannel.accept();
            if (channel != null) {
                channel.configureBlocking(false);
                channel.setOption(StandardSocketOptions.TCP_NODELAY, true)
                        .setOption(StandardSocketOptions.SO_LINGER, 0)
                        .setOption(StandardSocketOptions.SO_REUSEADDR, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
            channel = null;
        }
        return channel;
    }

    @Override
    public void terminate() throws Exception {
        running = false;

        closeServerSocketChannels();
        nioSelector.close();
    }

    private void closeServerSocketChannels() throws IOException {
        Iterator it = serverSocketInfos.keySet().iterator();
        while (it.hasNext()) {
            ServerSocketChannel ssc = (ServerSocketChannel) it.next();
            ssc.close();
        }
        serverSocketInfos.clear();
    }

    private class ServerSocketInfo {
        int port;
        boolean adjustSSL;

        ServerSocketInfo(int port, boolean adjustSSL) {
            this.port = port;
            this.adjustSSL = adjustSSL;
        }
    }
}
