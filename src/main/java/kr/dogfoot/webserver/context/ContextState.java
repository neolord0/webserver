package kr.dogfoot.webserver.context;

public enum ContextState {
    Waiting,
    SSLHandshaking,
    ReceivingRequest,
    PerformingRequest,
    ReceivingBody,
    SendingResponse,
    ConnectingProxier,
    ProxyingAjp,
    ProxyingHttp,
    Released
}
