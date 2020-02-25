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
import kr.dogfoot.webserver.util.Message;

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
    private ResponseSender sender;
    private ProxyConnector proxyConnector;
    private AjpProxier ajpProxier;
    private HttpProxier httpProxier;
    private BufferSender bufferSender;

    public Server() {
        serverObjects = new ServerObjects();
        hosts = new Host[DEFAULT_HOST_COUNT];
        hostCount = 0;
    }

    @Override
    public void start() throws Exception {
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
        handshaker = new SSLHandshaker(this);
        handshaker.start();

        Message.debug("start SSL Handshaker ...");
    }

    private void createAndStartRequestReceiver() throws Exception {
        requestReceiver = new RequestReceiver(this);
        requestReceiver.start();

        Message.debug("start Request Receiver ...");
    }

    private void createAndStartBodyReceiver() throws Exception {
        bodyReceiver = new BodyReceiver(this);
        bodyReceiver.start();

        Message.debug("start Body Receiver ...");
    }

    private void createAndStartPerformer() throws Exception {
        performer = new RequestPerformer(this);
        performer.start();

        Message.debug("start Request Performer ...");
    }

    private void createAndStartSender() throws Exception {
        sender = new ResponseSender(this);
        sender.start();

        Message.debug("start Response Sender ...");
    }

    private void createAndStartProxyConnector() throws Exception {
        proxyConnector = new ProxyConnector(this);
        proxyConnector.start();

        Message.debug("start Proxy Connector ...");
    }

    private void createAndStartAjpProxier() throws Exception {
        ajpProxier = new AjpProxier(this);
        ajpProxier.start();

        Message.debug("start Ajp Proxier ...");
    }

    private void createAndStartHttpProxier() throws Exception {
        httpProxier = new HttpProxier(this);
        httpProxier.start();

        Message.debug("start Http Proxier ...");
    }

    private void createAndStartBuffeSender() throws Exception {
        bufferSender = new BufferSender(this);
        bufferSender.start();

        Message.debug("start Buffer Sender ...");
    }

    @Override
    public void terminate() throws Exception {
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

        serverObjects.terminate();

        Message.debug("terminate Server...");
    }

    private void terminateListener() throws Exception {
        listener.terminate();
        listener = null;

        Message.debug("terminate Client Listener ...");
    }


    private void terminateHandshaker() throws Exception {
        handshaker.terminate();
        handshaker = null;

        Message.debug("terminate SSL Handshaker ...");
    }

    private void terminateRequestReceiver() throws Exception {
        requestReceiver.terminate();
        requestReceiver = null;

        Message.debug("terminate Request Receiver ...");
    }

    private void terminateBodyReceiver() throws Exception {
        bodyReceiver.terminate();
        bodyReceiver = null;

        Message.debug("terminate Body Receiver ...");
    }

    private void terminatePerformer() throws Exception {
        performer.terminate();
        performer = null;

        Message.debug("terminate Request Performer ...");
    }

    private void terminateSender() throws Exception {
        sender.terminate();
        sender = null;

        Message.debug("terminate Response Sender ...");
    }

    private void terminateProxyConnector() throws Exception {
        proxyConnector.terminate();
        proxyConnector = null;

        Message.debug("terminate Proxy Connector ...");
    }

    private void terminateAjpProxier() throws Exception {
        ajpProxier.terminate();
        ajpProxier = null;

        Message.debug("terminate Ajp Proxier ...");
    }

    private void terminateHttpProxier() throws Exception {
        httpProxier.terminate();
        httpProxier = null;

        Message.debug("terminate Http Proxier ...");
    }

    private void terminateBufferSender() throws Exception {
        bufferSender.terminate();
        bufferSender = null;

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
}
