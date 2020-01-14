package kr.dogfoot.webserver.server.resource.performer;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueConnection;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueRange;
import kr.dogfoot.webserver.httpMessage.response.EachRangePart;
import kr.dogfoot.webserver.httpMessage.response.Response;
import kr.dogfoot.webserver.httpMessage.response.StatusCode;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.server.host.HostObjects;
import kr.dogfoot.webserver.server.resource.ResourceFile;
import kr.dogfoot.webserver.server.resource.performer.util.ContentRange;
import kr.dogfoot.webserver.server.resource.performer.util.ContentRangeInfo;
import kr.dogfoot.webserver.server.resource.performer.util.ContentRangeInfoMaker;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.bytes.ToBytes;
import kr.dogfoot.webserver.util.http.HttpDateMaker;
import kr.dogfoot.webserver.util.http.HttpString;

public class FilePerformer {
    public static Response perform(Request request, ResourceFile resource, HostObjects hostObjects) {
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

    private static Response options(Request request, ResourceFile resource, HostObjects hostObjects) {
        return null;
    }

    public static Response get(Request request, ResourceFile resource, HostObjects hostObjects) {
        Response response = ConditonalGetPerformer.perform(request, resource, hostObjects);

        if (response == null) {
            response = hostObjects.responseMaker().new_200OK()
                    .addHeader(HeaderSort.Last_Modified, HttpDateMaker.makeBytes(resource.lastModified()))
                    .addHeader(HeaderSort.ETag, resource.etag())
                    .addHeader(HeaderSort.Content_Length, ToBytes.fromLong(resource.length()))
                    .addHeader(HeaderSort.Content_Type, contentType(resource.mediaType(), hostObjects.defaultCharset()))
                    .bodyFile(resource.file())
                    .bodyBytes(null);

            range(request, response, resource);
            connection(request, response, hostObjects);
        }
        return response;
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

    private static void range(Request request, Response response, ResourceFile resource) {
        HeaderValueRange range = (HeaderValueRange) request.getHeaderValueObj(HeaderSort.Range);
        if (range != null) {
            ContentRangeInfo cri = ContentRangeInfoMaker.make(range, resource.length());
            if (cri.invalid()) {
                response.isPartial(false);
                response.range(new ContentRange(0, resource.length() - 1));
            } else {
                response.isPartial(true);
                if (cri.rangeCount() == 1) {
                    ContentRange cr = cri.range(0);
                    response
                            .code(StatusCode.Code206)
                            .addHeader(HeaderSort.Accept_Ranges, HttpString.Bytes)
                            .changeHeader(HeaderSort.Content_Length, ToBytes.fromLong(cr.length()))
                            .addHeader(HeaderSort.Content_Range, cri.toBytes(0))
                            .range(cr);
                } else {
                    byte[] boundary = HttpString.newBoundary();
                    response
                            .code(StatusCode.Code206)
                            .addHeader(HeaderSort.Accept_Ranges, HttpString.Bytes)
                            .boundary(boundary)
                            .changeHeader(HeaderSort.Content_Type, HttpString.mulitPart_Byteranges(boundary))
                            .range(null);
                    for (int index = 0; index < cri.rangeCount(); index++) {
                        ContentRange cr = cri.range(index);
                        EachRangePart rangePart = response.addNewRangePart()
                                .addHeader(HeaderSort.Content_Type, resource.mediaType())
                                .addHeader(HeaderSort.Content_Range, cri.toBytes(index))
                                .range(cr);
                    }

                    response.changeHeader(HeaderSort.Content_Length, ToBytes.fromLong(response.calculateContextLength()));
                }
            }
        } else {
            response.isPartial(false);
            response.range(new ContentRange(0, resource.length() - 1));
        }
    }

    public static void connection(Request request, Response response, HostObjects hostObjects) {
        HeaderValueConnection connection = (HeaderValueConnection) request.getHeaderValueObj(HeaderSort.Connection);
        if (connection != null && connection.isKeepAlive()) {
            response.addHeader(HeaderSort.Keep_Alive, HttpString.keepAliveValue(hostObjects.serverProperties().keepAlive_timeout(),
                    hostObjects.serverProperties().keepAlive_max()));
            response.addHeader(HeaderSort.Connection, HttpString.Keep_Alive);
        } else if (request.isPersistentConnection() == false) {
            response.addHeader(HeaderSort.Connection, HttpString.Close);
        }
    }


    public static Response head(Request request, ResourceFile resource, HostObjects hostObjects) {
        Response response = hostObjects.responseMaker().new_200OK()
                .addHeader(HeaderSort.Last_Modified, ToBytes.fromLong(resource.lastModified()))
                .addHeader(HeaderSort.ETag, resource.etag())
                .addHeader(HeaderSort.Content_Length, ToBytes.fromLong(resource.length()))
                .addHeader(HeaderSort.Content_Type, resource.mediaType())
                .bodyFile(null)
                .bodyBytes(null);

        range(request, response, resource);
        connection(request, response, hostObjects);
        return response;
    }

    public static Response post(Request request, ResourceFile resource, HostObjects hostObjects) {
        return hostObjects.responseMaker().new_DefalutPost(request);
    }

    public static Response put(Request request, ResourceFile resource, HostObjects hostObjects) {
        return null;
    }

    public static Response delete(Request request, ResourceFile resource, HostObjects hostObjects) {
        return null;
    }

    public static Response trace(Request request, ResourceFile resource, HostObjects hostObjects) {
        return null;
    }

    public static Response connect(Request request, ResourceFile resource, HostObjects hostObjects) {
        return null;
    }
}
