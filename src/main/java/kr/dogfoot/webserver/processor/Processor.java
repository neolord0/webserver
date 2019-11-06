package kr.dogfoot.webserver.processor;

import kr.dogfoot.webserver.httpMessage.reply.ReplyMaker;
import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.context.ContextManager;
import kr.dogfoot.webserver.context.connection.ajp.AjpProxyConnectionManager;
import kr.dogfoot.webserver.context.connection.http.client.ClientConnectionManager;
import kr.dogfoot.webserver.context.connection.http.proxy.HttpProxyConnectionManager;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.server.Startable;
import kr.dogfoot.webserver.server.object.BufferManager;
import kr.dogfoot.webserver.server.object.ServerProperties;
import kr.dogfoot.webserver.server.timer.Timer;

import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class Processor implements Startable {
    protected Server server;
    protected ConcurrentLinkedQueue<Context> waitingContextQueue;

    public Processor(Server server) {
        this.server = server;
        waitingContextQueue = new ConcurrentLinkedQueue<Context>();
    }

    public void prepareContext(Context context) {
        waitingContextQueue.add(context);
        wakeup();
    }

    protected abstract void wakeup();

    public int waitingContextCount() {
        return waitingContextQueue.size();
    }

    protected ServerProperties serverProperties() {
        return server.objects().properties();
    }

    protected Timer timer() {
        return server.objects().timer();
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

    protected BufferManager bufferManager() {
        return server.objects().bufferManager();
    }

    protected ReplyMaker replyMaker() {
        return server.objects().replyMaker();
    }

    protected void closeAllConnectionFor(Context context) {
        if (context.ajpProxy() != null) {
            server.sendCloseSignalForAjpServer(context);
        }
        if (context.httpProxy() != null) {
            server.sendCloseSignalForHttpServer(context);
        }
        server.sendCloseSignalForClient(context);
    }
}
