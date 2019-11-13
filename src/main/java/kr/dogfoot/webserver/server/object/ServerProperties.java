package kr.dogfoot.webserver.server.object;

public class ServerProperties {
    private static byte[] serverInfos;

    static {
        serverInfos = "My WebServer 1.0".getBytes();
    }

    private int ioThreadCount;
    private boolean sendServerHeader;
    private int keepAlive_timeout;
    private int keepAlive_max;

    private int countOfSSLHandshaker;
    private int countOfRequestReceiver;
    private int countOfBodyReceiver;
    private int countOfRequestPerformer;
    private int countOfReplySender;
    private int countOfBufferSender;
    private int countOfProxyConnector;
    private int countOfAjpProxier;
    private int countOfHttpProxier;

    public ServerProperties() {
        ioThreadCount = 100;

        sendServerHeader = true;

        keepAlive_timeout = 3;
        keepAlive_max = 100;

        countOfSSLHandshaker = 1;
        countOfRequestReceiver = 1;
        countOfBodyReceiver = 1;
        countOfRequestPerformer = 1;
        countOfReplySender = 1;
        countOfProxyConnector = 1;
        countOfAjpProxier = 1;
        countOfHttpProxier = 1;
    }

    public int ioThreadCount() {
        return ioThreadCount;
    }

    public void ioThreadCount(int ioThreadCount) {
        this.ioThreadCount = ioThreadCount;
    }

    public boolean sendServerHeader() {
        return sendServerHeader;
    }

    public void sendServerHeader(boolean sendServerHeader) {
        this.sendServerHeader = sendServerHeader;
    }

    public byte[] serverInfos() {
        return serverInfos;
    }

    public int keepAlive_timeout() {
        return keepAlive_timeout;
    }

    public void keepAlive_timeout(int timeoutSecound) {
        this.keepAlive_timeout = timeoutSecound;
    }

    public int keepAlive_max() {
        return keepAlive_max;
    }

    public void keepAlive_max(int keepAlive_max) {
        this.keepAlive_max = keepAlive_max;
    }

    public int countOfSSLHandshaker() {
        return countOfSSLHandshaker;
    }

    public void countOfSSLHandshaker(int countOfSSLHandshaker) {
        this.countOfSSLHandshaker = countOfSSLHandshaker;
    }

    public int countOfRequestReceiver() {
        return countOfRequestReceiver;
    }

    public void countOfRequestReceiver(int countOfRequestReceiver) {
        this.countOfRequestReceiver = countOfRequestReceiver;
    }

    public int countOfBodyReceiver() {
        return countOfBodyReceiver;
    }

    public void countOfBodyReceiver(int countOfBodyReceiver) {
        this.countOfBodyReceiver = countOfBodyReceiver;
    }

    public int countOfRequestPerformer() {
        return countOfRequestPerformer;
    }

    public void countOfRequestPerformer(int countOfRequestPerformer) {
        this.countOfRequestPerformer = countOfRequestPerformer;
    }

    public int countOfReplySender() {
        return countOfReplySender;
    }

    public void countOfReplySender(int countOfReplySender) {
        this.countOfReplySender = countOfReplySender;
    }

    public int countOfBufferSender() {
        return countOfBufferSender;
    }
    public void countOfBufferSender(int countOfBufferSender) {
        this.countOfBufferSender = countOfBufferSender;
    }


    public int countOfProxyConnector() {
        return countOfProxyConnector;
    }

    public void countOfProxyConnector(int countOfProxyConnector) {
        this.countOfProxyConnector = countOfProxyConnector;
    }

    public int countOfAjpProxier() {
        return countOfAjpProxier;
    }

    public void countOfAjpProxier(int countOfAjpProxier) {
        this.countOfAjpProxier = countOfAjpProxier;
    }

    public int countOfHttpProxier() {
        return countOfHttpProxier;
    }

    public void countOfHttpProxier(int countOfHttpProxier) {
        this.countOfHttpProxier = countOfHttpProxier;
    }

}