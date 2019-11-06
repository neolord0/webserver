package kr.dogfoot.webserver.httpMessage.request;

import kr.dogfoot.webserver.context.connection.http.parserstatus.ParsingBuffer;
import kr.dogfoot.webserver.util.bytes.BytesUtil;

public enum MethodType {
    Unknown("", (byte) 0),
    OPTIONS("OPTIONS", (byte) 1),
    GET("GET", (byte) 2),
    HEAD("HEAD", (byte) 3),
    POST("POST", (byte) 4),
    PUT("PUT", (byte) 5),
    DELETE("DELETE", (byte) 6),
    TRACE("TRACE", (byte) 7),
    PROPFIND("PROPFIND", (byte) 8),
    PROPPATCH("PROPPATCH", (byte) 9),
    MKCOL("MKCOL", (byte) 10),
    COPY("COPY", (byte) 11),
    MOVE("MOVE", (byte) 12),
    LOCK("LOCK", (byte) 13),
    UNLOCK("UNLOCK", (byte) 14),
    ACL("ACL", (byte) 15),
    REPORT("REPORT", (byte) 16),
    VERSION_CONTROL("VERSION-CONTROL", (byte) 17),
    CHECKIN("CHECKIN", (byte) 18),
    CHECKOUT("CHECKOUT", (byte) 19),
    UNCHECKOUT("UNCHECKOUT", (byte) 20),
    SEARCH("SEARCH", (byte) 21),
    MKWORKSPACE("MKWORKSPACE", (byte) 22),
    UPDATE("UPDATE", (byte) 23),
    LABEL("LABEL", (byte) 24),
    MERGE("MERGE", (byte) 25),
    BASELINE_CONTROL("BASELINE_CONTROL", (byte) 26),
    MKACTIVITY("MKACTIVITY", (byte) 27),
    CONNECT("CONNECT", (byte) 0);

    private byte[] value;
    private byte ajpCode;

    MethodType(String str, byte ajpCode) {
        this.value = str.getBytes();
        this.ajpCode = ajpCode;
    }

    public static MethodType fromBuffer(ParsingBuffer buffer) {
        return fromBytes(buffer.data(), 0, buffer.length());
    }

    public static MethodType fromBytes(byte[] bytes, int offset, int length) {
        if (bytes != null && offset >= 0 && length > 0) {
            for (MethodType mt : values()) {
                if (BytesUtil.compare(bytes, offset, length, mt.value) == 0) {
                    return mt;
                }
            }
            return getUnknown(BytesUtil.newBytes(bytes, offset, length));
        }
        return getUnknown(null);
    }

    public static MethodType fromSting(String s) {
        if (s != null && s.length() > 0) {
            for (MethodType mt : values()) {
                if (new String(mt.getBytes()).equalsIgnoreCase(s)) {
                    return mt;
                }
            }
            return getUnknown(s.getBytes());
        }
        return getUnknown(null);
    }

    private static MethodType getUnknown(byte[] value) {
        MethodType mt = MethodType.Unknown;
        mt.value = value;
        return mt;
    }

    public byte[] getBytes() {
        return value;
    }

    public byte ajpCode() {
        return ajpCode;
    }
}
