package kr.dogfoot.webserver.processor.util;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.httpMessage.header.HeaderItem;
import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueMaxForwards;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.server.host.proxy_info.BackendServerInfo;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;
import kr.dogfoot.webserver.util.message.http.RequestToBuffer;

import java.nio.ByteBuffer;

public class ToHttpServer {
    public static void sendRequest(Context context, Server server) {
        ByteBuffer buffer = server.objects().bufferManager().pooledNormalBuffer();
        changeRequestURI(context.request(), context.backendServerInfo());
        changeMaxForwards(context.request(), context);
        addVia(context, server);

        RequestToBuffer.forRequestLineAndHeaders(buffer, context.request());
        buffer.flip();

        context.bufferSender().sendBufferToHttpServer(context, buffer, true);
    }

    private static void changeRequestURI(Request request, BackendServerInfo backendServerInfo) {
        request.requestURI().applyBackendServer(backendServerInfo);
    }

    private static void changeMaxForwards(Request request, Context context) {
        HeaderValueMaxForwards maxForwards = null;
        HeaderItem item = request.headerList().getHeader(HeaderSort.Max_Forwards);
        if (item != null) {
            try {
                maxForwards = (HeaderValueMaxForwards) item.updateValueObj();
            } catch (ParserException e) {
                e.printStackTrace();
                maxForwards = null;
            }
            if (maxForwards != null) {
                maxForwards.value(maxForwards.value() - 1);
                item.updateValueBytes();
            }
        }
    }

    private static void addVia(Context context, Server server) {
        HeaderItem item = context.request().headerList().getHeader(HeaderSort.Max_Forwards);
        if (item != null) {
            byte[] via = item.valueBytes();

            OutputBuffer buffer = OutputBuffer.pooledObject();
            if (via != null) {
                buffer
                        .append(via)
                        .appendComma()
                        .appendSP();
            }
            buffer
                    .append(HttpString.Http_1_1)
                    .appendSP()
                    .append(context.host().name());

            item.valueBytes(buffer.getBytes());
            OutputBuffer.release(buffer);
        }
    }
}
