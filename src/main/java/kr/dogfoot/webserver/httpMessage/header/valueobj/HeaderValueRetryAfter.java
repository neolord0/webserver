package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpDateMaker;

public class HeaderValueRetryAfter extends HeaderValue {
    private Long date;
    private Long deltaSeconds;

    @Override
    public HeaderSort sort() {
        return HeaderSort.Retry_After;
    }

    @Override
    public void reset() {
        date = null;
        deltaSeconds = null;
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
        return buffer.getBytesAndRelease();
    }

    @Override
    public Long getNumberValue() {
        return deltaSeconds;
    }

    @Override
    public Long getDateValue() {
        return date;
    }

    @Override
    public boolean isEqualValue(HeaderValue other) {
        if (other.sort() == HeaderSort.Retry_After) {
            HeaderValueRetryAfter other2 = (HeaderValueRetryAfter) other;
            boolean equalDate;
            if (date == null) {
                equalDate = other2.date == null;
            } else {
                equalDate = date.equals(other2.date);
            }
            boolean equalDeltaSeconds;
            if (deltaSeconds == null) {
                equalDeltaSeconds = other2.deltaSeconds == null;
            } else {
                equalDeltaSeconds = deltaSeconds.equals(other2.deltaSeconds);
            }
            return equalDate && equalDeltaSeconds;
        }
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
