package kr.dogfoot.webserver.util.message.http;

import kr.dogfoot.webserver.context.connection.http.senderstatus.SenderStatus;
import kr.dogfoot.webserver.httpMessage.header.HeaderItem;
import kr.dogfoot.webserver.httpMessage.response.EachRangePart;
import kr.dogfoot.webserver.httpMessage.response.Response;
import kr.dogfoot.webserver.util.bytes.ToBytes;
import kr.dogfoot.webserver.util.http.HttpString;

import java.nio.ByteBuffer;

public class ResponseToBuffer {
    public static void forStatusLine(ByteBuffer buffer, Response response) {
        if (response.statusCode() != null) {
            buffer
                    .put(HttpString.Version_Prefix)
                    .put(ToBytes.fromInt(response.majorVersion()))
                    .put(HttpString.Dot)
                    .put(ToBytes.fromInt(response.minorVersion()))
                    .put(HttpString.Space);
            buffer
                    .put(response.statusCode().getCodeByte())
                    .put(HttpString.Space);
            if (response.reason() != null) {
                buffer.put(response.reason());
            } else {
                buffer.put(response.statusCode().getDefaultReason());
            }
            buffer.put(HttpString.CRLF);
        }
    }

    public static void forHeaders(ByteBuffer buffer, Response response) {
        forRangeHeader(buffer, response);
    }

    private static void forRangeHeader(ByteBuffer buffer, EachRangePart rangePart) {
        int count = rangePart.headerCount();
        for (int index = 0; index < count; index++) {
            HeaderItem item = rangePart.getHeaderItem(index);
            buffer
                    .put(item.sort().toString().getBytes())
                    .put(HttpString.HeaderSeparator)
                    .put(item.valueBytes())
                    .put(HttpString.CRLF);
        }
        buffer.put(HttpString.CRLF);
    }

    public static void forPartBoundary(ByteBuffer buffer, Response response) {
        if (response.boundary() != null) {
            buffer
                    .put(HttpString.CRLF)
                    .put(HttpString.BoundaryPrefix)
                    .put(response.boundary())
                    .put(HttpString.CRLF);
        }
    }

    public static void forPartHeader(ByteBuffer buffer, SenderStatus ss, Response response) {
        EachRangePart rangePart = response.rangePart(ss.rangeIndex());
        forRangeHeader(buffer, rangePart);
    }

    public static void forEndBoundary(ByteBuffer buffer, Response response) {
        if (response.boundary() != null) {
            buffer
                    .put(HttpString.CRLF)
                    .put(HttpString.BoundaryPrefix)
                    .put(response.boundary())
                    .put(HttpString.BoundaryPrefix)
                    .put(HttpString.CRLF);
        }
    }

    public static void forHeaders2(ByteBuffer buffer, Response response) {
        forRangeHeader2(buffer, response);
    }

    private static void forRangeHeader2(ByteBuffer buffer, EachRangePart rangePart) {
        int count = rangePart.headerCount();
        for (int index = 0; index < count; index++) {
            HeaderItem item = rangePart.getHeaderItem(index);
            buffer
                    .put(item.sort().toString().getBytes())
                    .put(HttpString.HeaderSeparator)
                    .put(item.valueBytes())
                    .put(HttpString.CRLF);
        }
    }
}
