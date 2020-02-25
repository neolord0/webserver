package kr.dogfoot.webserver.processor.util;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.httpMessage.util.RequestSetter;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.server.host.proxy_info.BackendServerInfo;
import kr.dogfoot.webserver.util.http.HttpString;
import kr.dogfoot.webserver.util.message.http.RequestToBuffer;

import java.nio.ByteBuffer;

public class ToHttpServer {
    public static void sendRequest(Context context, Server server) {
        changeRequestURI(context.request(), context.httpProxy().backendServerInfo());
        RequestSetter.setHost(context.request(),
                context.httpProxy().backendServerInfo().ipOrDomain(),
                context.httpProxy().backendServerInfo().port());
        RequestSetter.reduceMaxForwards(context.request());
        RequestSetter.addVia(context.request(), HttpString.Http, HttpString.Version_1_1,
                context.host().name(), null);

        ByteBuffer buffer = server.objects().bufferManager().pooledNormalBuffer();
        RequestToBuffer.forRequestLineAndHeaders(buffer, context.request());
        buffer.flip();

        server.bufferSender().sendBufferToHttpServer(context, buffer, true);
    }

    private static void changeRequestURI(Request request, BackendServerInfo backendServerInfo) {
        request.requestURI().applyBackendServer(backendServerInfo);
    }

    private static void changeHostHeader(Request request, BackendServerInfo backendServerInfo) {
    }
}
