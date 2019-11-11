package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.CacheDirective;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

import java.util.ArrayList;

public class HeaderValueCacheControl extends HeaderValue {
    private static final CacheDirective[] Zero_Array = new CacheDirective[0];
    private ArrayList<CacheDirective> cacheDirectiveList;

    public HeaderValueCacheControl() {
        cacheDirectiveList = new ArrayList<CacheDirective>();
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.Cache_Control;
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.ioff = 0;
        ps.bufend = value.length;
        ps.separator = HttpString.Comma;
        ps.spaceIsSep = false;

        while (ByteParser.nextItem(value, ps) >= 0) {
            CacheDirective cd = new CacheDirective();
            try {
                cd.parse(value, ps);

                cacheDirectiveList.add(cd);
            } catch (ParserException e) {
                e.printStackTrace();
            }
        }
        ParseState.release(ps);
    }

    @Override
    public byte[] combineValue() {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer.appendArray((byte) ',', cacheDirectiveList.toArray());
        byte[] ret = buffer.getBytes();
        OutputBuffer.release(buffer);
        return ret;
    }

    public CacheDirective addNewCacheDirective() {
        CacheDirective cd = new CacheDirective();
        cacheDirectiveList.add(cd);
        return cd;
    }

    public CacheDirective[] cacheDirectives() {
        return cacheDirectiveList.toArray(Zero_Array);
    }
}