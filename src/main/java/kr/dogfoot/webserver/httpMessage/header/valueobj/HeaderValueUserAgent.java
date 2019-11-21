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

public class HeaderValueUserAgent extends HeaderValue {
    private static final String[] Zero_Array = new String[0];
    private ArrayList<String> infoList;

    public HeaderValueUserAgent() {
        infoList = new ArrayList<String>();
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.User_Agent;
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.ioff = 0;
        ps.bufend = value.length;
        ps.separator = HttpString.Space;
        ps.spaceIsSep = true;

        while (ByteParser.nextItem(value, ps) >= 0) {
            String info = ps.toString(value);

            if (info != null) {
                infoList.add(info);
            }
        }
        ParseState.release(ps);
    }

    @Override
    public byte[] combineValue() {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer.appendStringArray((byte) ' ', infoList.toArray());
        byte[] ret = buffer.getBytes();
        OutputBuffer.release(buffer);
        return ret;
    }

    public void addInfo(String info) {
        infoList.add(info);
    }

    public String[] infos() {
        return infoList.toArray(Zero_Array);
    }
}
