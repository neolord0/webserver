package kr.dogfoot.webserver.context.connection.ajp;

public enum AjpProxyState {
    NoConnect,
    Idle,
    ReceivePacketHeader,
    ReceivePacketBody,
    SendBody
}
