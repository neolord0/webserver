package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.request.URI;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.string.StringUtils;

import java.nio.charset.StandardCharsets;

public class HeaderValueContentLocation extends HeaderValue {
    private URI uri;

    public HeaderValueContentLocation() {
        uri = new URI();
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.Content_Location;
    }

    @Override
    public void reset() {
        uri.reset();
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        uri.parse(new String(value, StandardCharsets.ISO_8859_1));
    }

    @Override
    public byte[] combineValue() {
        String s = uri.toString();
        if (s != null) {
            return s.getBytes();
        } else {
            return null;
        }
    }

    @Override
    public boolean isEqualValue(HeaderValue other) {
        if (other.sort() == HeaderSort.Content_Location) {
            HeaderValueContentLocation other2 = (HeaderValueContentLocation) other;

            return StringUtils.equalsWithNull(uri(), other2.uri());
        }
        return false;
    }

    public String uri() {
        return uri.toString();
    }

    public void uri(String uri) {
        this.uri.parse(uri);
    }

    public URI uriObject() {
        return uri;
    }
}
