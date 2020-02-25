package kr.dogfoot.webserver.httpMessage.header.valueobj.part;

import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.AppendableToByte;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

public class InstanceLength implements AppendableToByte {
    private boolean isAsterisk;
    private long length;

    public InstanceLength() {
    }

    public void reset() {
        isAsterisk = false;
        length = 0;
    }

    public void parse(byte[] value, ParseState parentPS) throws ParserException {
        if (parentPS.end - parentPS.start == 1
                && value[parentPS.start] == HttpString.Asterisk) {
            isAsterisk = true;
        } else {
            isAsterisk = false;

            length = ByteParser.parseLong(value, parentPS);
        }
    }

    @Override
    public void append(OutputBuffer buffer) {
        if (isAsterisk) {
            buffer.append(HttpString.Asterisk);
        } else {
            buffer.appendLong(length);
        }
    }

    public boolean isMatch(InstanceLength other) {
        if (isAsterisk == other.isAsterisk
                && length == other.length) {
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

    public long length() {
        return length;
    }

    public void length(long length) {
        this.length = length;
    }
}
