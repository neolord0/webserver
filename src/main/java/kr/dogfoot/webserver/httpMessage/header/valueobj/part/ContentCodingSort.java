package kr.dogfoot.webserver.httpMessage.header.valueobj.part;

public enum ContentCodingSort {
    Unknown(""),
    GZip("gzip"),
    Compress("compress"),
    Deflate("deflate"),
    Identity("identity"),
    BR("br"),
    Asterisk("*");

    private String str;

    ContentCodingSort(String str) {
        this.str = str;
    }

    public static ContentCodingSort fromString(String str) {
        if (str != null) {
            for (ContentCodingSort ccs : values()) {
                if (ccs.str.equalsIgnoreCase(str)) {
                    return ccs;
                }
            }
        }
        return getUnknown(str);
    }

    private static ContentCodingSort getUnknown(String str) {
        ContentCodingSort ccs = ContentCodingSort.Unknown;
        ccs.str = str;
        return ccs;
    }

    @Override
    public String toString() {
        return str;
    }
}
