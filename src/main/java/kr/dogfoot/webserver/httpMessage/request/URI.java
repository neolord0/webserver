package kr.dogfoot.webserver.httpMessage.request;

import kr.dogfoot.webserver.server.host.proxy_info.BackendServerInfo;
import kr.dogfoot.webserver.server.host.proxy_info.Protocol;
import kr.dogfoot.webserver.util.http.HttpString;

public class URI {
    private URIType type;
    private Protocol protocol;
    private String ipOrDomain;
    private int port;
    private String path;
    private String queryString;

    public URI() {
        reset();
    }

    public void reset() {
        type = null;
        protocol = null;
        ipOrDomain = null;
        port = -1;
        path = null;
    }

    public void parse(String uri) {
        if (uri.equals(HttpString.Asterisk_String)) {
            asterisk();
        } else {
            parseURI(uri);
        }
    }

    private void parseURI(String uri) {
        if (uri.startsWith(HttpString.HttpWithPostFix)) {
            type = URIType.AbsoluteURI;
            protocol = Protocol.Http;
            parseHostAndPathAndParameters(uri.substring(HttpString.HttpWithPostFix.length()));
        } else if (uri.startsWith(HttpString.HttpsWithPostFix)) {
            type = URIType.AbsoluteURI;
            protocol = Protocol.Https;
            parseHostAndPathAndParameters(uri.substring(HttpString.HttpsWithPostFix.length()));
        } else {
            abs_path(uri);
        }
    }

    private void parseHostAndPathAndParameters(String hostAndPathAndParameters) {
        int index = hostAndPathAndParameters.indexOf(HttpString.Slash);
        parseHost(hostAndPathAndParameters.substring(0, index));
        parsePathAndQueryString(hostAndPathAndParameters.substring(index));
    }

    private void parseHost(String host) {
        int index = host.indexOf(HttpString.Colon);
        if (index == -1) {
            ipOrDomain = host;
            port = -1;
        } else {
            ipOrDomain = host.substring(0, index);
            port = Integer.parseInt(host.substring(index + 1));
        }
    }

    private void parsePathAndQueryString(String pathAndParameters) {
        int index = pathAndParameters.indexOf(HttpString.Question);
        if (index == -1) {
            path = pathAndParameters;
            queryString = null;
        } else {
            path = pathAndParameters.substring(0, index);
            queryString = pathAndParameters.substring(index + 1);
        }
    }

    private void abs_path(String uri) {
        type = URIType.Abs_Path;
        protocol = Protocol.None;
        ipOrDomain = null;
        port = -1;
        parsePathAndQueryString(uri);
    }

    private void asterisk() {
        type = URIType.Asterisk;
        protocol = Protocol.None;
        ipOrDomain = null;
        port = -1;
        path = null;
        queryString = null;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    public String toString(boolean includeQueryString) {
        switch (type) {
            case Asterisk:
                return HttpString.Asterisk_String;
            case AbsoluteURI:
                return getAbsoluteURI(includeQueryString);
            case Abs_Path:
                return getAbsPath(includeQueryString);
        }
        return null;
    }

    private String getAbsoluteURI(boolean includeQueryString) {
        StringBuffer sb = new StringBuffer();
        sb.append(protocol.toStringWithPostFix());
        sb.append(ipOrDomain);
        if (port >= 0) {
            sb.append((char) HttpString.Colon);
            sb.append(port);
        }
        sb.append(path);
        if (includeQueryString == true && queryString != null && queryString.length() > 0) {
            sb.append((char) HttpString.Question);
            sb.append(queryString);
        }
        return sb.toString();
    }

    private String getAbsPath(boolean includeQueryString) {
        StringBuffer sb = new StringBuffer();
        sb.append(path);
        if (includeQueryString == true && queryString != null && queryString.length() > 0) {
            sb.append((char) HttpString.Question);
            sb.append(queryString);
        }
        return sb.toString();
    }

    public URIType type() {
        return type;
    }

    public URI type(URIType type) {
        this.type = type;
        return this;
    }

    public Protocol protocol() {
        return protocol;
    }

    public URI protocol(Protocol protocol) {
        this.protocol = protocol;
        return this;
    }

    public String ipOrDomain() {
        return ipOrDomain;
    }

    public URI ipOrDomain(String ipOrDomain) {
        this.ipOrDomain = ipOrDomain;
        return this;
    }

    public int port() {
        return port;
    }

    public URI port(int port) {
        this.port = port;
        return this;
    }

    public String path() {
        return path;
    }

    public URI path(String path) {
        this.path = path;
        return this;
    }

    public String queryString() {
        return queryString;
    }

    public URI queryString(String queryString) {
        this.queryString = queryString;
        return this;
    }

    public void applyBackendServer(BackendServerInfo backendServerInfo) {
        type = URIType.AbsoluteURI;
        protocol = backendServerInfo.protocol();
        ipOrDomain = backendServerInfo.ipOrDomain();
        port = backendServerInfo.port();
    }
}
