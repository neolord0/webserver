package kr.dogfoot.webserver.server;

public interface Startable {
    void start() throws Exception;

    void terminate() throws Exception;
}
