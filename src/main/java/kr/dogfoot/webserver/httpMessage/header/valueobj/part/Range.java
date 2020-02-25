package kr.dogfoot.webserver.httpMessage.header.valueobj.part;


import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.AppendableToByte;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

public class Range implements AppendableToByte {
    private Long firstPos;
    private Long lastPos;

    public Range() {
    }

    public Range(Long firstPos, Long lastPos) {
        this.firstPos = firstPos;
        this.lastPos = lastPos;
    }

    public void parse(byte[] value, ParseState parentPS) throws ParserException {
        if (value[parentPS.start] == HttpString.Minus) {
            parseSuffixRange(value, parentPS);
        } else {
            parseByteRange(value, parentPS);
        }
    }

    private void parseSuffixRange(byte[] value, ParseState parentPS) throws ParserException {
        firstPos = null;

        ParseState ps = ParseState.pooledObject();
        ps.prepare(parentPS);
        ps.ioff++;

        lastPos = ByteParser.parseLong(value, ps);

        ParseState.release(ps);
    }

    private void parseByteRange(byte[] value, ParseState parentPS) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.prepare(parentPS);
        ps.separator = HttpString.Minus;
        ps.spaceIsSep = false;

        ParseState ps2 = ParseState.pooledObject();
        if (ByteParser.nextItem(value, ps) > 0) {
            ps2.prepare(ps);
            firstPos = ByteParser.parseLong(value, ps2);
        }

        if (ByteParser.nextItem(value, ps) > 0) {
            ps2.prepare(ps);
            lastPos = ByteParser.parseLong(value, ps2);
        } else {
            lastPos = null;
        }

        ParseState.release(ps);
        ParseState.release(ps2);
    }

    @Override
    public void append(OutputBuffer buffer) {
        if (firstPos != null || lastPos != null) {
            if (firstPos != null) {
                buffer.appendLong(firstPos);
            }
            buffer.append(HttpString.Minus);
            if (lastPos != null) {
                buffer.appendLong(lastPos);
            }
        }
    }

    public boolean isMatch(Range other) {
        boolean equalFirstPos;
        if (firstPos == null) {
            equalFirstPos = other.firstPos == null;
        } else {
            equalFirstPos = firstPos.equals(other.firstPos);
        }

        boolean equalLastPos;
        if (lastPos == null) {
            equalLastPos = other.lastPos == null;
        } else {
            equalLastPos = lastPos.equals(other.lastPos);
        }

        return equalFirstPos && equalLastPos;
    }

    public boolean isSuffix() {
        return firstPos == null;
    }

    public Long firstPos() {
        return firstPos;
    }

    public void firstPos(Long firstPos) {
        this.firstPos = firstPos;
    }

    public Long lastPos() {
        return lastPos;
    }

    public void lastPos(Long lastPos) {
        this.lastPos = lastPos;
    }
}
