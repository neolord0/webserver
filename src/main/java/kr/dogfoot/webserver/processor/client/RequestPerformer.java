package kr.dogfoot.webserver.processor.client;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueHost;
import kr.dogfoot.webserver.httpMessage.reply.Reply;
import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.context.ContextState;
import kr.dogfoot.webserver.context.connection.http.parserstatus.BodyParsingType;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.httpMessage.request.URIType;
import kr.dogfoot.webserver.processor.GeneralProcessor;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.server.host.Host;
import kr.dogfoot.webserver.server.host.proxy_info.BackendServerInfo;
import kr.dogfoot.webserver.server.host.proxy_info.ProxyInfo;
import kr.dogfoot.webserver.server.resource.filter.Filter;
import kr.dogfoot.webserver.server.resource.look.LookResult;
import kr.dogfoot.webserver.util.Message;

public class RequestPerformer extends GeneralProcessor {
    public RequestPerformer(Server server) {
        super(server);
    }

    public void start() throws Exception {
        Message.debug("start Request Performer ...");

        super.start();
    }

    @Override
    protected void onNewContext(Context context) {
        if (context.state() == ContextState.ReceivingRequest) {
            onAfterReceivingRequest(context);
        } else {
            onAfterReceivingBody(context);
        }
    }

    private void onAfterReceivingRequest(Context context) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                context.changeState(ContextState.PerformingRequest);

                Message.debug(context,
                        "perform request " + new String(context.request().method().getBytes()) + " " + context.request().requestURI());

                boolean proxied = false;
                context.host(findHost(context.request()));
                if (context.host() != null) {
                    ProxyInfo proxyInfo = context.host().findProxyInfo(context.request().requestURI().path());
                    if (proxyInfo != null) {
                        proxied = proxy(context, proxyInfo);
                    } else {
                        perform(context);
                    }
                } else {
                    context.reply(replyMaker().new_400BadRequest("no host header"));
                }

                if (proxied == false && context.reply() != null) {
                    context.clientConnection().senderStatus().reset();
                    server.gotoSender(context);
                }
            }
        };
        server.objects().ioExecutorService().execute(r);
    }

    private Host findHost(Request request) {
        String hostIpOrDomain = null;
        int hostPort = -1;

        if (request.requestURI().type() == URIType.AbsoluteURI) {
            hostIpOrDomain = request.requestURI().ipOrDomain();
            hostPort = request.requestURI().port();
        } else {
            HeaderValueHost hh = (HeaderValueHost) request.getHeaderValueObj(HeaderSort.Host);
            if (hh != null) {
                hostIpOrDomain = hh.ipOrDomain();
                hostPort = hh.port();
            }
        }
        if (hostIpOrDomain == null) {
            return null;
        }

        return server.findHost(hostIpOrDomain, hostPort);
    }


    private boolean proxy(Context context, ProxyInfo proxyInfo) {
        BackendServerInfo backendServer = proxyInfo.backendServerManager().appropriateBackendServer();
        long currentTime = System.currentTimeMillis();

        context.proxyId(proxyInfo.id())
                .backendServerInfo(backendServer);
        if (backendServer.isAjp()) {
            context.ajpProxy(ajpProxyConnectionManager().pooledbject(context));
            if (context.ajpProxy().stateIsNoConnect()) {
                server.gotoProxyConnector(context);
                return true;
            } else {
                context.ajpProxy().lastAccessTime(currentTime);

                server.gotoAjpProxier(context);
                return true;
            }
        } else if (backendServer.isHttp()) {
            if (context.httpProxy() != null && context.proxyId() == proxyInfo.id()) {
                context.httpProxy().lastAccessTime(currentTime);
                server.gotoHttpProxier(context);
                return true;
            } else {
                context.httpProxy(httpProxyConnectionManager().pooledObject(context));
                server.gotoProxyConnector(context);
                return true;
            }
        }
        return false;
    }

    private void perform(Context context) {
        LookResult lr = context.host().look(context.request());
        context
                .resource(lr.resource())
                .filters(lr.filters());

        boolean continuePerform = inboundFilter(context);
        if (continuePerform == true) {
            if (context.request().hasBody() && context.request().emptyBody()) {
                context.clientConnection().parserStatus()
                        .prepareBodyParsing(BodyParsingType.ForDefaultProcessing, context.request().contentLength());
                server.gotoBodyReceiver(context);
            } else {
                if (context.resource() != null) {
                    Reply reply = context.resource().perform(context.request(), context.host().hostObjects());
                    context.reply(reply);
                } else {
                    context.reply(replyMaker().new_404NotFound(context.request()));
                }

                outboundFilter(context);
            }
        }
    }

    private boolean inboundFilter(Context context) {
        for (Filter f : context.filters()) {
            if (f.inboundProcess(context, server) == false) {
                return false;
            }
        }
        return true;
    }

    private boolean outboundFilter(Context context) {
        for (int index = context.filters().length - 1; index >= 0; index--) {
            Filter f = context.filters()[index];
            if (f.outboundProcess(context, server) == false) {
                return false;
            }
        }
        return true;
    }

    private void onAfterReceivingBody(Context context) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (context.resource() != null) {
                    Reply reply = context.resource().perform(context.request(), context.host().hostObjects());
                    context.reply(reply);
                } else {
                    context.reply(replyMaker().new_404NotFound(context.request()));
                }

                outboundFilter(context);

                if (context.reply() != null) {
                    context.clientConnection().senderStatus().reset();
                    server.gotoSender(context);
                }
            }
        };
        server.objects().ioExecutorService().execute(r);
    }

    @Override
    public void terminate() throws Exception {
        super.terminate();

        Message.debug("terminate Request Performer ...");
    }
}
