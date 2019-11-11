package kr.dogfoot.webserver.server;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.context.ContextManager;
import kr.dogfoot.webserver.context.connection.http.parserstatus.ParsingState;
import kr.dogfoot.webserver.httpMessage.reply.Reply;
import kr.dogfoot.webserver.processor.Processor;
import kr.dogfoot.webserver.processor.client.*;
import kr.dogfoot.webserver.processor.proxy.ProxyConnector;
import kr.dogfoot.webserver.processor.proxy.ajp.AjpProxier;
import kr.dogfoot.webserver.processor.proxy.http.HttpProxier;
import kr.dogfoot.webserver.server.buffersender.BufferSender;
import kr.dogfoot.webserver.server.host.Host;
import kr.dogfoot.webserver.server.object.ServerObjects;
import kr.dogfoot.webserver.server.timer.Timer;
import kr.dogfoot.webserver.util.ConfigFileLoader;
import kr.dogfoot.webserver.util.Message;

import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

public class Server implements Startable {
    private static final int DEFAULT_HOST_COUNT = 5;

    private ServerObjects serverObjects;

    private Host[] hosts;
    private int hostCount;

    private ClientListener listener;

    private SSLHandshaker[] handshakers;
    private RequestReceiver[] requestReceivers;
    private BodyReceiver[] bodyReceivers;
    private RequestPerformer[] performers;
    private ReplySender[] senders;
    private ProxyConnector[] proxyConnectors;
    private AjpProxier[] ajpProxiers;
    private HttpProxier[] httpProxiers;
    private BufferSender[] bufferSender;
    private AtomicInteger bufferSenderIndex;

    public Server() throws Exception {
        ConfigFileLoader.setConfigDirectory("/Users/neolord/WebServerHome/config");

        serverObjects = new ServerObjects();
        hosts = new Host[DEFAULT_HOST_COUNT];
        hostCount = 0;
    }

    @Override
    public void start() throws Exception {
        startTimer();
        initialize();

        createAndStartListner();
        createAndStartHandshaker();
        createAndStartRequestReceiver();
        createAndStartBodyReceiver();
        createAndStartPerformer();
        createAndStartSender();
        createAndStartProxyConnector();
        createAndStartAjpProxier();
        createAndStartHttpProxier();
        createAndStartBuffeSender();
    }

    private void startTimer() {
        timer().start();
    }

    private Timer timer() {
        return serverObjects.timer();
    }

    private void initialize() {
        serverObjects.initialize();

        for (Host h : hosts) {
            if (h != null) {
                h.initialize();
            }
        }
    }

    private void createAndStartListner() throws Exception {
        listener = new ClientListener(this);
        listener.start();

        Message.debug("start Client Listener ...");
    }

    private void createAndStartHandshaker() throws Exception {
        int count  = serverObjects.properties().countOfSSLHandshaker();

        handshakers = new SSLHandshaker[count];
        for (int index = 0; index < count; index++) {
            handshakers[index] = new SSLHandshaker(this);
            handshakers[index].start();
        }

        Message.debug("start SSL Handshaker ...");
    }

    private void createAndStartRequestReceiver() throws Exception {
        int count = serverObjects.properties().countOfRequestReceiver();

        requestReceivers = new RequestReceiver[count];
        for (int index = 0; index < count; index++) {
            requestReceivers[index] = new RequestReceiver(this);
            requestReceivers[index].start();
        }

        Message.debug("start Request Receiver ...");
    }

    private void createAndStartBodyReceiver() throws Exception {
        int count = serverObjects.properties().countOfBodyReceiver();

        bodyReceivers = new BodyReceiver[count];
        for (int index = 0; index < count; index++) {
            bodyReceivers[index] = new BodyReceiver(this);
            bodyReceivers[index].start();
        }

        Message.debug("start Body Receiver ...");
    }

    private void createAndStartPerformer() throws Exception {
        int count = serverObjects.properties().countOfRequestPerformer();

        performers = new RequestPerformer[count];
        for (int index = 0; index < count; index++) {
            performers[index] = new RequestPerformer(this);
            performers[index].start();
        }

        Message.debug("start Request Performer ...");
    }

    private void createAndStartSender() throws Exception {
        int count = serverObjects.properties().countOfReplySender();

        senders = new ReplySender[count];
        for (int index = 0; index < count; index++) {
            senders[index] = new ReplySender(this);
            senders[index].start();
        }

        Message.debug("start Reply Sender ...");
    }

    private void createAndStartProxyConnector() throws Exception {
        int count = serverObjects.properties().countOfProxyConnector();

        proxyConnectors = new ProxyConnector[count];
        for (int index = 0; index < count; index++) {
            proxyConnectors[index] = new ProxyConnector(this);
            proxyConnectors[index].start();
        }

        Message.debug("start Proxy Connector ...");
    }

    private void createAndStartAjpProxier() throws Exception {
        int count = serverObjects.properties().countOfAjpProxier();

        ajpProxiers = new AjpProxier[count];
        for (int index = 0; index < count; index++) {
            ajpProxiers[index] = new AjpProxier(this);
            ajpProxiers[index].start();
        }

        Message.debug("start Ajp Proxier ...");
    }

    private void createAndStartHttpProxier() throws Exception {
        int count = serverObjects.properties().countOfHttpProxier();

        httpProxiers = new HttpProxier[count];
        for (int index = 0; index < count; index++) {
            httpProxiers[index] = new HttpProxier(this);
            httpProxiers[index].start();
        }

        Message.debug("start Http Proxier ...");
    }

    private void createAndStartBuffeSender() throws Exception {
        int count = serverObjects.properties().countOfBufferSender();
        bufferSender = new BufferSender[count];
        for (int index = 0; index < count; index++) {
            bufferSender[index] = new BufferSender(this);
            bufferSender[index].start();
        }
        bufferSenderIndex = new AtomicInteger(0);

        Message.debug("start Buffer Sender ...");
    }

    @Override
    public void terminate() throws Exception {
        timer().terminate();

        terminateListener();
        terminateHandshaker();
        terminateRequestReceiver();
        terminateBodyReceiver();
        terminatePerformer();
        terminateSender();
        terminateProxyConnector();
        terminateAjpProxier();
        terminateHttpProxier();
        terminateBufferSender();

        contextManager().releaseAll();

        Message.debug("terminate Server...");
    }

    private void terminateListener() throws Exception {
        listener.terminate();

        Message.debug("terminate Client Listener ...");
    }


    private void terminateHandshaker() throws Exception {
        int count = serverObjects.properties().countOfSSLHandshaker();
        for (int index = 0; index < count; index++) {
            handshakers[index].terminate();
            handshakers[index] = null;
        }

        Message.debug("terminate SSL Handshaker ...");
    }

    private void terminateRequestReceiver() throws Exception {
        int count = serverObjects.properties().countOfRequestReceiver();
        for (int index = 0; index < count; index++) {
            requestReceivers[index].terminate();
            requestReceivers[index] = null;
        }

        Message.debug("terminate Request Receiver ...");
    }

    private void terminateBodyReceiver() throws Exception {
        int count = serverObjects.properties().countOfBodyReceiver();
        for (int index = 0; index < count; index++) {
            bodyReceivers[index].terminate();
            bodyReceivers[index] = null;
        }

        Message.debug("terminate Body Receiver ...");
    }

    private void terminatePerformer() throws Exception {
        int count = serverObjects.properties().countOfRequestPerformer();
        for (int index = 0; index < count; index++) {
            performers[index].terminate();
            performers[index] = null;
        }

        Message.debug("terminate Request Performer ...");
    }

    private void terminateSender() throws Exception {
        int count = serverObjects.properties().countOfReplySender();
        for (int index = 0; index < count; index++) {
            senders[index].terminate();
            senders[index] = null;
        }

        Message.debug("terminate Reply Sender ...");
    }

    private void terminateProxyConnector() throws Exception {
        int count = serverObjects.properties().countOfProxyConnector();
        for (int index = 0; index < count; index++) {
             proxyConnectors[index].terminate();
        }

        Message.debug("terminate Proxy Connector ...");
    }

    private void terminateAjpProxier() throws Exception {
        int count = serverObjects.properties().countOfAjpProxier();
        for (int index = 0; index < count; index++) {
            ajpProxiers[index].terminate();
            ajpProxiers[index] = null;
        }

        Message.debug("terminate Ajp Proxier ...");
    }

    private void terminateHttpProxier() throws Exception {
        int count = serverObjects.properties().countOfHttpProxier();
        for (int index = 0; index < count; index++) {
            httpProxiers[index].terminate();
            httpProxiers[index] = null;
        }

        Message.debug("terminate Http Proxier ...");
    }

    private void terminateBufferSender() throws Exception {
        int count = serverObjects.properties().countOfBufferSender();
        for (int index = 0; index < count; index++) {
            bufferSender[index].terminate();
            bufferSender[index] = null;
        }

        Message.debug("terminate Buffer Sender ...");
    }

    private ContextManager contextManager() {
        return serverObjects.contextManager();
    }

    public Host addNewHost() {
        if (hosts.length <= hostCount) {
            Host[] newArray = new Host[hosts.length * 2];
            System.arraycopy(hosts, 0, newArray, 0, hosts.length);
            hosts = newArray;
        }

        hosts[hostCount++] = new Host(this);
        return hosts[hostCount - 1];
    }

    public Host findHost(String hostAddr, int port) {
        Host defaultHost = null;
        for (Host h : hosts) {
            if (h != null) {
                if (h.isMatched(hostAddr, port)) {
                    return h;
                }

                if (defaultHost == null || h.defaultHost()) {
                    defaultHost = h;
                }
            }
        }
        return defaultHost;
    }

    public Host[] hosts() {
        return hosts;
    }

    public ClientListener listener() {
        return listener;
    }

    public void gotoSSLHandshaker(Context context) {
        appropriateProccsser(handshakers).prepareContext(context);
    }

    private Processor appropriateProccsser(Processor[] processors) {
        Processor p = processors[0];
        for (Processor p2 : processors) {
            if (p2.waitContextCount() == 0) {
                return p2;
            } else if (p.waitContextCount() > p2.waitContextCount()) {
                p = p2;
            }
        }
        return p;
    }

    public void gotoRequestReceiver(Context context) {
        Processor p = appropriateProccsser(requestReceivers);
        p.prepareContext(context);
    }

    public void gotoBodyReceiver(Context context) {
        Processor p = appropriateProccsser(bodyReceivers);
        p.prepareContext(context);
    }

    public void gotoPerformer(Context context) {
        Processor p = appropriateProccsser(performers);
        p.prepareContext(context);
    }

    public void gotoSender(Context context) {
        Processor p = appropriateProccsser(senders);
        p.prepareContext(context);
    }

    public BufferSender bufferSender() {
        if (bufferSenderIndex.compareAndSet(bufferSender.length - 1, 0)) {
            return bufferSender[0];
        } else {
            return bufferSender[bufferSenderIndex.incrementAndGet()];
        }
    }

    public void gotoProxyConnector(Context context) {
        appropriateProccsser(proxyConnectors).prepareContext(context);
    }

    public void gotoAjpProxier(Context context) {
        appropriateProccsser(ajpProxiers).prepareContext(context);
    }

    public void gotoHttpProxier(Context context) {
        appropriateProccsser(httpProxiers).prepareContext(context);
    }


    public void sendBufferToClient(Context context, ByteBuffer buffer, boolean willRelease) {
        objects().sendBufferStorage().addForClient(context, buffer, willRelease);
        context.bufferSender().notifyStoring(context.clientConnection().channel());
    }

    public void sendCloseSignalForClient(Context context) {
        objects().sendBufferStorage().addForClientClose(context);
        context.bufferSender().notifyStoring(context.clientConnection().channel());
    }

    public void sendReleaseSignalForClient(Context context) {
        objects().sendBufferStorage().addForClientRelease(context);
        context.bufferSender().notifyStoring(context.clientConnection().channel());
    }

    public void sendBufferToAjpServer(Context context, ByteBuffer buffer, boolean willRelease) {
        objects().sendBufferStorage().addForAjpServer(context, buffer, willRelease);
        context.bufferSender().notifyStoring(context.ajpProxy().channel());
    }

    public void sendCloseSignalForAjpServer(Context context) {
        objects().sendBufferStorage().addForAjpServerClose(context);
        context.bufferSender().notifyStoring(context.ajpProxy().channel());
    }

    public void sendBufferToHttpServer(Context context, ByteBuffer buffer, boolean willRelease) {
        objects().sendBufferStorage().addForHttpServer(context, buffer, willRelease);
        context.bufferSender().notifyStoring(context.httpProxy().channel());
    }

    public void sendCloseSignalForHttpServer(Context context) {
        objects().sendBufferStorage().addForHttpServerClose(context);
        context.bufferSender().notifyStoring(context.httpProxy().channel());
    }

    public ServerObjects objects() {
        return serverObjects;
    }
}
