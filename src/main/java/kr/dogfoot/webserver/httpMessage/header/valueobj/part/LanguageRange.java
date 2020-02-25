package kr.dogfoot.webserver.httpMessage.header.valueobj.part;

import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.AppendableToByte;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;
import kr.dogfoot.webserver.util.string.StringUtils;

public class LanguageRange implements AppendableToByte {
    private String range;
    private String subrange;
    private Float qvalue;

    public LanguageRange() {
    }

    public void parse(byte[] value, ParseState parentPS) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.prepare(parentPS);
        ps.separator = HttpString.Semicolon;
        ps.spaceIsSep = false;

        boolean isFirst = true;
        while (ByteParser.nextItem(value, ps) >= 0) {
            if (isFirst) {
                parseLanguageRange(value, ps);
                isFirst = false;
            } else {
                Parameter p = new Parameter();
                try {
                    p.parse(value, ps);

                    if (p.name().equalsIgnoreCase(HttpString.Q)) {
                        qvalue = new Float(p.value());
                    }
                } catch (ParserException e) {
                    e.printStackTrace();
                }
            }
        }
        ParseState.release(ps);
    }

    private void parseLanguageRange(byte[] value, ParseState parentPS) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.prepare(parentPS);

        ps.separator = HttpString.Minus;
        ps.spaceIsSep = false;
        ps.permitNoSeparator = false;

        if (ByteParser.nextItem(value, ps) >= 0) {
            range = ps.toString(value);
            ps.rest();
            subrange = ps.toString(value);
        } else {
            range = parentPS.toString(value);
            subrange = null;
        }
        ParseState.release(ps);
    }

    @Override
    public void append(OutputBuffer buffer) {
        buffer.append(range);
        if (subrange != null) {
            buffer.append(HttpString.Minus).append(subrange);
        }
        if (qvalue != null) {
            buffer.append(HttpString.Semicolon).appendQValue(qvalue);
        }
    }

    public boolean isMatch(String compare) {
        if ("*".equals(range) && "*".equals(subrange)) {
            return true;
        }

        String[] ranges = compare.split(HttpString.Minus_String);
        if (range != null && (subrange == null || "*".equals(subrange))) {
            return range.equalsIgnoreCase(ranges[0]);
        } else if (range != null && subrange != null) {
            return ranges.length == 2
                    && range.equalsIgnoreCase(ranges[0])
                    && subrange.equalsIgnoreCase(ranges[1]);
        }
        return false;
    }

    public boolean isMatch(LanguageRange other) {
        if ("*".equals(range) && "*".equals(subrange)) {
            return true;
        }

        if (subrange == null || "*".equals(subrange)) {
            return StringUtils.equalsWithNull(range, other.range);
        } else {
            return StringUtils.equalsWithNull(range, other.range)
                    && StringUtils.equalsWithNull(subrange, other.subrange);
        }
    }

    public String range() {
        return range;
    }

    public void range(String range) {
        this.range = range;
    }

    public String subrange() {
        return subrange;
    }

    public void subrange(String subrange) {
        this.subrange = subrange;
    }

    public Float qvalue() {
        return qvalue;
    }

    public void qvalue(Float qvalue) {
        this.qvalue = qvalue;
    }
}

