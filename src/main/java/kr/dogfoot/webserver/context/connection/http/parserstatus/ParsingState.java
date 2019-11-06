package kr.dogfoot.webserver.context.connection.http.parserstatus;

public enum ParsingState {
    FirstCRLF,
    Method,
    RequestURI,
    Version,
    CRLF,
    HeaderName,
    HeaderValue,
    HeaderContinue,
    HeaderValueContinue,
    HeaderEnd,
    BodyStart,
    StatusCode,
    Reason
}
