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

    public ServerProperties() {
        ioThreadCount = 100;

        sendServerHeader = true;

        keepAlive_timeout = 3;
        keepAlive_max = 100;
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
}