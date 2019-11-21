package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.server.resource.filter.part.condition.CompareOperator;
import kr.dogfoot.webserver.util.http.HttpDateMaker;

public class HeaderValueExpires extends HeaderValue {
    private Long date;

    public HeaderValueExpires() {
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.Expires;
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.ioff = 0;
        ps.bufend = value.length;
        date = new Long(ByteParser.parseDate(value, ps));
        ParseState.release(ps);
    }

    @Override
    public byte[] combineValue() {
        return HttpDateMaker.makeBytes(date);
    }

    @Override
    public Long getDateValue() {
        return date;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

}
