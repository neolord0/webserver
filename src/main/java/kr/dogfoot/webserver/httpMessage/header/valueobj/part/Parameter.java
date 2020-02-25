package kr.dogfoot.webserver.httpMessage.header.valueobj.part;

import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.AppendableToByte;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

public class Parameter implements AppendableToByte {
    private String name;
    private String value;

    public Parameter() {
    }

    public Parameter(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public void parse(byte[] value, ParseState parentPS) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.prepare(parentPS);
        ps.separator = HttpString.Equal;
        ps.spaceIsSep = false;
        ps.permitNoSeparator = false;

        if (ByteParser.nextItem(value, ps) < 0) {
            throw new ParserException("Invalidate parameter.");
        }

        this.name = ps.toString(value);
        ps.rest();
        ByteParser.unquote(value, ps);
        this.value = ps.toString(value);

        ParseState.release(ps);
    }

    @Override
    public void append(OutputBuffer buffer) {
        buffer.append(name, HttpString.Equal, value);
    }

    public String name() {
        return name;
    }

    public void name(String name) {
        this.name = name;
    }

    public String value() {
        return value;
    }

    public void value(String value) {
        this.value = value;
    }

    public long getWriteSize() {
        return name.length() + 1 + value.length();
    }
}
