package kr.dogfoot.webserver.context;

import kr.dogfoot.webserver.context.connection.ajp.AjpProxyConnection;
import kr.dogfoot.webserver.context.connection.http.client.HttpClientConnection;
import kr.dogfoot.webserver.context.connection.http.proxy.HttpProxyConnection;
import kr.dogfoot.webserver.httpMessage.reply.Reply;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.server.buffersender.BufferSender;
import kr.dogfoot.webserver.server.host.Host;
import kr.dogfoot.webserver.server.host.proxy_info.BackendServerInfo;
import kr.dogfoot.webserver.server.host.proxy_info.ProxyInfo;
import kr.dogfoot.webserver.server.resource.Resource;
import kr.dogfoot.webserver.server.resource.filter.Filter;

public class Context {
    private ContextState state;

    private Request request;
    private Reply reply;

    private Host host;
    private Resource resource;
    private Filter[] filters;

    private HttpClientConnection clientConnection;

    private ProxyInfo proxyInfo;
    private BackendServerInfo backendServerInfo;
    private AjpProxyConnection ajpProxyConnection;
    private HttpProxyConnection httpProxyConnection;

    private BufferSender bufferSender;

    public Context() {
        state = ContextState.Waiting;

        request = new Request();
        reply = null;

        host = null;
        resource = null;
        filters = null;

        clientConnection = null;

        proxyInfo = null;
        backendServerInfo = null;
        ajpProxyConnection = null;
        httpProxyConnection = null;
    }

    public Context resetForPooled() {
        state = ContextState.Waiting;

        request.reset();
        reply = null;

        host = null;
        resource = null;
        filters = null;

        clientConnection = null;

        proxyInfo = null;
        backendServerInfo = null;
        ajpProxyConnection = null;
        httpProxyConnection = null;

        return this;
    }

    public void resetForNextRequest() {
        state = ContextState.Waiting;

        request.reset();
        reply = null;

        host = null;
        resource = null;
        filters = null;

        clientConnection.resetForNextRequest();

        ajpProxyConnection = null;
    }

    public ContextState state() {
        return state;
    }

    public void changeState(ContextState state) {
        this.state = state;
    }

    public Request request() {
        return request;
    }

    public Reply reply() {
        return reply;
    }

    public void reply(Reply reply) {
        this.reply = reply;
    }

    public Host host() {
        return host;
    }

    public void host(Host host) {
        this.host = host;
    }

    public Resource resource() {
        return resource;
    }

    public Context resource(Resource resource) {
        this.resource = resource;
        return this;
    }

    public Filter[] filters() {
        return filters;
    }

    public void filters(Filter[] filters) {
        this.filters = filters;
    }

    public HttpClientConnection clientConnection() {
        return clientConnection;
    }

    public void clientConnection(HttpClientConnection httpConnection) {
        this.clientConnection = httpConnection;
        if (httpConnection != null) {
            httpConnection.context(this);
        }
    }

    public ProxyInfo proxyInfo() {
        return proxyInfo;
    }

    public Context proxyInfo(ProxyInfo proxyInfo) {
        this.proxyInfo = proxyInfo;
        return this;
    }

    public BackendServerInfo backendServerInfo() {
        return backendServerInfo;
    }

    public void backendServerInfo(BackendServerInfo backendServerInfo) {
        this.backendServerInfo = backendServerInfo;
    }

    public AjpProxyConnection ajpProxy() {
        return ajpProxyConnection;
    }

    public void ajpProxy(AjpProxyConnection ajpProxy) {
        ajpProxyConnection = ajpProxy;
        if (ajpProxyConnection != null) {
            ajpProxyConnection.context(this);
        }
    }

    public HttpProxyConnection httpProxy() {
        return httpProxyConnection;
    }

    public Context httpProxy(HttpProxyConnection httpProxy) {
        this.httpProxyConnection = httpProxy;
        if (httpProxyConnection != null) {
            httpProxyConnection.context(this);
        }
        return this;
    }
}
