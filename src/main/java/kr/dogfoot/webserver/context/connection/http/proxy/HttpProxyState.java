package kr.dogfoot.webserver.context.connection.http.proxy;

public enum HttpProxyState {
    Idle,
    SendRequestBodyChunk,
    ReceivingReply,
    ReceivingReplyBody,
    Close
}
