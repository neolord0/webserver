package kr.dogfoot.webserver.context.connection.ajp;

import kr.dogfoot.webserver.context.connection.Connection;
import kr.dogfoot.webserver.context.connection.ConnectionSort;
import kr.dogfoot.webserver.server.host.proxy_info.BackendServerInfo;
import kr.dogfoot.webserver.server.timer.Timer;
import kr.dogfoot.webserver.server.timer.TimerEventHandler;

public class AjpProxyConnection extends Connection {
    public static final int MaxPacketSize = 8192;
    public static final int RequestBodyChunk_HeaderSize = 6;
    public static final int MaxRequestBodyChunkSize = MaxPacketSize - RequestBodyChunk_HeaderSize;

    private BackendServerInfo backendServerInfo;

    private AjpProxyState state;

    private short packetSize;
    private long bodyChunkSize;
    private boolean responseHasContentLength;

    private Object timerEventForIdle;


    public AjpProxyConnection(int id) {
        super(id);
    }

    @Override
    public ConnectionSort sort() {
        return ConnectionSort.AjpProxyConnection;
    }

    @Override
    public int receiveBufferSize() {
        return MaxPacketSize;
    }

    @Override
    public void resetForPooled() {
        super.resetForPooled();

        backendServerInfo = null;

        state = AjpProxyState.NoConnect;

        packetSize = 0;
        bodyChunkSize = 0;
        responseHasContentLength = false;

        timerEventForIdle = null;
    }

    public AjpProxyConnection resetForIdled() {
        context = null;
        receiveBuffer.clear();

        state = AjpProxyState.Idle;
        packetSize = 0;

        bodyBuffer = null;
        bodyChunkSize = 0;
        responseHasContentLength = false;

        return this;
    }

    public BackendServerInfo backendServerInfo() {
        return backendServerInfo;
    }

    public void backendServerInfo(BackendServerInfo backendServerInfo) {
        this.backendServerInfo = backendServerInfo;
    }

    public AjpProxyState state() {
        return state;
    }

    public void changeState(AjpProxyState state) {
        this.state = state;
    }

    public boolean stateIsNoConnect() {
        return state == AjpProxyState.NoConnect;
    }

    public short packetSize() {
        return packetSize;
    }

    public void packetSize(short packetSize) {
        this.packetSize = packetSize;
    }

    public long bodyChunkSize() {
        return bodyChunkSize;
    }

    public void bodyChunkSize(long bodyChunkSize) {
        this.bodyChunkSize = bodyChunkSize;
    }

    public void addBodyChunkSize(long size) {
        bodyChunkSize += size;
    }

    public boolean responseHasContentLength() {
        return responseHasContentLength;
    }

    public void responseHasContentLength(boolean responseHasContentLength) {
        this.responseHasContentLength = responseHasContentLength;
    }

    public void setTimerForIdle(Timer timer, long timeout, TimerEventHandler handler) {
        timerEventForIdle = timer.addEvent(timeout, handler, this);
    }

    public void killTimerForIdle(Timer timer) {
        if (timerEventForIdle != null) {
            timer.removeEvent(timerEventForIdle);
            timerEventForIdle = null;
        }
    }
}