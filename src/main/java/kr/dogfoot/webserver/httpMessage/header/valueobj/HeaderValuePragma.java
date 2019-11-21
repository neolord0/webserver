package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.server.resource.filter.part.condition.CompareOperator;

public class HeaderValuePragma extends HeaderValue {
    private byte[] value;

    @Override
    public HeaderSort sort() {
        return HeaderSort.Pragma;
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        this.value = value;
    }

    @Override
    public byte[] combineValue() {
        return value;
    }

    public byte[] value() {
        return value;
    }

    public void value(byte[] value) {
        this.value = value;
    }
}
