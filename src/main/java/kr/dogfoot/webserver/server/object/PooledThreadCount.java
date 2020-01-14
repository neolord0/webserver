package kr.dogfoot.webserver.server.object;

public class PooledThreadCount {
    private int ssl_handshaking;
    private int request_receiving;
    private int body_receiving;
    private int request_performing;
    private int response_sending;
    private int file_reading;
    private int buffer_sending;
    private int proxy_connecting;
    private int ajp_proxing;
    private int http_proxing;

    public PooledThreadCount() {
        ssl_handshaking = 1;
        request_receiving = 1;
        body_receiving = 1;
        request_performing = 1;
        response_sending = 1;
        file_reading = 1;
        buffer_sending = 1;
        proxy_connecting = 1;
        ajp_proxing = 1;
        http_proxing = 1;
    }

    public int ssl_handshaking() {
        return ssl_handshaking;
    }

    public void ssl_handshaking(int ssl_handshaking) {
        this.ssl_handshaking = ssl_handshaking;
    }

    public int request_receiving() {
        return request_receiving;
    }

    public void request_receiving(int request_receiving) {
        this.request_receiving = request_receiving;
    }

    public int body_receiving() {
        return body_receiving;
    }

    public void body_receiving(int body_receiving) {
        this.body_receiving = body_receiving;
    }

    public int request_performing() {
        return request_performing;
    }

    public void request_performing(int request_performing) {
        this.request_performing =  request_performing;
    }

    public int response_sending() {
        return response_sending;
    }

    public void response_sending(int response_sending) {
        this.response_sending = response_sending;
    }

    public int file_reading() {
        return file_reading;
    }

    public void file_reading(int file_reading) {
        this.file_reading = file_reading;
    }

    public int buffer_sending() {
        return buffer_sending;
    }

    public void buffer_sending(int buffer_sending) {
        this.buffer_sending = buffer_sending;
    }

    public int proxy_connecting() {
        return proxy_connecting;
    }

    public void proxy_connecting(int proxy_connecting) {
        this.proxy_connecting = proxy_connecting;
    }

    public int ajp_proxing() {
        return ajp_proxing;
    }

    public void ajp_proxing(int ajp_proxing) {
        this.ajp_proxing = ajp_proxing;
    }

    public int http_proxing() {
        return http_proxing;
    }

    public void http_proxing(int http_proxing) {
        this.http_proxing = http_proxing;
    }
}
