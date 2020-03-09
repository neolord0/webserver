package kr.dogfoot.webserver.server.timer;

public interface TimerEventHandler {
    void handleTimerEvent(Object data, long time);
}
