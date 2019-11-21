package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.server.resource.filter.part.condition.CompareOperator;
import kr.dogfoot.webserver.util.bytes.ToBytes;

public class HeaderValueContentLength extends HeaderValue {
    private int value;

    public HeaderValueContentLength() {
        value = -1;
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.Content_Length;
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.ioff = 0;
        ps.bufend = value.length;

        try {
            this.value = ByteParser.parseInt(value, ps);
        } catch (ParserException e) {
            e.printStackTrace();
            this.value = -1;
        }

        ParseState.release(ps);
    }

    @Override
    public byte[] combineValue() {
        return ToBytes.fromInt(value);
    }

    @Override
    public Long getNumberValue() {
        return Long.valueOf(value);
    }

    public int value() {
        return value;
    }

    public void value(int value) {
        this.value = value;
    }
}
