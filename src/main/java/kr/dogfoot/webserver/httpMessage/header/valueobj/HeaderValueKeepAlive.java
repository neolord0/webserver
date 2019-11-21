package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.server.resource.filter.part.condition.CompareOperator;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

public class HeaderValueKeepAlive extends HeaderValue {
    private int timeout;
    private int max;

    public HeaderValueKeepAlive() {
        timeout = -1;
        max = -1;
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.Keep_Alive;
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.ioff = 0;
        ps.bufend = value.length;
        ps.separator = HttpString.Comma;
        ps.spaceIsSep = false;

        while (ByteParser.nextItem(value, ps) >= 0) {
            parseNameValue(value, ps);
        }

    }

    private void parseNameValue(byte[] value, ParseState parentPS) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.prepare(parentPS);
        ps.separator = HttpString.Equal;
        ps.spaceIsSep = false;
        ps.permitNoSeparator = false;

        if (ByteParser.nextItem(value, ps) < 0) {
            throw new ParserException("Invalidate parameter.");
        }

        String name = ps.toString(value);
        ps.rest();
        String value2 = ps.toString(value);
        if (name != null) {
            if (HttpString.Timeout_String.equalsIgnoreCase(name)) {
                timeout = Integer.parseInt(value2);
            } else if (HttpString.Max_String.equalsIgnoreCase(name)) {
                max = Integer.parseInt(value2);
            }
        }
    }

    @Override
    public byte[] combineValue() {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer.append(HttpString.Timeout, HttpString.Equal, timeout)
                .append(HttpString.Separator_Comma)
                .append(HttpString.Max, HttpString.Equal, max);
        byte[] ret = buffer.getBytes();
        OutputBuffer.release(buffer);
        return ret;

    }

    public int timeout() {
        return timeout;
    }

    public void timeout(int timeout) {
        this.timeout = timeout;
    }

    public int max() {
        return max;
    }

    public void max(int max) {
        this.max = max;
    }
}
