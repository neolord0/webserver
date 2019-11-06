package kr.dogfoot.webserver.util.message.http;

import kr.dogfoot.webserver.context.connection.http.senderstatus.SenderStatus;
import kr.dogfoot.webserver.httpMessage.header.HeaderItem;
import kr.dogfoot.webserver.httpMessage.reply.EachRangePart;
import kr.dogfoot.webserver.httpMessage.reply.Reply;
import kr.dogfoot.webserver.util.bytes.ToBytes;
import kr.dogfoot.webserver.util.http.HttpString;

import java.nio.ByteBuffer;

public class ReplyToBuffer {
    public static void forStatusLine(ByteBuffer buffer, Reply reply) {
        if (reply.code() != null) {
            buffer
                    .put(HttpString.Version_Prefix)
                    .put(ToBytes.fromInt(reply.majorVersion()))
                    .put(HttpString.Dot)
                    .put(ToBytes.fromInt(reply.minorVersion()))
                    .put(HttpString.Space);
            buffer
                    .put(reply.code().getCodeByte())
                    .put(HttpString.Space);
            if (reply.reason() != null) {
                buffer.put(reply.reason());
            } else {
                buffer.put(reply.code().getDefaultReason());
            }
            buffer.put(HttpString.CRLF);
        }
    }

    public static void forHeaders(ByteBuffer buffer, Reply reply) {
        forRangeHeader(buffer, reply);
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

    public static void forPartBoundary(ByteBuffer buffer, Reply reply) {
        if (reply.boundary() != null) {
            buffer
                    .put(HttpString.CRLF)
                    .put(HttpString.BoundaryPrefix)
                    .put(reply.boundary())
                    .put(HttpString.CRLF);
        }
    }

    public static void forPartHeader(ByteBuffer buffer, SenderStatus ss, Reply reply) {
        EachRangePart rangePart = reply.rangePart(ss.rangeIndex());
        forRangeHeader(buffer, rangePart);
    }

    public static void forEndBoundary(ByteBuffer buffer, Reply reply) {
        if (reply.boundary() != null) {
            buffer
                    .put(HttpString.CRLF)
                    .put(HttpString.BoundaryPrefix)
                    .put(reply.boundary())
                    .put(HttpString.BoundaryPrefix)
                    .put(HttpString.CRLF);
        }
    }

    public static void forHeaders2(ByteBuffer buffer, Reply reply) {
        forRangeHeader2(buffer, reply);
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
