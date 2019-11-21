package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.server.resource.filter.part.condition.CompareOperator;

public abstract class HeaderValue {
    public abstract HeaderSort sort();

    public abstract void parseValue(byte[] value) throws ParserException;

    public abstract byte[] combineValue();

    public boolean hasQvalue() {
        return getQvalue(null) != null;
    }

    public Float getQvalue(String compare) {
        return null;
    }

    public Long getNumberValue() {
        return null;
    }

    public Long getDateValue() {
        return null;
    }
}
