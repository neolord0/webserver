package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.server.resource.filter.part.condition.CompareOperator;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

import java.util.ArrayList;

public class HeaderValueTrailer extends HeaderValue {
    private static final HeaderSort[] Zero_Array = new HeaderSort[0];
    private ArrayList<HeaderSort> fieldNameList;

    public HeaderValueTrailer() {
        fieldNameList = new ArrayList<HeaderSort>();
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.Trailer;
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
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

    @Override
    public byte[] combineValue() {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer.appendStringArray(HttpString.Comma, fieldNameList.toArray());
        byte[] ret = buffer.getBytes();
        OutputBuffer.release(buffer);
        return ret;
    }

    @Override
    public boolean compare(CompareOperator compareOp, String value) {
        return false;
    }

    public void addFieldName(HeaderSort fieldName) {
        fieldNameList.add(fieldName);
    }

    public HeaderSort[] fieldNames() {
        return fieldNameList.toArray(Zero_Array);
    }
}
