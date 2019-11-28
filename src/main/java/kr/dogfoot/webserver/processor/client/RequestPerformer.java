package kr.dogfoot.webserver.processor.client;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.context.ContextState;
import kr.dogfoot.webserver.context.connection.http.parserstatus.BodyParsingType;
import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueHost;
import kr.dogfoot.webserver.httpMessage.reply.Reply;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.httpMessage.request.URIType;
import kr.dogfoot.webserver.processor.GeneralProcessor;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.server.host.Host;
import kr.dogfoot.webserver.server.host.proxy_info.BackendServerInfo;
import kr.dogfoot.webserver.server.host.proxy_info.Protocol;
import kr.dogfoot.webserver.server.host.proxy_info.ProxyInfo;
import kr.dogfoot.webserver.server.host.proxy_info.filter.ProxyFilter;
import kr.dogfoot.webserver.server.resource.filter.Filter;
import kr.dogfoot.webserver.server.resource.look.LookResult;
import kr.dogfoot.webserver.util.Message;
import sun.tools.tree.OrExpression;

import static kr.dogfoot.webserver.server.host.proxy_info.Protocol.*;

public class RequestPerformer extends GeneralProcessor {
    private static int RequestPerformerID = 0;

    public RequestPerformer(Server server) {
        super(server, RequestPerformerID++);
    }

    @Override
    protected void onNewContext(Context context) {
        server.objects().executorForRequestPerforming()
                .execute(() -> {
                    if (context.state() == ContextState.ReceivingRequest) {
                        onAfterReceivingRequest(context);
                    } else {
                        onAfterReceivingBody(context);
                    }
                });
    }

    private void onAfterReceivingRequest(Context context) {
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
        long currentTime = System.currentTimeMillis();
        boolean continuePerform = inboundProxyFilter(context, proxyInfo);
        boolean proxied = false;
        if (continuePerform == true) {
            BackendServerInfo backendServer = proxyInfo.backendServerManager().appropriateBackendServer();
            System.out.println("backend server( " + backendServer.index()+ " ) connection count : " + backendServer.connectCount());

            context.proxyProtocol(backendServer.protocol());
            switch(context.proxyProtocol()) {
                case Ajp13: {
                        context.ajpProxy(ajpProxyConnectionManager().pooledbject(context, backendServer));
                        if (context.ajpProxy().stateIsNoConnect()) {

                            server.gotoProxyConnector(context);
                            proxied = true;
                        } else {
                            context.ajpProxy().lastAccessTime(currentTime);

                            server.gotoAjpProxier(context);
                            proxied = true;
                        }
                    }
                    break;
                case Http: {
                        if (httpProxyIsNoConnect(context, proxyInfo)) {
                            context.httpProxy(httpProxyConnectionManager().pooledObject(context, backendServer));

                            server.gotoProxyConnector(context);
                            proxied = true;
                        } else {
                            context.httpProxy().lastAccessTime(currentTime);

                            server.gotoHttpProxier(context);
                            proxied = true;
                        }
                    }
                    break;
                default: {
                        context.reply(replyMaker().new_500NotSupportedProxyProtocol(context.proxyProtocol()));
                        proxied = false;
                    }
                    break;
            }
        }
        return proxied;
    }

    private boolean inboundProxyFilter(Context context, ProxyInfo proxyInfo) {
        for (ProxyFilter f : proxyInfo.filters()) {
            if (f != null && f.inboundProcess(context, server) == false) {
                return false;
            }
        }
        return true;
    }

    private boolean httpProxyIsNoConnect(Context context, ProxyInfo newProxyInfo) {
        return context.httpProxy() == null ||
                context.httpProxy().backendServerInfo() == null ||
                context.httpProxy().backendServerInfo().proxyInfo().id() != newProxyInfo.id();
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
}
