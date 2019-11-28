package kr.dogfoot.webserver.httpMessage.reply.maker;


import kr.dogfoot.webserver.httpMessage.header.HeaderItem;
import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueHost;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.ContentCodingSort;
import kr.dogfoot.webserver.httpMessage.reply.Reply;
import kr.dogfoot.webserver.httpMessage.request.MethodType;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.server.host.proxy_info.Protocol;
import kr.dogfoot.webserver.server.resource.ResourceNegotiatedFile;
import kr.dogfoot.webserver.server.resource.filter.part.condition.HeaderCondition;
import kr.dogfoot.webserver.server.resource.negotiation.NegotiationVariant;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.bytes.ToBytes;
import kr.dogfoot.webserver.util.http.HttpString;

public class DefaultMessageBody {
    public static void make(Reply reply, String message) {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer.append("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\n")
                .append("<html><head>\n")
                .append("<title>").append(reply.code().getCodeByte()).append(" ").append(reply.code().getDefaultReason()).append("</title>\n")
                .append("</head><body>\n")
                .append("<h1>").append(reply.code().getCodeByte()).append(" ").append(reply.code().getDefaultReason()).append("</h1>\n")
                .append("<p>").append(message).append("</p>\n")
                .append("</body></html>\n");
        setReplyBody(buffer, reply);
        OutputBuffer.release(buffer);
    }

    private static void setReplyBody(OutputBuffer bodyBuffer, Reply reply) {
        reply
                .addHeader(HeaderSort.Content_Length, ToBytes.fromInt(bodyBuffer.getLength()))
                .addHeader(HeaderSort.Content_Type, HttpString.Text_Html)
                .bodyBytes(bodyBuffer.getBytes())
                .bodyFile(null);
    }

    public static void makeForPostReply(Reply reply, Request request) {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer
                .append("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\n")
                .append("<html><head>\n")
                .append("<title>Default Post Reply</title>\n")
                .append("</head><body>\n")
                .append("<h1>Default Post Reply</h1>\n")
                .append("<br>\n")
                .append("<h2>URL : ").append(request.requestURI().toString()).append("</h2>\n")
                .append("<br>\n")
                .append("<h2>header list</h2>\n")
                .append("<ol>\n");

        HeaderItem[] items = request.headerList().getHeaderByteArray();
        for (HeaderItem item : items) {
            buffer
                    .append("<li>")
                    .append(item.sort().toString())
                    .append(":")
                    .append(item.valueBytes())
                    .append("</li>")
                    .append("\n");
        }
        buffer
                .append("</ol>\n")
                .append("<br>\n")
                .append("<h2>body</h2>\n")
                .append("<br>\n")
                .append("<textarea rows=\"20\" cols=\"100\">")
                .append(request.bodyBytes())
                .append("</textarea>\n")
                .append("</body></html>\n");

        setReplyBody(buffer, reply);
        OutputBuffer.release(buffer);
    }


    public static void make_401Unauthorized(Reply reply) {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer.append("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\n");
        buffer.append("<html><head>\n");
        buffer.append("<title>401 Unauthorized</title>\n");
        buffer.append("</head><body>\n");
        buffer.append("<h1>401 Unauthorized</h1>\n");
        buffer.append("<p>The requested URL was Unauthorized.</p>\n");
        buffer.append("</body></html>\n");

        setReplyBody(buffer, reply);
        OutputBuffer.release(buffer);
    }

    public static void make_404NotFound(Reply reply, Request request) {
        HeaderValueHost host = (HeaderValueHost) request.getHeaderValueObj(HeaderSort.Host);
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer.append("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\n")
                .append("<html><head>\n")
                .append("<title>404 Not Found</title>\n")
                .append("</head><body>\n")
                .append("<h1>Not Found</h1>\n")
                .append("<p>The requested URL ");
        if (host != null) {
            buffer
                    .append(host.ipOrDomain());
            if (host.port() != -1) {
                buffer.append(HttpString.Colon)
                        .appendInt(host.port());

            }
        }
        buffer
                .append(request.requestURI().toString())
                .append(" was not found on this timer.</p>\n")
                .append("</body></html>\n");
        setReplyBody(buffer, reply);
        OutputBuffer.release(buffer);
    }

    public static void make_405MethodNotAllowed(Reply reply, Request request, MethodType[] allowedMethods) {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer.append("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\n");
        buffer.append("<html><head>\n");
        buffer.append("<title>405 Method Not Allowed</title>\n");
        buffer.append("</head><body>\n");
        buffer.append("<h1>Method Not Allowed</h1>\n");
        buffer.append("<p>The requested Method " + request.method() + " was not allowed on this resource.</p>\n");
        buffer.append("<p>Allowed Methods : ");
        boolean first = true;
        for (MethodType mt : allowedMethods) {
            if (first) {
                first = false;
            } else {
                buffer.append(", ");
            }
            buffer.append(mt.getBytes());
        }
        buffer.append("</p>\n");
        buffer.append("</body></html>\n");

        setReplyBody(buffer, reply);
        OutputBuffer.release(buffer);
    }

    public static void make_406NotAcceptable(Reply reply, ResourceNegotiatedFile resource) {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer.append("<html>\r\n");
        buffer.append("<head><title>406 Not Acceptable</title></head>\r\n");
        buffer.append("<body>\r\n");
        buffer.append("<h1>Not Acceptable</h1>\r\n");
        buffer.append("Available variants:\r\n");
        buffer.append("<ul>\r\n");
        NegotiationVariant[] variants = resource.negoInfo().variants();
        for (NegotiationVariant variant : variants) {
            buffer.append("<li>");
            buffer.append("<a href=\"");
            buffer.append(variant.pathFromRoot());
            buffer.append("\">");
            buffer.append(variant.fileName());
            buffer.append("</a>");
            HeaderCondition[] conditions = variant.conditions();
            for (HeaderCondition condition : conditions) {
                buffer.append(" ,");
                buffer.append(condition.header().toString());
                buffer.append('=');
                buffer.append(condition.value());
            }
            buffer.append("</li>\r\n");
        }

        buffer.append("</ul>\r\n");
        buffer.append("</body></html>\r\n");

        setReplyBody(buffer, reply);
        OutputBuffer.release(buffer);
    }

    public static void make_407ProxyUnauthorized(Reply reply) {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer.append("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\n");
        buffer.append("<html><head>\n");
        buffer.append("<title>407 Proxy Unauthorized</title>\n");
        buffer.append("</head><body>\n");
        buffer.append("<h1>407 Proxy Unauthorized</h1>\n");
        buffer.append("<p>The requested URL was proxy unauthorized.</p>\n");
        buffer.append("</body></html>\n");

        setReplyBody(buffer, reply);
        OutputBuffer.release(buffer);
    }

    public static void make_500NotSupportedEncoding(Reply reply, ContentCodingSort coding) {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer.append("<html>\r\n");
        buffer.append("<head><title>500 Internal Server Error</title></head>\r\n");
        buffer.append("<body>\r\n");
        buffer.append("<h1>Not Supported Encoding</h1>\r\n");
        buffer.append(coding.toString()).append(" is not supported encoding.");
        buffer.append("</body></html>\r\n");

        setReplyBody(buffer, reply);
        OutputBuffer.release(buffer);
    }

    public static void make_500NotSupportedProxyProtocol(Reply reply, Protocol protocol) {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer.append("<html>\r\n");
        buffer.append("<head><title>500 Internal Server Error</title></head>\r\n");
        buffer.append("<body>\r\n");
        buffer.append("<h1>Not Supported Proxy Protocol</h1>\r\n");
        buffer.append(protocol.toString()).append(" is not supported proxy protocol.");
        buffer.append("</body></html>\r\n");

        setReplyBody(buffer, reply);
        OutputBuffer.release(buffer);
    }


    public static void make_500CannotChangeCharset(Reply reply, String sourceCharset, String targetCharset) {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer.append("<html>\r\n");
        buffer.append("<head><title>500 Internal Server Error</title></head>\r\n");
        buffer.append("<body>\r\n");
        buffer.append("<h1>CannotChangeCharset</h1>\r\n");
        buffer.append("cannot change charset from ").append(sourceCharset).append(" to ").append(targetCharset).append(".");
        buffer.append("</body></html>\r\n");

        setReplyBody(buffer, reply);
        OutputBuffer.release(buffer);
    }
}
