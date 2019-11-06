package kr.dogfoot.webserver.util.message.ajp;

public enum AjpAttribute {
    Context((byte) 0x01),
    Servlet_Path((byte) 0x02),
    Remote_User((byte) 0x03),
    Auth_Type((byte) 0x04),
    Query_String((byte) 0x05),
    Route((byte) 0x06),
    SSL_Cert((byte) 0x07),
    SSL_Cipher((byte) 0x08),
    SSL_Session((byte) 0x09),
    Req_Attribute((byte) 0x0A),
    SSL_Key_Size((byte) 0x0B),
    Secret((byte) 0x0C),
    Stored_Method((byte) 0x0D),
    Are_Done((byte) 0xFF);

    private byte code;

    AjpAttribute(byte code) {
        this.code = code;
    }

    public byte code() {
        return code;
    }
}
