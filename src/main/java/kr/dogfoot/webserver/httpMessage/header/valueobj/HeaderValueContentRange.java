package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.ContentRange;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.InstanceLength;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;
import kr.dogfoot.webserver.util.string.StringUtils;

public class HeaderValueContentRange extends HeaderValue {
    private String unit;
    private ContentRange contentRange;
    private InstanceLength instanceLength;

    public HeaderValueContentRange() {
        contentRange = new ContentRange();
        instanceLength = new InstanceLength();
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.Content_Range;
    }

    @Override
    public void reset() {
        unit = null;
        contentRange.reset();
        instanceLength.reset();
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.ioff = 0;
        ps.bufend = value.length;
        ps.separator = HttpString.Space;
        ps.permitNoSeparator = false;
        if (ByteParser.nextItem(value, ps) < 0) {
            throw new ParserException("Invalid byte range (no byte unit).");
        }
        this.unit = ps.toString(value);

        ps.separator = HttpString.Slash;
        ps.prepare();

        if (ByteParser.nextItem(value, ps) < 0) {
            new ParserException("Invalid byte range (no byte unit).");
        }
        contentRange.parse(value, ps);
        ps.rest();
        instanceLength.parse(value, ps);
    }

    @Override
    public byte[] combineValue() {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer.append(unit).append(' ');
        contentRange.append(buffer);
        buffer.append('/');
        instanceLength.append(buffer);
        return buffer.getBytesAndRelease();
    }

    @Override
    public boolean isEqualValue(HeaderValue other) {
        if (other.sort() == HeaderSort.Content_Range) {
            HeaderValueContentRange other2 = (HeaderValueContentRange) other;

            if (StringUtils.equalsIgnoreCaseWithNull(unit, other2.unit)
                    && contentRange.isMatch(other2.contentRange)
                    && instanceLength.isMatch(other2.instanceLength)) {
                return true;
            }
        }
        return false;
    }

    public String unit() {
        return unit;
    }

    public void unit(String unit) {
        this.unit = unit;
    }

    public ContentRange contentRange() {
        return contentRange;
    }

    public InstanceLength instanceLength() {
        return instanceLength;
    }
}
