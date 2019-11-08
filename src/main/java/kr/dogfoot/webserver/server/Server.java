package kr.dogfoot.webserver.server;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.context.ContextManager;
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

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Server implements Startable {
    private static final int DEFAULT_HOST_COUNT = 5;

    private ServerObjects serverObjects;

    private Host[] hosts;
    private int hostCount;

    private ClientListener listener;

    private SSLHandshaker handshaker;
    private RequestReceiver requestReceiver;
    private BodyReceiver bodyReceiver;
    private RequestPerformer performer;
    private ReplySender sender;
    private ProxyConnector proxyConnector;
    private AjpProxier ajpProxier;
    private HttpProxier httpProxier;
    private BufferSender bufferSender;

    public Server() throws Exception {
        ConfigFileLoader.setConfigDirectory("/Users/neolord/WebServerHome/config");

        serverObjects = new ServerObjects();
        hosts = new Host[DEFAULT_HOST_COUNT];
        hostCount = 0;
    }

    @Override
    public void start() throws Exception {
        startTimer();
        initializeHosts();

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

    private void initializeHosts() {
        for (Host h : hosts) {
            if (h != null) {
                h.initialize();
            }
        }
    }

    private void createAndStartListner() throws Exception {
        listener = new ClientListener(this);
        listener.start();
    }

    private void createAndStartHandshaker() throws Exception {
        handshaker = new SSLHandshaker(this);
        handshaker.start();
    }

    private void createAndStartRequestReceiver() throws Exception {
        requestReceiver = new RequestReceiver(this);
        requestReceiver.start();
    }

    private void createAndStartBodyReceiver() throws Exception {
        bodyReceiver = new BodyReceiver(this);
        bodyReceiver.start();
    }

    private void createAndStartPerformer() throws Exception {
        performer = new RequestPerformer(this);
        performer.start();
    }

    private void createAndStartSender() throws Exception {
        sender = new ReplySender(this);
        sender.start();
    }

    private void createAndStartProxyConnector() throws Exception {
        proxyConnector = new ProxyConnector(this);
        proxyConnector.start();
    }

    private void createAndStartAjpProxier() throws Exception {
        ajpProxier = new AjpProxier(this);
        ajpProxier.start();
    }

    private void createAndStartHttpProxier() throws Exception {
        httpProxier = new HttpProxier(this);
        httpProxier.start();
    }

    private void createAndStartBuffeSender() throws Exception {
        bufferSender = new BufferSender(this);
        bufferSender.start();
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
    }


    private void terminateHandshaker() throws Exception {
        handshaker.terminate();
    }

    private void terminateRequestReceiver() throws Exception {
        requestReceiver.terminate();
    }

    private void terminateBodyReceiver() throws Exception {
        bodyReceiver.terminate();
    }

    private void terminatePerformer() throws Exception {
        performer.terminate();
    }

    private void terminateSender() throws Exception {
        sender.terminate();
    }

    private void terminateProxyConnector() throws Exception {
        proxyConnector.terminate();
    }

    private void terminateAjpProxier() throws Exception {
        ajpProxier.terminate();
    }

    private void terminateHttpProxier() throws Exception {
        httpProxier.terminate();
    }

    private void terminateBufferSender() throws Exception {
        bufferSender.terminate();
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
        handshaker.prepareContext(context);
    }

    public void gotoRequestReceiver(Context context) {
        requestReceiver.prepareContext(context);
    }

    public void gotoBodyReceiver(Context context) {
        bodyReceiver.prepareContext(context);
    }

    public void gotoPerformer(Context context) {
        performer.prepareContext(context);
    }

    public void gotoSender(Context context) {
        sender.prepareContext(context);
    }

    public void gotoProxyConnector(Context context) {
        proxyConnector.prepareContext(context);
    }

    public void gotoAjpProxier(Context context) {
        ajpProxier.prepareContext(context);
    }

    public void gotoHttpProxier(Context context) {
        httpProxier.prepareContext(context);
    }

    public BufferSender bufferSender() {
        return bufferSender;
    }

    public ServerObjects objects() {
        return serverObjects;
    }

    public void sendBufferToClient(Context context, ByteBuffer buffer, boolean willRelease) {
        objects().sendBufferStorage().addForClient(context, buffer, willRelease);
        bufferSender.notifyStoring(context.clientConnection().channel());
    }

    public void sendCloseSignalForClient(Context context) {
        objects().sendBufferStorage().addForClientClose(context);
        bufferSender.notifyStoring(context.clientConnection().channel());
    }

    public void sendReleaseSignalForClient(Context context) {
        objects().sendBufferStorage().addForClientRelease(context);
        bufferSender.notifyStoring(context.clientConnection().channel());
    }

    public void sendBufferToAjpServer(Context context, ByteBuffer buffer, boolean willRelease) {
        objects().sendBufferStorage().addForAjpServer(context, buffer, willRelease);
        bufferSender.notifyStoring(context.ajpProxy().channel());
    }

    public void sendCloseSignalForAjpServer(Context context) {
        objects().sendBufferStorage().addForAjpServerClose(context);
        bufferSender.notifyStoring(context.ajpProxy().channel());
    }

    public void sendBufferToHttpServer(Context context, ByteBuffer buffer, boolean willRelease) {
        objects().sendBufferStorage().addForHttpServer(context, buffer, willRelease);
        bufferSender.notifyStoring(context.httpProxy().channel());
    }

    public void sendCloseSignalForHttpServer(Context context) {
        objects().sendBufferStorage().addForHttpServerClose(context);
        bufferSender.notifyStoring(context.httpProxy().channel());
    }
}
