package kr.dogfoot.webserver.server.timer;

public interface TimerEventHandler {
    void HandleTimerEvent(Object data, long time);
}
