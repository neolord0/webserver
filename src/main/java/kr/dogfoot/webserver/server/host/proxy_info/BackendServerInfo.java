package kr.dogfoot.webserver.server.host.proxy_info;

import java.net.InetSocketAddress;

public class BackendServerInfo {
    private Protocol protocol;
    private String ipOrDomain;
    private int port;
    private InetSocketAddress socketAddress;
    private int keepAlive_timeout;
    private int idle_timeout;

    public Protocol protocol() {
        return protocol;
    }

    public void protocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public void protocol(String protocol) {
        this.protocol = Protocol.fromString(protocol);
    }

    public boolean isAjp() {
        return protocol == Protocol.Ajp13;
    }

    public boolean isHttp() {
        return protocol == Protocol.Http;
    }

    public String ipOrDomain() {
        return ipOrDomain;
    }

    public int port() {
        return port;
    }

    public void address(String ipOrDomain, int port) {
        this.ipOrDomain = ipOrDomain;
        this.port = port;
        socketAddress = new InetSocketAddress(ipOrDomain, port);
    }

    public InetSocketAddress socketAddress() {
        return socketAddress;
    }

    public int keepAlive_timeout() {
        return keepAlive_timeout;
    }

    public void keepAlive_timeout(int timeoutSecond) {
        this.keepAlive_timeout = timeoutSecond;
    }

    public int idle_timeout() {
        return idle_timeout;
    }

    public void idle_timeout(int idle_timeout) {
        this.idle_timeout = idle_timeout;
    }
}
