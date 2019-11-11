package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.server.resource.filter.part.condition.CompareOperator;
import kr.dogfoot.webserver.util.http.HttpDateMaker;

public class HeaderValueIfRange extends HeaderValue {
    private Long date;
    private byte[] entityTag;

    public HeaderValueIfRange() {
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.If_Range;
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.ioff = 0;
        ps.bufend = value.length;

        try {
            long dateValue = ByteParser.parseDate(value, ps);

            date = new Long(dateValue);
            entityTag = null;
        } catch (ParserException e) {
            e.printStackTrace();

            ps.ioff = 0;
            ps.bufend = value.length;
            ByteParser.unquote(value, ps);

            date = null;
            entityTag = ps.toNewBytes(value);
        }
        ParseState.release(ps);
    }

    @Override
    public byte[] combineValue() {
        if (date != null) {
            return HttpDateMaker.makeBytes(date);
        } else {
            return entityTag;
        }
    }

    @Override
    public boolean compare(CompareOperator compareOp, String value) {
        return false;
    }

    public Long date() {
        return date;
    }

    public void date(Long date) {
        this.date = date;
    }

    public byte[] entityTag() {
        return entityTag;
    }

    public void entityTag(byte[] entityTag) {
        this.entityTag = entityTag;
    }
}
