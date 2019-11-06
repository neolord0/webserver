package kr.dogfoot.webserver.httpMessage.reply;

public enum ReplyCode {
    Code100(100, "100", "Continue"),
    Code101(101, "101", "Switching Protocols"),
    Code200(200, "200", "OK"),
    Code201(201, "201", "Create"),
    Code202(202, "202", "Accepted"),
    Code203(203, "203", "Non Authoritative Information"),
    Code204(204, "204", "No Body"),
    Code205(205, "205", "Reset Body"),
    Code206(206, "206", "Partial Content"),
    Code300(300, "300", "Multiple Choices"),
    Code301(301, "301", "Moved Permanently"),
    Code302(302, "302", "Found"),
    Code303(303, "303", "See Other"),
    Code304(304, "304", "Not Modified"),
    Code305(305, "305", "Use Proxy"),
    Code307(307, "307", "Temporary Redirect"),
    Code308(308, "308", "Permanent Redirect"),
    Code400(400, "400", "Bad Request"),
    Code401(401, "401", "Unauthorized"),
    Code402(402, "402", "Payment Required"),
    Code403(403, "403", "Forbidden"),
    Code404(404, "404", "Not Found"),
    Code405(405, "405", "Method Not Allowed"),
    Code406(406, "406", "Not Acceptable"),
    Code407(407, "407", "Proxy Authentication Required"),
    Code408(408, "408", "Request Time-out"),
    Code409(409, "409", "Conflict"),
    Code410(410, "410", "Gone"),
    Code411(411, "411", "Length Required"),
    Code412(412, "412", "Precondition Failed"),
    Code413(413, "413", "Request Entity Too Large"),
    Code414(414, "414", "Request-URI Too Large"),
    Code415(415, "415", "Unsupported Media Type"),
    Code416(416, "416", "Requested range not satisfiable"),
    Code417(417, "417", "Expectation Failed"),
    Code500(500, "500", "Internal Server Error"),
    Code501(501, "501", "Not Implemented"),
    Code502(502, "502", "Bad Gateway"),
    Code503(503, "503", "Service Unavailable"),
    Code504(504, "504", "Gateway Time-out"),
    Code505(505, "505", "HTTP Version not supportedCode"),
    ExtensionCode(0, "", "");

    private short code;
    private byte[] codeByte;
    private byte[] defaultReason;

    ReplyCode(int code, String code2, String defaultReason) {
        this.code = (short) code;
        this.codeByte = code2.getBytes();
        this.defaultReason = defaultReason.getBytes();
    }

    public static ReplyCode fromCode(short code) {
        for (ReplyCode rc : values()) {
            if (rc.code == code) {
                return rc;
            }
        }
        return getExtensionCode(code);
    }

    private static ReplyCode getExtensionCode(Short code) {
        ReplyCode extension = ReplyCode.ExtensionCode;
        extension.code = code;
        extension.codeByte = code.toString().getBytes();
        extension.defaultReason = extension.codeByte;
        return extension;
    }

    public int getCode() {
        return code;
    }

    public byte[] getCodeByte() {
        return codeByte;
    }

    public byte[] getDefaultReason() {
        return defaultReason;
    }

    public boolean isError() {
        return code > 400;
    }
}
