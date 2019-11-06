package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ParserException;

import java.nio.charset.StandardCharsets;

public class HeaderValueContentLocation extends HeaderValue {
    private String uri;

    @Override
    public HeaderSort sort() {
        return HeaderSort.Content_Location;
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        uri = new String(value, StandardCharsets.ISO_8859_1);
    }

    @Override
    public byte[] combineValue() {
        if (uri != null) {
            return uri.getBytes();
        } else {
            return null;
        }
    }

    public String uri() {
        return uri;
    }

    public void uri(String uri) {
        this.uri = uri;
    }
}
