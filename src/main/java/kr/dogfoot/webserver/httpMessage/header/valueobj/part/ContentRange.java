package kr.dogfoot.webserver.httpMessage.header.valueobj.part;


import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.AppendableToByte;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

public class ContentRange implements AppendableToByte {
    private boolean isAsterisk;
    private long firstPos;
    private long lastPos;

    public ContentRange() {
    }

    public ContentRange(long firstPos, long lastPos) {
        isAsterisk = false;
        this.firstPos = firstPos;
        this.lastPos = lastPos;
    }

    public void reset() {
        isAsterisk = false;
        firstPos = 0;
        lastPos = 0;
    }

    public void parse(byte[] value, ParseState parentPS) throws ParserException {
        if (parentPS.end - parentPS.start == 1
                && value[parentPS.start] == HttpString.Asterisk) {
            isAsterisk = true;
        } else {
            isAsterisk = false;
            ParseState ps = ParseState.pooledObject();
            ps.prepare(parentPS);
            ps.separator = HttpString.Minus;
            ps.spaceIsSep = false;
            ps.permitNoSeparator = false;

            if (ByteParser.nextItem(value, ps) < 0) {
                throw new ParserException("Invalid byte range.");
            }

            ParseState ps2 = ParseState.pooledObject();
            ps2.prepare(ps);
            this.firstPos = ByteParser.parseLong(value, ps2);
            ps.rest();
            ps2.prepare(ps);
            this.lastPos = ByteParser.parseLong(value, ps2);

            ParseState.release(ps);
            ParseState.release(ps2);
        }
    }

    @Override
    public void append(OutputBuffer buffer) {
        if (isAsterisk) {
            buffer.append(HttpString.Asterisk);
        } else {
            buffer.appendLong(firstPos).append(HttpString.Minus).appendLong(lastPos);
        }
    }

    public boolean canMerge(ContentRange other) {
        if (this.firstPos <= other.firstPos && this.lastPos >= other.lastPos) {
            return true;
        } else if (this.firstPos >= other.firstPos && this.lastPos <= other.lastPos) {
            this.firstPos = other.firstPos;
            this.lastPos = other.lastPos;
            return true;
        } else if (this.firstPos >= other.firstPos && this.firstPos - 1 <= other.lastPos) {
            this.firstPos = other.firstPos;
            return true;
        } else if (this.lastPos <= other.lastPos && this.lastPos + 1 >= other.firstPos) {
            this.lastPos = other.lastPos;
            return true;
        }
        return false;
    }

    public long getRangeLength() {
        return lastPos - firstPos + 1;
    }

    public long getWriteSize() {
        if (isAsterisk) {
            return 1;
        } else {
            return OutputBuffer.getWriteSize_Long(firstPos) + 1 + OutputBuffer.getWriteSize_Long(lastPos);
        }
    }

    public boolean isMatch(ContentRange other) {
        if (isAsterisk == other.isAsterisk
                && firstPos == other.firstPos
                && lastPos == other.lastPos) {
            return true;
        }
        return false;
    }

    public boolean isAsterisk() {
        return isAsterisk;
    }

    public void isAsterisk(boolean asterisk) {
        isAsterisk = asterisk;
    }

    public long firstPos() {
        return firstPos;
    }

    public void firstPos(long firstPos) {
        this.firstPos = firstPos;
    }

    public long lastPos() {
        return lastPos;
    }

    public void lastPos(long lastPos) {
        this.lastPos = lastPos;
    }
}
