package kr.dogfoot.webserver.server.resource.performer;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.reply.EachRangePart;
import kr.dogfoot.webserver.httpMessage.reply.Reply;
import kr.dogfoot.webserver.httpMessage.reply.ReplyCode;
import kr.dogfoot.webserver.server.resource.performer.util.ContentRange;
import kr.dogfoot.webserver.server.resource.performer.util.ContentRangeInfo;
import kr.dogfoot.webserver.server.resource.performer.util.ContentRangeInfoMaker;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueConnection;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueRange;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.server.host.HostObjects;
import kr.dogfoot.webserver.server.resource.ResourceFile;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.bytes.ToBytes;
import kr.dogfoot.webserver.util.http.HttpDateMaker;
import kr.dogfoot.webserver.util.http.HttpString;

public class FilePerformer {
    public static Reply perform(Request request, ResourceFile resource, HostObjects hostObjects) {
        switch (request.method()) {
            case OPTIONS:
                return options(request, resource, hostObjects);
            case GET:
                return get(request, resource, hostObjects);
            case HEAD:
                return head(request, resource, hostObjects);
            case POST:
                return post(request, resource, hostObjects);
            case PUT:
                return put(request, resource, hostObjects);
            case DELETE:
                return delete(request, resource, hostObjects);
            case TRACE:
                return trace(request, resource, hostObjects);
            case CONNECT:
                return connect(request, resource, hostObjects);
        }
        return null;
    }

    private static Reply options(Request request, ResourceFile resource, HostObjects hostObjects) {
        return null;
    }

    public static Reply get(Request request, ResourceFile resource, HostObjects hostObjects) {
        Reply reply = ConditonalGetPerformer.perform(request, resource, hostObjects);

        if (reply == null) {
            reply = hostObjects.replyMaker().new_200OK()
                    .addHeader(HeaderSort.Last_Modified, HttpDateMaker.makeBytes(resource.lastModified()))
                    .addHeader(HeaderSort.ETag, resource.etag())
                    .addHeader(HeaderSort.Content_Length, ToBytes.fromLong(resource.length()))
                    .addHeader(HeaderSort.Content_Type, contentType(resource.mediaType(), hostObjects.defaultCharset()))
                    .bodyFile(resource.file())
                    .bodyBytes(null);

            range(request, reply, resource);
            connection(request, reply, hostObjects);
        }
        return reply;
    }

    private static byte[] contentType(byte[] mediaType, String defaultCharset) {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer
                .append(mediaType)
                .append(HttpString.Semicolon)
                .append(HttpString.Charset_String, HttpString.Equal, defaultCharset);
        byte[] bytes = buffer.getBytes();
        OutputBuffer.release(buffer);
        return bytes;
    }

    private static void range(Request request, Reply reply, ResourceFile resource) {
        HeaderValueRange range = (HeaderValueRange) request.getHeaderValueObj(HeaderSort.Range);
        if (range != null) {
            ContentRangeInfo cri = ContentRangeInfoMaker.make(range, resource.length());
            if (cri.invalid()) {
                reply.isPartial(false);
                reply.range(new ContentRange(0, resource.length() - 1));
            } else {
                reply.isPartial(true);
                if (cri.rangeCount() == 1) {
                    ContentRange cr = cri.range(0);
                    reply
                            .code(ReplyCode.Code206)
                            .addHeader(HeaderSort.Accept_Ranges, HttpString.Bytes)
                            .changeHeader(HeaderSort.Content_Length, ToBytes.fromLong(cr.length()))
                            .addHeader(HeaderSort.Content_Range, cri.toBytes(0))
                            .range(cr);
                } else {
                    byte[] boundary = HttpString.newBoundary();
                    reply
                            .code(ReplyCode.Code206)
                            .addHeader(HeaderSort.Accept_Ranges, HttpString.Bytes)
                            .boundary(boundary)
                            .changeHeader(HeaderSort.Content_Type, HttpString.mulitPart_Byteranges(boundary))
                            .range(null);
                    for (int index = 0; index < cri.rangeCount(); index++) {
                        ContentRange cr = cri.range(index);
                        EachRangePart rangePart = reply.addNewRangePart()
                                .addHeader(HeaderSort.Content_Type, resource.mediaType())
                                .addHeader(HeaderSort.Content_Range, cri.toBytes(index))
                                .range(cr);
                    }

                    reply.changeHeader(HeaderSort.Content_Length, ToBytes.fromLong(reply.calculateContextLength()));
                }
            }
        } else {
            reply.isPartial(false);
            reply.range(new ContentRange(0, resource.length() - 1));
        }
    }

    public static void connection(Request request, Reply reply, HostObjects hostObjects) {
        HeaderValueConnection connection = (HeaderValueConnection) request.getHeaderValueObj(HeaderSort.Connection);
        if (connection != null && connection.isKeepAlive()) {
            reply.addHeader(HeaderSort.Keep_Alive, HttpString.keepAliveValue(hostObjects.serverProperties().keepAlive_timeout(),
                    hostObjects.serverProperties().keepAlive_max()));
            reply.addHeader(HeaderSort.Connection, HttpString.Keep_Alive);
        } else if (request.isPersistentConnection() == false) {
            reply.addHeader(HeaderSort.Connection, HttpString.Close);
        }
    }


    public static Reply head(Request request, ResourceFile resource, HostObjects hostObjects) {
        Reply reply = hostObjects.replyMaker().new_200OK()
                .addHeader(HeaderSort.Last_Modified, ToBytes.fromLong(resource.lastModified()))
                .addHeader(HeaderSort.ETag, resource.etag())
                .addHeader(HeaderSort.Content_Length, ToBytes.fromLong(resource.length()))
                .addHeader(HeaderSort.Content_Type, resource.mediaType())
                .bodyFile(null)
                .bodyBytes(null);

        range(request, reply, resource);
        connection(request, reply, hostObjects);
        return reply;
    }

    public static Reply post(Request request, ResourceFile resource, HostObjects hostObjects) {
        return hostObjects.replyMaker().new_DefalutPostReply(request);
    }

    public static Reply put(Request request, ResourceFile resource, HostObjects hostObjects) {
        return null;
    }

    public static Reply delete(Request request, ResourceFile resource, HostObjects hostObjects) {
        return null;
    }

    public static Reply trace(Request request, ResourceFile resource, HostObjects hostObjects) {
        return null;
    }

    public static Reply connect(Request request, ResourceFile resource, HostObjects hostObjects) {
        return null;
    }
}
