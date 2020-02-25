package kr.dogfoot.webserver.httpMessage.header.valueobj.part;

public enum TransferCodingSort {
    Unknown(""),
    Chunked("chunked"),
    Compress("compress"),
    Deflate("deflate"),
    GZip("gzip"),
    Identity("identity"),
    Trailers("trailers");

    private String str;

    TransferCodingSort(String str) {
        this.str = str;
    }

    public static TransferCodingSort fromString(String str) {
        if (str != null) {
            for (TransferCodingSort tes : values()) {
                if (tes.str.equalsIgnoreCase(str)) {
                    return tes;
                }
            }
        }
        return unknown(str);
    }

    private static TransferCodingSort unknown(String str) {
        TransferCodingSort ccs = TransferCodingSort.Unknown;
        ccs.str = str;
        return ccs;
    }

    public String toString() {
        return str;
    }
}
