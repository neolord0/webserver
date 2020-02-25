package kr.dogfoot.webserver.util.message.http;

import kr.dogfoot.webserver.httpMessage.header.HeaderItem;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.util.http.HttpString;

import java.nio.ByteBuffer;

public class RequestToBuffer {
    public static void forRequestLineAndHeaders(ByteBuffer buffer, Request request) {
        forRequestLine(buffer, request);
        forHeaders(buffer, request);
    }

    private static void forRequestLine(ByteBuffer buffer, Request request) {
        buffer
                .put(request.method().getBytes())
                .put(HttpString.Space)
                .put(request.requestURI().toString().getBytes())
                .put(HttpString.Space)
                .put(HttpString.Http_1_1)
                .put(HttpString.CRLF);
    }

    private static void forHeaders(ByteBuffer buffer, Request request) {
        for (HeaderItem item : request.headerList().getHeaderItemArray()) {
            buffer
                    .put(item.sort().toString().getBytes())
                    .put(HttpString.HeaderSeparator)
                    .put(item.valueBytes())
                    .put(HttpString.CRLF);
        }
        buffer.put(HttpString.CRLF);
    }
}
