package kr.dogfoot.webserver.httpMessage.response.maker;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.FactoryForHeaderValue;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueAllow;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.ContentCodingSort;
import kr.dogfoot.webserver.httpMessage.response.Response;
import kr.dogfoot.webserver.httpMessage.response.StatusCode;
import kr.dogfoot.webserver.httpMessage.request.MethodType;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.server.host.proxy_info.Protocol;
import kr.dogfoot.webserver.server.object.ServerProperties;
import kr.dogfoot.webserver.server.resource.ResourceFile;
import kr.dogfoot.webserver.server.resource.ResourceNegotiatedFile;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpDateMaker;
import kr.dogfoot.webserver.util.http.HttpString;

import java.util.Date;

public class ResponseMaker {
    private static final String Error_500CannotConnectWAS = "cannot connect WAS.";
    private static final String Error_500DisconnectWAS = "disconnected WAS.";
    private static final String Error__500CannotConnectWS = "cannot connect Web Server.";
    private static final String Error_500DisconnectWS = "disconnected Web Server.";
    private static final String Error_500TooManyFileOpen = "too many file open.";
    private static final String Error_417ExpectationFail = "expectation fail.";
    private Response _500CannotConnectWAS;
    private Response _500DisconnectWAS;
    private Response _500CannotConnectWS;
    private Response _500DisconnectWS;
    private Response _500TooManyFileOpen;
    private Response _417ExpectationFail;
    private ServerProperties serverProperties;

    public ResponseMaker(ServerProperties serverProperties) {
        this.serverProperties = serverProperties;

        createStaticResponse();
    }

    private void createStaticResponse() {
        _500CannotConnectWAS = new_500InternalServerError(Error_500CannotConnectWAS);
        _500DisconnectWAS = new_500InternalServerError(Error_500DisconnectWAS);
        _500CannotConnectWS = new_500InternalServerError(Error__500CannotConnectWS);
        _500DisconnectWS = new_500InternalServerError(Error_500DisconnectWS);
        _500TooManyFileOpen = new_500InternalServerError("too many file open.");
        _417ExpectationFail = new_417ExpectationFail();
    }

    private Response new_500InternalServerError(String message) {
        Response response = new Response().code(StatusCode.Code500);
        addHeader_Date(response);
        addHeader_Server(response);

        DefaultMessageBody.make(response, message);
        return response;
    }

    public void addHeader_Date(Response response) {
        byte[] dateBytes = HttpDateMaker.makeBytes(new Date().getTime());
        response.addHeader(HeaderSort.Date, dateBytes);
    }

    public void addHeader_Server(Response response) {
        if (serverProperties.sendServerHeader()) {
            response.addHeader(HeaderSort.Server, serverProperties.serverInfos());
        }
    }

    private Response new_417ExpectationFail() {
        Response response = new Response().code(StatusCode.Code417);
        addHeader_Date(response);
        addHeader_Server(response);

        DefaultMessageBody.make(response, Error_417ExpectationFail);
        return response;
    }

    public Response new_200OK() {
        Response response = new Response().code(StatusCode.Code200);
        addHeader_Date(response);
        addHeader_Server(response);
        return response;
    }

    public Response new_DefalutPost(Request request) {
        Response response = new_200OK();
        addHeader_Date(response);
        addHeader_Server(response);

        DefaultMessageBody.makeForPostResponse(response, request);
        return response;
    }

    public Response new_Redirect(StatusCode statusCode, String targetURL) {
        Response response = new Response().code(statusCode);
        addHeader_Date(response);
        addHeader_Server(response);
        response.addHeader(HeaderSort.Location, targetURL.getBytes());
        response.addHeader(HeaderSort.Cache_Control, HttpString.No_Cache);
        response.addHeader(HeaderSort.Connection, HttpString.Close);
        response.addHeader(HeaderSort.Pragma, HttpString.No_Cache);

        return response;
    }

    public Response new_304NotModified(ResourceFile resource) {
        Response response = new Response().code(StatusCode.Code304);
        addHeader_Date(response);
        addHeader_Server(response);
        response.addHeader(HeaderSort.ETag, resource.getETag());
        return response;
    }

    public Response new_400BadRequest(String message) {
        Response response = new Response().code(StatusCode.Code304);
        addHeader_Date(response);
        addHeader_Server(response);

        DefaultMessageBody.make(response, message);
        return response;
    }

    public Response new_401Unauthorized(String authorizationType, String realmDescription) {
        Response response = new Response().code(StatusCode.Code401);
        addHeader_Date(response);
        addHeader_Server(response);
        addHeader_WWWAuthenticate(response, authorizationType, realmDescription);

        DefaultMessageBody.make_401Unauthorized(response);
        return response;
    }

    private static void addHeader_WWWAuthenticate(Response response, String authorizationType, String realmDescription) {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer
                .append(authorizationType.getBytes())
                .append(HttpString.Space)
                .append(HttpString.Realm)
                .append(HttpString.Equal)
                .appendQuoted(realmDescription);
        response.addHeader(HeaderSort.WWW_Authenticate, buffer.getBytes());
        OutputBuffer.release(buffer);
    }

    public Response new_404NotFound(Request request) {
        Response response = new Response().code(StatusCode.Code404);
        addHeader_Date(response);
        addHeader_Server(response);

        DefaultMessageBody.make_404NotFound(response, request);
        return response;
    }

    public Response new_405MethodNotAllowed(Request request, MethodType[] allowedMethods) {
        Response response = new Response().code(StatusCode.Code405);
        addHeader_Date(response);
        addHeader_Server(response);
        addHeader_Allow(response, allowedMethods);

        DefaultMessageBody.make_405MethodNotAllowed(response, request, allowedMethods);
        return response;
    }

    private static void addHeader_Allow(Response response, MethodType[] allowedMethods) {
        HeaderValueAllow allow = (HeaderValueAllow) FactoryForHeaderValue.create(HeaderSort.Allow);
        for (MethodType mt : allowedMethods) {
            allow.addMethodType(mt);
        }
        response.addHeader(HeaderSort.Allow, allow.combineValue());
    }

    public Response new_406NotAcceptable(ResourceNegotiatedFile resource) {
        Response response = new Response().code(StatusCode.Code406);
        addHeader_Date(response);
        addHeader_Server(response);

        DefaultMessageBody.make_406NotAcceptable(response, resource);
        return response;
    }

    public Response new_407Unauthorized(String authorizationType, String realmDescription) {
        Response response = new Response().code(StatusCode.Code407);
        addHeader_Date(response);
        addHeader_Server(response);
        addHeader_ProxyAuthenticate(response, authorizationType, realmDescription);

        DefaultMessageBody.make_407ProxyUnauthorized(response);
        return response;
    }

    private void addHeader_ProxyAuthenticate(Response response, String authorizationType, String realmDescription) {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer
                .append(authorizationType.getBytes())
                .append(HttpString.Space)
                .append(HttpString.Realm)
                .append(HttpString.Equal)
                .appendQuoted(realmDescription);

        response.addHeader(HeaderSort.Proxy_Authenticate, buffer.getBytes());
        OutputBuffer.release(buffer);
    }

    public Response new_412PreconditionFailed(ResourceFile resource) {
        Response response = new Response().code(StatusCode.Code412);
        addHeader_Date(response);
        addHeader_Server(response);
        response.addHeader(HeaderSort.Last_Modified, HttpDateMaker.makeBytes(resource.lastModified()));
        response.addHeader(HeaderSort.ETag, resource.etag());

        return response;
    }

    public Response get_417ExpectationFail() {
        return _417ExpectationFail;
    }

    public Response new_500NotSupportedEncoding(ContentCodingSort coding) {
        Response response = new Response().code(StatusCode.Code500);
        addHeader_Date(response);
        addHeader_Server(response);

        DefaultMessageBody.make_500NotSupportedEncoding(response, coding);
        return response;
    }

    public Response new_500NotSupportedProxyProtocol(Protocol protocol) {
        Response response = new Response().code(StatusCode.Code500);
        addHeader_Date(response);
        addHeader_Server(response);

        DefaultMessageBody.make_500NotSupportedProxyProtocol(response, protocol);
        return response;
    }

    public Response new_500CannotChangeCharset(String sourceCharset, String targetCharset) {
        Response response = new Response().code(StatusCode.Code500);
        addHeader_Date(response);
        addHeader_Server(response);

        DefaultMessageBody.make_500CannotChangeCharset(response, sourceCharset, targetCharset);
        return response;
    }

    public Response get_500CannotConnectWAS() {
        return _500CannotConnectWAS;
    }

    public Response get_500DisconnectWAS() {
        return _500DisconnectWAS;
    }

    public Response get_500CannotConnectWS() {
        return _500CannotConnectWS;
    }

    public Response get_500DisconnectWS() {
        return _500DisconnectWS;
    }

    public Response get_500TooManyFileOpen() {
        return _500TooManyFileOpen;
    }
}

