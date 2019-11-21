package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;

public class HeaderValueAge extends HeaderValue {
    private long ageValue;

    @Override
    public HeaderSort sort() {
        return HeaderSort.Age;
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.ioff = 0;
        ps.bufend = value.length;

        ageValue = ByteParser.parseLong(value, ps);
        ParseState.release(ps);
    }

    @Override
    public byte[] combineValue() {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer.appendLong(ageValue);
        byte[] ret = buffer.getBytes();
        OutputBuffer.release(buffer);
        return ret;
    }

    @Override
    public Long getNumberValue() {
        return ageValue;
    }

    public long ageValue() {
        return ageValue;
    }

    public void ageValue(long ageValue) {
        this.ageValue = ageValue;
    }
}

