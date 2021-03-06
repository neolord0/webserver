package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.Range;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;
import kr.dogfoot.webserver.util.string.StringUtils;

import java.util.ArrayList;

public class HeaderValueRange extends HeaderValue {
    private String unit;
    private ArrayList<Range> rangeList;

    public HeaderValueRange() {
        rangeList = new ArrayList<Range>();
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.Range;
    }

    @Override
    public void reset() {
        unit = null;
        rangeList.clear();
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.ioff = 0;
        ps.bufend = value.length;
        ps.separator = HttpString.Equal;
        ps.spaceIsSep = false;
        ps.permitNoSeparator = false;

        if (ByteParser.nextItem(value, ps) < 0) {
            throw new ParserException("no bytes-unit");
        }
        unit = ps.toString(value);
        ps.rest();
        parseRanges(value, ps);

        ParseState.release(ps);
    }

    @Override
    public byte[] combineValue() {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer
                .append(unit)
                .append(HttpString.Equal)
                .appendArray(HttpString.Comma, rangeList.toArray());
        return buffer.getBytesAndRelease();
    }

    @Override
    public boolean isEqualValue(HeaderValue other) {
        if (other.sort() == HeaderSort.Range) {
            HeaderValueRange other2 = (HeaderValueRange) other;
            if (StringUtils.equalsIgnoreCaseWithNull(unit, other2.unit)) {
                int includeCount = 0;
                for (Range range : other2.rangeList) {
                    if (isInclude(range)) {
                        includeCount++;
                    }
                }
                if (includeCount == other2.rangeList.size()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isInclude(Range other) {
        for (Range range : rangeList) {
            if (range.isMatch(other)) {
                return true;
            }
        }
        return false;
    }

    private void parseRanges(byte[] value, ParseState parentPS) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.prepare(parentPS);
        ps.separator = HttpString.Comma;
        ps.spaceIsSep = false;

        while (ByteParser.nextItem(value, ps) >= 0) {
            Range range = new Range();
            try {
                range.parse(value, ps);

                rangeList.add(range);
            } catch (ParserException e) {
                e.printStackTrace();
            }
        }
        ParseState.release(ps);
    }

    public String unit() {
        return unit;
    }

    public void unit(String unit) {
        this.unit = unit;
    }

    public ArrayList<Range> rangeList() {
        return rangeList;
    }

    public int rangeCount() {
        return rangeList.size();
    }
}
