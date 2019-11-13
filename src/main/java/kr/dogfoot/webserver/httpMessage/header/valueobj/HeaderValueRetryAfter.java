package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.server.resource.filter.part.condition.CompareOperator;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpDateMaker;

public class HeaderValueRetryAfter extends HeaderValue {
    private Long date;
    private Long deltaSeconds;

    public HeaderValueRetryAfter() {
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.Retry_After;
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.ioff = 0;
        ps.bufend = value.length;

        try {
            long date = ByteParser.parseDate(value, ps);

            this.date = new Long(date);
            this.deltaSeconds = null;
        } catch (ParserException e) {
            e.printStackTrace();

            ps.ioff = 0;
            ps.bufend = value.length;

            this.date = null;
            this.deltaSeconds = new Long(ByteParser.parseLong(value, ps));
        }
        ParseState.release(ps);
    }

    @Override
    public byte[] combineValue() {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        if (date != null) {
            buffer.append(HttpDateMaker.makeBytes(date));
        } else {
            buffer.appendLong(deltaSeconds);
        }
        byte[] ret = buffer.getBytes();
        OutputBuffer.release(buffer);
        return ret;
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

    public Long deltaSeconds() {
        return deltaSeconds;
    }

    public void deltaSeconds(Long deltaSeconds) {
        this.deltaSeconds = deltaSeconds;
    }
}
