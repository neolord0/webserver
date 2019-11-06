package kr.dogfoot.webserver.httpMessage.header.valueobj.part;

public enum CacheDirectiveSort {
    Unknown(""),
    NoCache("no-cache"),
    NoState("no-state"),
    MaxAge("max-age"),
    MaxStale("max-stale"),
    MinFresh("min-fresh"),
    NoTransform("no-transfrom"),
    OnlyIfCached("only-if-cached"),
    Public("public"),
    Private("private"),
    MustRevalidate("must-revalidate"),
    ProxyRevalidate("proxy-revalidate"),
    SMaxAge("s-maxage");

    private String str;

    CacheDirectiveSort(String str) {
        this.str = str;
    }

    public static CacheDirectiveSort fromString(String str) {
        if (str != null) {
            for (CacheDirectiveSort cds : values()) {
                if (str.equalsIgnoreCase(cds.toString())) {
                    return cds;
                }
            }
        }
        return getUnknown(str);
    }

    private static CacheDirectiveSort getUnknown(String str) {
        CacheDirectiveSort cds = CacheDirectiveSort.Unknown;
        cds.str = str;
        return cds;
    }

    @Override
    public String toString() {
        return str;
    }
}
