package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.server.resource.filter.part.condition.CompareOperator;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

import java.util.ArrayList;

public class HeaderValueVary extends HeaderValue {
    private static final HeaderSort[] Zero_Array = new HeaderSort[0];
    private boolean isAsterisk;
    private ArrayList<HeaderSort> fieldNameList;

    public HeaderValueVary() {
        fieldNameList = new ArrayList<HeaderSort>();
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.Vary;
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

            String fieldName;
            while (ByteParser.nextItem(value, ps) >= 0) {
                fieldName = ps.toString(value);
                if (fieldName != null) {
                    fieldNameList.add(HeaderSort.fromString(fieldName));
                }
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
            buffer.appendStringArray(HttpString.Comma, fieldNameList.toArray());
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

    public void addFieldName(HeaderSort header) {
        fieldNameList.add(header);
    }

    public HeaderSort[] fieldNames() {
        return fieldNameList.toArray(Zero_Array);
    }
}
