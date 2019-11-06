package kr.dogfoot.webserver.httpMessage.header;

public enum HeaderSort {
    Unknown("", (short) 0, (short) 0),
    Accept("Accept", (short) 0xA001, (short) 0),
    Accept_Charset("Accept-Charset", (short) 0xA002, (short) 0),
    Accept_Encoding("Accept-Encoding", (short) 0xA003, (short) 0),
    Accept_Language("Accept-Language", (short) 0xA004, (short) 0),
    Accept_Ranges("Accept-Ranges", (short) 0, (short) 0),
    Age("Age", (short) 0, (short) 0),
    Allow("Allow", (short) 0, (short) 0),
    Authorization("Authorization", (short) 0xA005, (short) 0),
    Cache_Control("Cache-Control", (short) 0, (short) 0),
    Connection("Connection", (short) 0xA006, (short) 0),
    Content_Encoding("Content-Encoding", (short) 0, (short) 0),
    Content_Language("Content-Language", (short) 0, (short) 0xA002),
    Content_Length("Content-Length", (short) 0xA008, (short) 0xA003),
    Content_Location("Content-Location", (short) 0, (short) 0),
    Content_MD5("Content-Md5", (short) 0, (short) 0),
    Content_Range("Content-Range", (short) 0, (short) 0),
    Content_Type("Content-Type", (short) 0xA007, (short) 0xA001),
    Cookie("Cookie", (short) 0xA009, (short) 0),
    Cookie2("Cookie2", (short) 0xA00A, (short) 0),
    Date("Date", (short) 0, (short) 0xA004),
    ETag("ETag", (short) 0, (short) 0),
    Expect("Expect", (short) 0, (short) 0),
    Expires("Expires", (short) 0, (short) 0),
    From("From", (short) 0, (short) 0),
    Host("Host", (short) 0xA00B, (short) 0),
    If_Match("If-Match", (short) 0, (short) 0),
    If_Modified_Since("If-Modified-Since", (short) 0, (short) 0),
    If_None_Match("If-None-Match", (short) 0, (short) 0),
    If_Range("If-Range", (short) 0, (short) 0),
    If_Unmodified_Since("If-Unmodified-Since", (short) 0, (short) 0),
    Keep_Alive("Keep-Alive", (short) 0, (short) 0),
    Last_Modified("Last-Modified", (short) 0, (short) 0xA005),
    Location("Location", (short) 0, (short) 0xA006),
    Max_Forwards("Max-Forwards", (short) 0, (short) 0),
    Pragma("Pragma", (short) 0xA00C, (short) 0),
    Proxy_Authenticate("Proxy-Authenticate", (short) 0, (short) 0),
    Proxy_Authorization("Proxy-Authorization", (short) 0, (short) 0),
    Range("Range", (short) 0, (short) 0),
    Referer("Referer", (short) 0xA00D, (short) 0),
    Retry_After("Retry-After", (short) 0, (short) 0),
    Server("Server", (short) 0, (short) 0),
    Set_Cookie("Set-Cookie", (short) 0, (short) 0xA007),
    Set_Cookie2("Set-Cookie2", (short) 0, (short) 0xA008),
    Servlet_Engine("Servlet-Engine", (short) 0, (short) 0xA009),
    Status("Status", (short) 0, (short) 0xA00A),
    TE("Te", (short) 0, (short) 0),
    Trailer("Trailer", (short) 0, (short) 0),
    Transfer_Encoding("Transfer-Encoding", (short) 0, (short) 0),
    Upgrade("Upgrade", (short) 0, (short) 0),
    User_Agent("User-Agent", (short) 0xA00E, (short) 0),
    Vary("Vary", (short) 0, (short) 0),
    Via("Via", (short) 0, (short) 0),
    Warning("Warning", (short) 0, (short) 0),
    WWW_Authenticate("WWW-Authenticate", (short) 0, (short) 0xA00B);

    private String str;
    private short ajpSendCode;
    private short ajpReceiveCode;

    HeaderSort(String str, short ajpSendCode, short ajpReceiveCode) {
        this.str = str;
        this.ajpSendCode = ajpSendCode;
        this.ajpReceiveCode = ajpReceiveCode;
    }

    public static HeaderSort fromString(String str) {
        if (str != null) {
            for (HeaderSort hs : values()) {
                if (str.compareToIgnoreCase(hs.str) == 0) {
                    return hs;
                }
            }
        }
        return getUnknown(str);
    }

    private static HeaderSort getUnknown(String str) {
        HeaderSort hs = HeaderSort.Unknown;
        hs.str = str;
        return hs;
    }

    public static HeaderSort fromAjpSendCode(short ajpCode) {
        for (HeaderSort hs : values()) {
            if (hs.ajpSendCode == ajpCode) {
                return hs;
            }
        }
        return getUnknown(new Integer(ajpCode).toString());
    }

    public static HeaderSort fromAjpReceiveCode(short ajpCode) {
        for (HeaderSort hs : values()) {
            if (hs.ajpReceiveCode == ajpCode) {
                return hs;
            }
        }
        return getUnknown(new Integer(ajpCode).toString());
    }

    @Override
    public String toString() {
        return str;
    }

    public short getAjpSendCode() {
        return ajpSendCode;
    }
}
