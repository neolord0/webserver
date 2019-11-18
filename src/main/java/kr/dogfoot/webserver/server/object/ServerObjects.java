package kr.dogfoot.webserver.server.object;

import kr.dogfoot.webserver.context.ContextManager;
import kr.dogfoot.webserver.context.connection.ajp.AjpProxyConnectionManager;
import kr.dogfoot.webserver.context.connection.http.client.ClientConnectionManager;
import kr.dogfoot.webserver.context.connection.http.proxy.HttpProxyConnectionManager;
import kr.dogfoot.webserver.httpMessage.reply.maker.ReplyMaker;
import kr.dogfoot.webserver.server.buffersender.SendBufferStorage;
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

    private ExecutorService ioExecutorService;

    private DefinedMediaTypeManager defaultMediaTypeManager;
    private ReplyMaker replyMaker;
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
        replyMaker = new ReplyMaker(serverProperties);
    }
    public void initialize() {
        ioExecutorService = Executors.newFixedThreadPool(serverProperties.pooledThreadCount());
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

    public ExecutorService ioExecutorService() {
        return ioExecutorService;
    }

    public DefinedMediaTypeManager defaultMediaTypeManager() {
        return defaultMediaTypeManager;
    }

    public ReplyMaker replyMaker() {
        return replyMaker;
    }
}

