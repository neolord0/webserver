package kr.dogfoot.webserver.httpMessage.header.valueobj.part;

import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.AppendableToByte;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

public class Coding implements AppendableToByte {
    private ContentCodingSort contentCoding;
    private Float qvalue;

    public Coding() {
    }

    public void parse(byte[] value, ParseState parentPS) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.prepare(parentPS);
        ps.separator = HttpString.Semicolon;
        ps.spaceIsSep = false;

        boolean isFirst = true;
        while (ByteParser.nextItem(value, ps) >= 0) {
            if (isFirst) {
                contentCoding = ContentCodingSort.fromString(ps.toString(value));
                isFirst = false;
            } else {
                Parameter p = new Parameter();
                try {
                    p.parse(value, ps);

                    if (p.getName().equalsIgnoreCase(HttpString.Q)) {
                        qvalue = new Float(p.getValue());
                    }
                } catch (ParserException e) {
                    e.printStackTrace();

                    p = null;
                }
            }
        }
        ParseState.release(ps);
    }

    @Override
    public void append(OutputBuffer buffer) {
        buffer.append(contentCoding.toString());
        if (qvalue != null) {
            buffer.append(HttpString.Semicolon)
                    .appendQValue(qvalue);
        }
    }

    public ContentCodingSort getContentCoding() {
        return contentCoding;
    }

    public void getContentCoding(ContentCodingSort contentCoding) {
        this.contentCoding = contentCoding;
    }

    public Float getQvalue() {
        return qvalue;
    }

    public void setQvalue(Float qvalue) {
        this.qvalue = qvalue;
    }

    public boolean isMatch(String compare) {
        if (contentCoding == ContentCodingSort.Asterisk) {
            return true;
        }

        return contentCoding != null && contentCoding.toString().equals(compare);
    }

    public boolean isAsterisk() {
        return contentCoding == ContentCodingSort.Asterisk;
    }
}
