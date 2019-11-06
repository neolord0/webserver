package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.server.resource.filter.part.condition.CompareOperator;
import kr.dogfoot.webserver.util.bytes.BytesUtil;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

import java.util.ArrayList;

public class HeaderValueIfNoneMatch extends HeaderValue {
    private static final byte[][] Zero_Array = new byte[0][];
    private boolean isAsterisk;
    private ArrayList<byte[]> entityTagList;

    public HeaderValueIfNoneMatch() {
        entityTagList = new ArrayList<byte[]>();
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.If_None_Match;
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
            buffer.appendStringArray(HttpString.Comma, entityTagList.toArray());
        }
        byte[] ret = buffer.getBytes();
        OutputBuffer.release(buffer);
        return ret;
    }

    @Override
    public boolean compare(CompareOperator compareOp, String value) {
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

    public boolean isMatch(byte[] etag) {
        for (byte[] etag2 : entityTagList) {
            if (BytesUtil.compare(etag, 0, etag.length, etag2) == 0) {
                return true;
            }
        }
        return false;
    }
}
