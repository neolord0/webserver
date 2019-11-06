package kr.dogfoot.webserver.server.resource.filter.part;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;

public class HeaderSetting {
    private HeaderSort soft;
    private String value;

    public HeaderSetting() {
    }

    public HeaderSort sort() {
        return soft;
    }

    public void sort(HeaderSort soft) {
        this.soft = soft;
    }

    public String value() {
        return value;
    }

    public void value(String value) {
        this.value = value;
    }
}
