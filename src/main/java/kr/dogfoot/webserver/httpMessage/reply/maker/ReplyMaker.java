package kr.dogfoot.webserver.httpMessage.reply.maker;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.FactoryForHeaderValue;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueAllow;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.ContentCodingSort;
import kr.dogfoot.webserver.httpMessage.reply.Reply;
import kr.dogfoot.webserver.httpMessage.reply.ReplyCode;
import kr.dogfoot.webserver.httpMessage.reply.maker.DefaultMessageBody;
import kr.dogfoot.webserver.httpMessage.request.MethodType;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.server.object.ServerProperties;
import kr.dogfoot.webserver.server.resource.ResourceFile;
import kr.dogfoot.webserver.server.resource.ResourceNegotiatedFile;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpDateMaker;
import kr.dogfoot.webserver.util.http.HttpString;

import java.util.Date;

public class ReplyMaker {
    private static final String Error_500CannotConnectWAS = "cannot connect WAS.";
    private static final String Error_500DisconnectWAS = "disconnected WAS.";
    private static final String Error__500CannotConnectWS = "cannot connect Web Server.";
    private static final String Error_500DisconnectWS = "disconnected Web Server.";
    private static final String Error_500TooManyFileOpen = "too many file open.";
    private static final String Error_417ExpectationFail = "expectation fail.";
    private Reply _500CannotConnectWAS;
    private Reply _500DisconnectWAS;
    private Reply _500CannotConnectWS;
    private Reply _500DisconnectWS;
    private Reply _500TooManyFileOpen;
    private Reply _417ExpectationFail;
    private ServerProperties serverProperties;

    public ReplyMaker(ServerProperties serverProperties) {
        this.serverProperties = serverProperties;

        createStaticReply();
    }

    private void createStaticReply() {
        _500CannotConnectWAS = new_500InternalServerError(Error_500CannotConnectWAS);
        _500DisconnectWAS = new_500InternalServerError(Error_500DisconnectWAS);
        _500CannotConnectWS = new_500InternalServerError(Error__500CannotConnectWS);
        _500DisconnectWS = new_500InternalServerError(Error_500DisconnectWS);
        _500TooManyFileOpen = new_500InternalServerError("too many file open.");
        _417ExpectationFail = new_417ExpectationFail();
    }

    private Reply new_500InternalServerError(String message) {
        Reply reply = new Reply().code(ReplyCode.Code500);
        addHeader_Date(reply);
        addHeader_Server(reply);

        DefaultMessageBody.make(reply, message);
        return reply;
    }

    public void addHeader_Date(Reply reply) {
        byte[] dateBytes = HttpDateMaker.makeBytes(new Date().getTime());
        reply.addHeader(HeaderSort.Date, dateBytes);
    }

    public void addHeader_Server(Reply reply) {
        if (serverProperties.sendServerHeader()) {
            reply.addHeader(HeaderSort.Server, serverProperties.serverInfos());
        }
    }

    private Reply new_417ExpectationFail() {
        Reply reply = new Reply().code(ReplyCode.Code417);
        addHeader_Date(reply);
        addHeader_Server(reply);

        DefaultMessageBody.make(reply, Error_417ExpectationFail);
        return reply;
    }

    public Reply new_200OK() {
        Reply reply = new Reply().code(ReplyCode.Code200);
        addHeader_Date(reply);
        addHeader_Server(reply);
        return reply;
    }

    public Reply new_DefalutPostReply(Request request) {
        Reply reply = new_200OK();
        addHeader_Date(reply);
        addHeader_Server(reply);

        DefaultMessageBody.makeForPostReply(reply, request);
        return reply;
    }

    public Reply new_RedirectReply(ReplyCode replyCode, String targetURL) {
        Reply reply = new Reply().code(replyCode);
        addHeader_Date(reply);
        addHeader_Server(reply);
        reply.addHeader(HeaderSort.Location, targetURL.getBytes());
        reply.addHeader(HeaderSort.Cache_Control, HttpString.No_Cache);
        reply.addHeader(HeaderSort.Connection, HttpString.Close);
        reply.addHeader(HeaderSort.Pragma, HttpString.No_Cache);

        return reply;
    }

    public Reply new_304NotModified(ResourceFile resource) {
        Reply reply = new Reply().code(ReplyCode.Code304);
        addHeader_Date(reply);
        addHeader_Server(reply);
        reply.addHeader(HeaderSort.ETag, resource.getETag());
        return reply;
    }

    public Reply new_400BadRequest(String message) {
        Reply reply = new Reply().code(ReplyCode.Code304);
        addHeader_Date(reply);
        addHeader_Server(reply);

        DefaultMessageBody.make(reply, message);
        return reply;
    }

    public Reply new_401Unauthorized(String authorizationType, String realmDescription) {
        Reply reply = new Reply().code(ReplyCode.Code401);
        addHeader_Date(reply);
        addHeader_Server(reply);
        addHeader_WWWAuthenticate(reply, authorizationType, realmDescription);

        DefaultMessageBody.make_401Unauthorized(reply);
        return reply;
    }

    private static void addHeader_WWWAuthenticate(Reply reply, String authorizationType, String realmDescription) {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer
                .append(authorizationType.getBytes())
                .append(HttpString.Space)
                .append(HttpString.Realm)
                .append(HttpString.Equal)
                .appendQuoted(realmDescription);
        reply.addHeader(HeaderSort.WWW_Authenticate, buffer.getBytes());
        OutputBuffer.release(buffer);
    }

    public Reply new_404NotFound(Request request) {
        Reply reply = new Reply().code(ReplyCode.Code404);
        addHeader_Date(reply);
        addHeader_Server(reply);

        DefaultMessageBody.make_404NotFound(reply, request);
        return reply;
    }

    public Reply new_405MethodNotAllowed(Request request, MethodType[] allowedMethods) {
        Reply reply = new Reply().code(ReplyCode.Code405);
        addHeader_Date(reply);
        addHeader_Server(reply);
        addHeader_Allow(reply, allowedMethods);

        DefaultMessageBody.make_405MethodNotAllowed(reply, request, allowedMethods);
        return reply;
    }

    private static void addHeader_Allow(Reply reply, MethodType[] allowedMethods) {
        HeaderValueAllow allow = (HeaderValueAllow) FactoryForHeaderValue.create(HeaderSort.Allow);
        for (MethodType mt : allowedMethods) {
            allow.addMethodType(mt);
        }
        reply.addHeader(HeaderSort.Allow, allow.combineValue());
    }

    public Reply new_406NotAcceptable(ResourceNegotiatedFile resource) {
        Reply reply = new Reply().code(ReplyCode.Code406);
        addHeader_Date(reply);
        addHeader_Server(reply);

        DefaultMessageBody.make_406NotAcceptable(reply, resource);
        return reply;
    }

    public Reply new_407Unauthorized(String authorizationType, String realmDescription) {
        Reply reply = new Reply().code(ReplyCode.Code407);
        addHeader_Date(reply);
        addHeader_Server(reply);
        addHeader_ProxyAuthenticate(reply, authorizationType, realmDescription);

        DefaultMessageBody.make_407ProxyUnauthorized(reply);
        return reply;
    }

    private void addHeader_ProxyAuthenticate(Reply reply, String authorizationType, String realmDescription) {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer
                .append(authorizationType.getBytes())
                .append(HttpString.Space)
                .append(HttpString.Realm)
                .append(HttpString.Equal)
                .appendQuoted(realmDescription);

        reply.addHeader(HeaderSort.Proxy_Authenticate, buffer.getBytes());
        OutputBuffer.release(buffer);
    }

    public Reply new_412PreconditionFailed(ResourceFile resource) {
        Reply reply = new Reply().code(ReplyCode.Code412);
        addHeader_Date(reply);
        addHeader_Server(reply);
        reply.addHeader(HeaderSort.Last_Modified, HttpDateMaker.makeBytes(resource.lastModified()));
        reply.addHeader(HeaderSort.ETag, resource.etag());

        return reply;
    }

    public Reply get_417ExpectationFail() {
        return _417ExpectationFail;
    }

    public Reply new_500NotSupportedEncoding(ContentCodingSort coding) {
        Reply reply = new Reply().code(ReplyCode.Code500);
        addHeader_Date(reply);
        addHeader_Server(reply);

        DefaultMessageBody.make_500NotSupportedEncoding(reply, coding);
        return reply;
    }

    public Reply new_500CannotChangeCharset(String sourceCharset, String targetCharset) {
        Reply reply = new Reply().code(ReplyCode.Code500);
        addHeader_Date(reply);
        addHeader_Server(reply);

        DefaultMessageBody.make_500CannotChangeCharset(reply, sourceCharset, targetCharset);
        return reply;
    }

    public Reply get_500CannotConnectWAS() {
        return _500CannotConnectWAS;
    }

    public Reply get_500DisconnectWAS() {
        return _500DisconnectWAS;
    }

    public Reply get_500CannotConnectWS() {
        return _500CannotConnectWS;
    }

    public Reply get_500DisconnectWS() {
        return _500DisconnectWS;
    }

    public Reply get_500TooManyFileOpen() {
        return _500TooManyFileOpen;
    }
}
