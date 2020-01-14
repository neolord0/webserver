package kr.dogfoot.webserver.processor;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.context.ContextManager;
import kr.dogfoot.webserver.context.connection.ajp.AjpProxyConnectionManager;
import kr.dogfoot.webserver.context.connection.http.client.ClientConnectionManager;
import kr.dogfoot.webserver.context.connection.http.proxy.HttpProxyConnectionManager;
import kr.dogfoot.webserver.httpMessage.response.maker.ResponseMaker;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.server.Startable;
import kr.dogfoot.webserver.server.buffersender.BufferSender;
import kr.dogfoot.webserver.server.object.BufferManager;
import kr.dogfoot.webserver.server.object.ServerProperties;

import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class Processor implements Startable {
    protected Server server;
    protected ConcurrentLinkedQueue<Context> waitingContextQueue;
    protected int id = 0;

    protected Processor(Server server, int id) {
        this.server = server;
        this.id = id;
        waitingContextQueue = new ConcurrentLinkedQueue<Context>();
    }

    public int id() {
        return id;
    }

    public void prepareContext(Context context) {
        waitingContextQueue.add(context);
        wakeup();
    }

    public int waitContextCount() {
        return waitingContextQueue.size();
    }

    protected void gotoSelf(Context context) {
        prepareContext(context);
    }

    protected abstract void wakeup();

    protected ServerProperties serverProperties() {
        return server.objects().properties();
    }

    protected ContextManager contextManager() {
        return server.objects().contextManager();
    }

    protected ClientConnectionManager clientConnectionManager() {
        return server.objects().clientConnectionManager();
    }

    protected AjpProxyConnectionManager ajpProxyConnectionManager() {
        return server.objects().ajpProxyConnectionManager();
    }

    protected HttpProxyConnectionManager httpProxyConnectionManager() {
        return server.objects().httpProxyConnectionManager();
    }

    protected BufferSender bufferSender() {
        return server.bufferSender();
    }

    protected BufferManager bufferManager() {
        return server.objects().bufferManager();
    }

    protected ResponseMaker responseMaker() {
        return server.objects().responseMaker();
    }

    protected void closeAllConnectionFor(Context context) {
        if (context.ajpProxy() != null) {
            bufferSender().sendCloseSignalForAjpServer(context);
        }
        if (context.httpProxy() != null) {
            bufferSender().sendCloseSignalForHttpServer(context);
        }
        bufferSender().sendCloseSignalForClient(context);
    }

}
