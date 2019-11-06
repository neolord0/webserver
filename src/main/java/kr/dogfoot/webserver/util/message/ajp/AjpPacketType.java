package kr.dogfoot.webserver.util.message.ajp;

public enum AjpPacketType {
    Unknown((byte) 0),
    ForwardRequest((byte) 2),
    ShutDown((byte) 7),
    Ping((byte) 8),
    CPing((byte) 10),
    SendBodyChunk((byte) 3),
    SendHeaders((byte) 4),
    EndResponse((byte) 5),
    GetBodyChunk((byte) 6),
    CPongReply((byte) 9);

    private byte code;

    AjpPacketType(byte code) {
        this.code = code;
    }

    public static AjpPacketType fromCode(byte code) {
        for (AjpPacketType apt : values()) {
            if (apt.code == code) {
                return apt;
            }
        }
        return Unknown;
    }

    public byte code() {
        return code;
    }

}
