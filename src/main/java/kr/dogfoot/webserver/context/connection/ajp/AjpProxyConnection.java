package kr.dogfoot.webserver.context.connection.ajp;

import kr.dogfoot.webserver.context.connection.Connection;
import kr.dogfoot.webserver.context.connection.ConnectionSort;
import kr.dogfoot.webserver.server.timer.Timer;
import kr.dogfoot.webserver.server.timer.TimerEventHandler;

public class AjpProxyConnection extends Connection {
    public static final int MaxPacketSize = 8192;
    public static final int RequestBodyChunk_HeaderSize = 6;
    public static final int MaxRequestBodyChunkSize = MaxPacketSize - RequestBodyChunk_HeaderSize;

    private AjpProxyState state;

    private short packetSize;

    private int bodyChunkSize;

    private boolean replyHasContentLength;

    private Object timerEventForIdle;

    public AjpProxyConnection(int id) {
        super(id);

        state = AjpProxyState.NoConnect;

        packetSize = 0;
        bodyChunkSize = 0;
        replyHasContentLength = false;

        timerEventForIdle = null;
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

        state = AjpProxyState.NoConnect;

        packetSize = 0;
        bodyChunkSize = 0;
        replyHasContentLength = false;
        timerEventForIdle = null;
    }

    public AjpProxyConnection resetForIdled() {
        context = null;
        receiveBuffer.clear();

        state = AjpProxyState.Idle;
        packetSize = 0;

        bodyBuffer = null;
        bodyChunkSize = 0;

        replyHasContentLength = false;

        return this;
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

    public int bodyChunkSize() {
        return bodyChunkSize;
    }

    public void bodyChunkSize(int bodyChunkSize) {
        this.bodyChunkSize = bodyChunkSize;
    }

    public void addBodyChunkSize(int size) {
        bodyChunkSize += size;
    }

    public boolean replyHasContentLength() {
        return replyHasContentLength;
    }

    public void replyHasContentLength(boolean replyHasContentLength) {
        this.replyHasContentLength = replyHasContentLength;
    }

    public void setTimerForIdle(Timer timer, int timeout, TimerEventHandler handler) {
        timerEventForIdle = timer.addEvent(timeout, handler, this);
    }

    public void killTimerForIdle(Timer timer) {
        if (timerEventForIdle != null) {
            timer.removeEvent(timerEventForIdle);
            timerEventForIdle = null;
        }
    }
}