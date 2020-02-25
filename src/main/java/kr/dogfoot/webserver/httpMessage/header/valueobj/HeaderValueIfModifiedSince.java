package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.http.HttpDateMaker;

public class HeaderValueIfModifiedSince extends HeaderValue {
    private Long date;

    @Override
    public HeaderSort sort() {
        return HeaderSort.If_Modified_Since;
    }

    @Override
    public void reset() {
        date = null;
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

    @Override
    public boolean isEqualValue(HeaderValue other) {
        if (other.sort() == HeaderSort.If_Modified_Since) {
            HeaderValueIfModifiedSince other2 = (HeaderValueIfModifiedSince) other;
            if (date == null) {
                return other2.date == null;
            } else {
                return date.equals(other2.date);
            }
        }
        return false;
    }

    public Long date() {
        return date;
    }

    public void date(Long date) {
        this.date = date;
    }


}
