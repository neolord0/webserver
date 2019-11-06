package kr.dogfoot.webserver.context;

public enum ContextState {
    Waiting,
    SSLHandshaking,
    ReceivingRequest,
    PerformingRequest,
    ReceivingBody,
    SendingReply,
    ConnectingProxier,
    ProxyingAjp,
    ProxyingHttp,
    Released
}
