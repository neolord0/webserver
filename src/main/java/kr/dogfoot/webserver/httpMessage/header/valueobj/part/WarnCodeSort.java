package kr.dogfoot.webserver.httpMessage.header.valueobj.part;

public enum WarnCodeSort {
    Code110("110", "Response is stale"),
    Code111("111", "Revalidation failed"),
    Code112("112", "Disconnected operation"),
    Code113("113", "Heuristic expiration"),
    Code199("199", "Miscellaneous warning"),
    Code214("214", "Transformation applied"),
    Code299("299", "Miscellaneous persistent warning"),
    Unknown(null, null);

    private String str;
    private String defaultText;

    WarnCodeSort(String str, String defaultText) {
        this.str = str;
        this.defaultText = defaultText;
    }

    public static WarnCodeSort fromString(String str) {
        if (str != null) {
            for (WarnCodeSort wcs : values()) {
                if (wcs.str.equals(str)) {
                    return wcs;
                }
            }
        }
        return unknown(str);
    }

    private static WarnCodeSort unknown(String str) {
        WarnCodeSort warnCodeSort = WarnCodeSort.Unknown;
        warnCodeSort.str = str;
        return warnCodeSort;
    }

    public boolean is1xx() {
        return str.startsWith("1");
    }


    public String toString() {
        return str;
    }

    public String getDefaultText() {
        return defaultText;
    }
}
