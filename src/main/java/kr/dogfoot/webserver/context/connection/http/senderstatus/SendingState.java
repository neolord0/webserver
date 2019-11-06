package kr.dogfoot.webserver.context.connection.http.senderstatus;

public enum SendingState {
    BeforeBody,
    Body,
    BodyEnd,
}
