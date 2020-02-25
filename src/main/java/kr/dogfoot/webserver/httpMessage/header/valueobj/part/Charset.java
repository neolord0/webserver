package kr.dogfoot.webserver.httpMessage.header.valueobj.part;

import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.AppendableToByte;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;
import kr.dogfoot.webserver.util.string.StringUtils;

public class Charset implements AppendableToByte {
    private String charset;
    private Float qvalue;

    public Charset() {
    }

    public void parse(byte[] value, ParseState parentPS) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.prepare(parentPS);
        ps.separator = HttpString.Semicolon;
        ps.spaceIsSep = false;

        boolean isFirst = true;
        while (ByteParser.nextItem(value, ps) >= 0) {
            if (isFirst) {
                charset = ps.toString(value);
                isFirst = false;
            } else {
                Parameter p = new Parameter();
                try {
                    p.parse(value, ps);

                    if (p.name().equalsIgnoreCase("q")) {
                        qvalue = new Float(p.value());
                    }
                } catch (ParserException e) {
                    e.printStackTrace();
                }
            }
        }
        ParseState.release(ps);
    }

    @Override
    public void append(OutputBuffer buffer) {
        buffer.append(charset);
        if (qvalue != null) {
            buffer.append(HttpString.Semicolon).appendQValue(qvalue);
        }
    }

    public boolean isMatch(Charset other) {
        return StringUtils.equalsWithNull(charset, other.charset);
    }

    public String charset() {
        return charset;
    }

    public void charset(String charset) {
        this.charset = charset;
    }

    public Float qvalue() {
        return qvalue;
    }

    public void qvalue(Float qvalue) {
        this.qvalue = qvalue;
    }

}
