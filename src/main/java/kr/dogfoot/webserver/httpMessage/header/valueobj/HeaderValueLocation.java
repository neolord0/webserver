package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.server.resource.filter.part.condition.CompareOperator;

import java.nio.charset.StandardCharsets;

public class HeaderValueLocation extends HeaderValue {
    private String absoluteURI;

    public HeaderValueLocation() {
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.Location;
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        absoluteURI = new String(value, StandardCharsets.ISO_8859_1);
    }

    @Override
    public byte[] combineValue() {
        return absoluteURI.getBytes();
    }

    @Override
    public boolean compare(CompareOperator compareOp, String value) {
        return false;
    }

    public String absoluteURI() {
        return absoluteURI;
    }

    public void absoluteURI(String absoluteURI) {
        this.absoluteURI = absoluteURI;
    }
}
