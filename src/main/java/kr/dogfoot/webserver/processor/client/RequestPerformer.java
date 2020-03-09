package kr.dogfoot.webserver.processor.client;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.context.ContextState;
import kr.dogfoot.webserver.context.connection.http.parserstatus.BodyParsingType;
import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueETag;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueHost;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueLastModified;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.httpMessage.request.URIType;
import kr.dogfoot.webserver.httpMessage.response.Response;
import kr.dogfoot.webserver.httpMessage.util.RequestSetter;
import kr.dogfoot.webserver.httpMessage.util.ResponseSetter;
import kr.dogfoot.webserver.processor.GeneralProcessor;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.server.cache.SelectedResourceInfo;
import kr.dogfoot.webserver.server.cache.StoredResponse;
import kr.dogfoot.webserver.server.host.Host;
import kr.dogfoot.webserver.server.host.proxy_info.BackendServerInfo;
import kr.dogfoot.webserver.server.host.proxy_info.ProxyInfo;
import kr.dogfoot.webserver.server.host.proxy_info.filter.ProxyFilter;
import kr.dogfoot.webserver.server.resource.filter.Filter;
import kr.dogfoot.webserver.server.resource.look.LookResult;
import kr.dogfoot.webserver.server.resource.performer.ConditonalGetPerformer;
import kr.dogfoot.webserver.util.Message;

import java.io.IOException;
import java.util.ArrayList;

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

        // test
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        try {
            System.out.println(context.clientConnection().channel().getRemoteAddress() + " ==> " + context.clientConnection().channel().getLocalAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(context.request());
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        boolean preformByProxy = false;

        context.host(findHost(context.request()));
        if (context.host() != null) {
            ProxyInfo proxyInfo = context.host().findProxyInfo(context.request().requestURI().path());
            if (proxyInfo != null) {
                preformByProxy = proxy(context, proxyInfo);
            } else {
                // test
                System.out.println("perform from original server");
                perform(context);
            }
        } else {
            context.response(responseMaker().new_400BadRequest("no host header"));
        }

        if (preformByProxy == false && context.response() != null) {
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
            HeaderValueHost host = (HeaderValueHost) request.getHeaderValueObj(HeaderSort.Host);
            if (host != null) {
                hostIpOrDomain = host.ipOrDomain();
                hostPort = host.port();
            }
        }
        if (hostIpOrDomain == null) {
            return null;
        }

        return server.findHost(hostIpOrDomain, hostPort);
    }

    private boolean proxy(Context context, ProxyInfo proxyInfo) {
        BackendServerInfo backendServer = proxyInfo.backendServerManager().appropriateBackendServer();

        if (backendServer.cacheOption().use()) {
            return performByCache(context, proxyInfo, backendServer);
        } else {
            return sendRequestToBackendServer(context, proxyInfo, backendServer);
        }
    }

    private boolean performByCache(Context context, ProxyInfo proxyInfo, BackendServerInfo backendServer) {
        context.backupOriginalRequest();

        SelectedResourceInfo selectedResourceInfo = cacheManager().select(backendServer, context.request());
        System.out.println("!!  " + selectedResourceInfo.postSelectProcessing());
        switch (selectedResourceInfo.postSelectProcessing()) {
            case SendRequest:
                return sendRequestToBackendServer(context, proxyInfo, backendServer);
            case SendValidationRequest:
                return sendValidationRequestToBackendServer(context, proxyInfo, backendServer, selectedResourceInfo);
            case UseStoredResponse:
                if (context.request().hasHeader(HeaderSort.If_None_Match)
                        || context.request().hasHeader(HeaderSort.If_Modified_Since)) {
                    return performValidationRequest(context, proxyInfo, backendServer, selectedResourceInfo);
                } else {
                    return sendStoredResponseToClient(context, selectedResourceInfo);
                }
        }
        return sendRequestToBackendServer(context, proxyInfo, backendServer);
    }

    private boolean sendRequestToBackendServer(Context context, ProxyInfo proxyInfo, BackendServerInfo backendServer) {
        long currentTime = System.currentTimeMillis();

        boolean continuePerform = inboundProxyFilter(context, proxyInfo);

        boolean proxied = false;
        if (continuePerform == true) {
            context.proxyProtocol(backendServer.protocol());
            switch (context.proxyProtocol()) {
                case Ajp13:
                    context.ajpProxy(ajpProxyConnectionManager().pooledbject(context, backendServer));
                    proxied = true;
                    if (context.ajpProxy().stateIsNoConnect()) {
                        server.gotoProxyConnector(context);
                    } else {
                        context.ajpProxy().lastAccessTime(currentTime);
                        server.gotoAjpProxier(context);
                    }
                    break;

                case Http:
                    proxied = true;
                    if (httpProxyIsNoConnect(context, proxyInfo)) {
                        context.httpProxy(httpProxyConnectionManager().pooledObject(context, backendServer));
                        server.gotoProxyConnector(context);
                    } else {
                        context.httpProxy().lastAccessTime(currentTime);
                        server.gotoHttpProxier(context);
                    }
                    break;

                default:
                    context.response(responseMaker().new_500NotSupportedProxyProtocol(context.proxyProtocol()));
                    proxied = false;
                    break;
            }
        }
        return proxied;
    }

    private boolean sendValidationRequestToBackendServer(Context context, ProxyInfo proxyInfo, BackendServerInfo backendServer, SelectedResourceInfo selectedResourceInfo) {
        selectedResourceInfo.lockUsing();

        addETagToIfNoneMatch(context.request(), selectedResourceInfo);
        setIfModifiedSinceToLastModified(context.request(), selectedResourceInfo);

        selectedResourceInfo.freeUsing();
        return sendRequestToBackendServer(context, proxyInfo, backendServer);
    }

    private void addETagToIfNoneMatch(Request request, SelectedResourceInfo selectedResourceInfo) {
        ArrayList<byte[]> etags = new ArrayList<byte[]>();
        for (StoredResponse storedResponse : selectedResourceInfo.storedResponses()) {
            HeaderValueETag eTag = (HeaderValueETag) storedResponse.response().getHeaderValueObj(HeaderSort.ETag);
            etags.add(eTag.etag());
     }
        RequestSetter.addETagToIfNoneMatch(request, etags);
    }

    private void setIfModifiedSinceToLastModified(Request request, SelectedResourceInfo selectedResourceInfo) {
        StoredResponse storedResponse = selectedResourceInfo.mostRecentResponses();

        HeaderValueLastModified lastModified = (HeaderValueLastModified) storedResponse.response().getHeaderValueObj(HeaderSort.Last_Modified);
        if (lastModified != null) {
            RequestSetter.setIfModifiedSince(request, lastModified.date());
        }
    }

    private boolean performValidationRequest(Context context, ProxyInfo proxyInfo, BackendServerInfo backendServer, SelectedResourceInfo selectedResourceInfo) {
        Message.debug(context,
                "perform validation request for cached response");

        selectedResourceInfo.lockUsing();
        Response response = ConditonalGetPerformer.performForCache(context.request(), selectedResourceInfo, context.host().hostObjects());
        selectedResourceInfo.freeUsing();

        if (response != null) {
            context.response(response);
            return false;
        } else {
            return sendValidationRequestToBackendServer(context, proxyInfo, backendServer, selectedResourceInfo);
        }
    }

    private boolean sendStoredResponseToClient(Context context, SelectedResourceInfo selectedResourceInfo) {
        Message.debug(context,
                "send cached response to client");

        StoredResponse storedResponse = selectedResourceInfo.mostRecentResponses();

        storedResponse.lockUsing();

        context.response(storedResponse.response().clone());
        ResponseSetter.setAge(context.response(), storedResponse.currentAge() / 1000);
        context.response().bodyFile(storedResponse.bodyFile());

        storedResponse.freeUsing();

        return false;
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
            if (context.request().hasBody() && context.request().isEmptyBody()) {
                context.clientConnection().parserStatus()
                        .prepareBodyParsing(BodyParsingType.ForDefaultProcessing, context.request().contentLength());
                server.gotoBodyReceiver(context);
            } else {
                if (context.resource() != null) {
                    Response response = context.resource().perform(context.request(), context.host().hostObjects());
                    context.response(response);
                } else {
                    context.response(responseMaker().new_404NotFound(context.request()));
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
            Response response = context.resource().perform(context.request(), context.host().hostObjects());
            context.response(response);
        } else {
            context.response(responseMaker().new_404NotFound(context.request()));
        }

        outboundFilter(context);

        if (context.response() != null) {
            context.clientConnection().senderStatus().reset();
            server.gotoSender(context);
        }
    }
}
