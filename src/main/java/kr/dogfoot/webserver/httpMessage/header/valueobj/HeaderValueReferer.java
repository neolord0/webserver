package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.string.StringUtils;

import java.nio.charset.StandardCharsets;

public class HeaderValueReferer extends HeaderValue {
    private String uri;

    @Override
    public HeaderSort sort() {
        return HeaderSort.Referer;
    }

    @Override
    public void reset() {
        uri = null;
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        uri = new String(value, StandardCharsets.ISO_8859_1);
    }

    @Override
    public byte[] combineValue() {
        return uri.getBytes();
    }

    @Override
    public boolean isEqualValue(HeaderValue other) {
        if (other.sort() == HeaderSort.Referer) {
            HeaderValueReferer other2 = (HeaderValueReferer) other;

            return StringUtils.equalsWithNull(uri, other2.uri);
        }
        return false;
    }

    public String uri() {
        return uri;
    }

    public void uri(String uri) {
        this.uri = uri;
    }
}
