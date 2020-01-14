package kr.dogfoot.webserver.server.object;

import kr.dogfoot.webserver.context.ContextManager;
import kr.dogfoot.webserver.context.connection.ajp.AjpProxyConnectionManager;
import kr.dogfoot.webserver.context.connection.http.client.ClientConnectionManager;
import kr.dogfoot.webserver.context.connection.http.proxy.HttpProxyConnectionManager;
import kr.dogfoot.webserver.httpMessage.response.maker.ResponseMaker;
import kr.dogfoot.webserver.server.timer.Timer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerObjects {
    private ServerProperties serverProperties;

    private ContextManager contextManager;
    private ClientConnectionManager clientConnectionManager;
    private AjpProxyConnectionManager ajpProxyConnectionManager;
    private HttpProxyConnectionManager httpProxyConnectionManager;

    private BufferManager bufferManager;

    private ExecutorService executorForSSLHandshaking;
    private ExecutorService executorForRequestReceiving;
    private ExecutorService executorForBodyReceiving;
    private ExecutorService executorForRequestPerforming;
    private ExecutorService executorForResponseSending;
    private ExecutorService executorForFileReading;
    private ExecutorService executorForBufferSending;
    private ExecutorService executorForProxyConnecting;
    private ExecutorService executorForAjpProxing;
    private ExecutorService executorForHttpProxing;

    private DefinedMediaTypeManager defaultMediaTypeManager;
    private ResponseMaker responseMaker;
    private Timer timer;

    public ServerObjects() {
        serverProperties = new ServerProperties();

        timer = new Timer();

        contextManager = new ContextManager();
        clientConnectionManager = new ClientConnectionManager();
        ajpProxyConnectionManager = new AjpProxyConnectionManager(timer);
        httpProxyConnectionManager = new HttpProxyConnectionManager(timer);

        bufferManager = new BufferManager();

        defaultMediaTypeManager = new DefinedMediaTypeManager();
        responseMaker = new ResponseMaker(serverProperties);
    }

    public void initialize() {
        executorForSSLHandshaking = Executors.newFixedThreadPool(serverProperties.pooledThreadCount().ssl_handshaking());
        executorForRequestReceiving = Executors.newFixedThreadPool(serverProperties.pooledThreadCount().request_receiving());
        executorForBodyReceiving = Executors.newFixedThreadPool(serverProperties.pooledThreadCount().body_receiving());;
        executorForRequestPerforming = Executors.newFixedThreadPool(serverProperties.pooledThreadCount().request_performing());
        executorForResponseSending = Executors.newFixedThreadPool(serverProperties.pooledThreadCount().response_sending());
        executorForFileReading = Executors.newFixedThreadPool(serverProperties.pooledThreadCount().file_reading());
        executorForBufferSending = Executors.newFixedThreadPool(serverProperties.pooledThreadCount().buffer_sending());
        executorForProxyConnecting = Executors.newFixedThreadPool(serverProperties.pooledThreadCount().proxy_connecting());
        executorForAjpProxing = Executors.newFixedThreadPool(serverProperties.pooledThreadCount().ajp_proxing());
        executorForHttpProxing = Executors.newFixedThreadPool(serverProperties.pooledThreadCount().http_proxing());
    }

    public ServerProperties properties() {
        return serverProperties;
    }

    public Timer timer() {
        return timer;
    }

    public ContextManager contextManager() {
        return contextManager;
    }

    public ClientConnectionManager clientConnectionManager() {
        return clientConnectionManager;
    }

    public AjpProxyConnectionManager ajpProxyConnectionManager() {
        return ajpProxyConnectionManager;
    }

    public HttpProxyConnectionManager httpProxyConnectionManager() {
        return httpProxyConnectionManager;
    }

    public BufferManager bufferManager() {
        return bufferManager;
    }

    public ExecutorService executorForSSLHandshaking() {
        return executorForSSLHandshaking;
    }

    public ExecutorService executorForRequestReceiving() {
        return executorForRequestReceiving;
    }

    public ExecutorService executorForBodyReceiving() {
        return executorForBodyReceiving;
    }

    public ExecutorService executorForRequestPerforming() {
        return executorForRequestPerforming;
    }

    public ExecutorService executorForResponseSending() {
        return executorForResponseSending;
    }

    public ExecutorService executorForFileReading() {
        return executorForFileReading;
    }

    public ExecutorService executorForBufferSending() {
        return executorForBufferSending;
    }

    public ExecutorService executorForProxyConnecting() {
        return executorForProxyConnecting;
    }

    public ExecutorService executorForAjpProxing() {
        return executorForAjpProxing;
    }

    public ExecutorService executorForHttpProxing() {
        return executorForHttpProxing;
    }

    public DefinedMediaTypeManager defaultMediaTypeManager() {
        return defaultMediaTypeManager;
    }

    public ResponseMaker responseMaker() {
        return responseMaker;
    }
}

