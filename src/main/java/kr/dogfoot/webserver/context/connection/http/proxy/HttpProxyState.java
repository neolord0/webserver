package kr.dogfoot.webserver.context.connection.http.proxy;

public enum HttpProxyState {
    Idle,
    SendRequestBodyChunk,
    ReceivingResponse,
    ReceivingResponseBody,
    Close
}
