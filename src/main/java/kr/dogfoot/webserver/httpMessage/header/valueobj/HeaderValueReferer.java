package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.server.resource.filter.part.condition.CompareOperator;

import java.nio.charset.StandardCharsets;

public class HeaderValueReferer extends HeaderValue {
    private String uri;

    public HeaderValueReferer() {
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.Referer;
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
    public boolean compare(CompareOperator compareOp, String value) {
        return false;
    }

    public String uri() {
        return uri;
    }

    public void uri(String uri) {
        this.uri = uri;
    }
}
