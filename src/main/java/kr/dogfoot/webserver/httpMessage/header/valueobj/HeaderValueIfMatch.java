package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.BytesUtil;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

import java.util.ArrayList;

public class HeaderValueIfMatch extends HeaderValue {
    private static final byte[][] Zero_Array = new byte[0][];
    private boolean isAsterisk;
    private ArrayList<byte[]> entityTagList;

    public HeaderValueIfMatch() {
        entityTagList = new ArrayList<byte[]>();
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.If_Match;
    }

    @Override
    public void reset() {
        isAsterisk = false;
        entityTagList.clear();
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        if (value.length == 1 && value[0] == HttpString.Asterisk) {
            isAsterisk = true;
        } else {
            isAsterisk = false;

            ParseState ps = ParseState.pooledObject();
            ps.ioff = 0;
            ps.bufend = value.length;
            ps.separator = HttpString.Comma;
            ps.spaceIsSep = false;

            while (ByteParser.nextItem(value, ps) >= 0) {
                ParseState ps2 = ParseState.pooledObject();
                ps2.prepare(ps);
                ByteParser.unquote(value, ps2);
                entityTagList.add(ps2.toNewBytes(value));
                ParseState.release(ps2);
            }
            ParseState.release(ps);
        }

    }

    @Override
    public byte[] combineValue() {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        if (isAsterisk) {
            buffer.append(HttpString.Asterisk);
        } else {
            buffer.appendByteArrayQuoted(HttpString.Comma, entityTagList.toArray(Zero_Array));
        }
        return buffer.getBytesAndRelease();
    }

    @Override
    public boolean isEqualValue(HeaderValue other) {
        if (other.sort() == HeaderSort.If_Match) {
            HeaderValueIfMatch other2 = (HeaderValueIfMatch) other;
            if (isAsterisk == other2.isAsterisk) {
                return true;
            }
            int includedCount = 0;
            for (byte[] entryTag : other2.entityTagList) {
                if (isInclude(entryTag)) {
                    includedCount++;
                }
            }
            if (includedCount == other2.entityTagList.size()) {
                return true;
            }
        }
        return false;
    }

    public boolean isInclude(byte[] other) {
        for (byte[] entryTag : entityTagList) {
            if (BytesUtil.compareWithNull(entryTag, other) == 0) {
                return true;
            }
        }
        return false;
    }

    public boolean isAsterisk() {
        return isAsterisk;
    }

    public void setAsterisk(boolean asterisk) {
        isAsterisk = asterisk;
    }

    public void addEntityTag(byte[] entityTag) {
        entityTagList.add(entityTag);
    }

    public byte[][] entityTags() {
        return entityTagList.toArray(Zero_Array);
    }

}


