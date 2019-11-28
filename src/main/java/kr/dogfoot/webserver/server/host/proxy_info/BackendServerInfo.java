package kr.dogfoot.webserver.server.host.proxy_info;

import java.net.InetSocketAddress;

public class BackendServerInfo {
    private ProxyInfo proxyInfo;
    private int index;

    private Protocol protocol;
    private String ipOrDomain;
    private int port;
    private InetSocketAddress socketAddress;
    private int keepAlive_timeout;
    private int idle_timeout;

    private volatile int connectCount;

    public BackendServerInfo(ProxyInfo proxyInfo, int index) {
        this.proxyInfo = proxyInfo;
        this.index = index;

        connectCount = 0;
    }

    public int index() {
        return index;
    }

    public ProxyInfo proxyInfo() {
        return proxyInfo;
    }

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

    public String address() {
        return socketAddress.toString();
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

    public int connectCount() {
        return connectCount;
    }

    public synchronized void increaseConnectCount() {
        connectCount++;
    }

    public synchronized void decreaseConnectCount() {
        connectCount--;
    }
}
