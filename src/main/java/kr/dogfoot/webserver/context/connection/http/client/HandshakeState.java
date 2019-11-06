package kr.dogfoot.webserver.context.connection.http.client;

public enum HandshakeState {
    NotBegin,
    Handshaking,
    ReceiveData,
    SendData,
    SendDataAtLast,
    Success,
    Fail
}
