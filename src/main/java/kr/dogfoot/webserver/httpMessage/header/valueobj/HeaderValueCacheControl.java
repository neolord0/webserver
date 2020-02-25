package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.CacheDirective;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.CacheDirectiveSort;
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
    public void reset() {
        cacheDirectiveList.clear();
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
        return buffer.getBytesAndRelease();
    }

    @Override
    public boolean isEqualValue(HeaderValue other) {
        if (other.sort() == HeaderSort.Cache_Control) {
            HeaderValueCacheControl other2 = (HeaderValueCacheControl) other;
            int includedCount = 0;
            for (CacheDirective cd : other2.cacheDirectiveList) {
                if (isInclude(cd)) {
                    includedCount++;
                }
            }
            if (includedCount == other2.cacheDirectiveList.size()) {
                return true;
            }
        }
        return false;
    }

    private boolean isInclude(CacheDirective other) {
        for (CacheDirective cd : cacheDirectiveList) {
            if (cd.isMatch(other)) {
                return true;
            }
        }
        return false;
    }

    public CacheDirective addNewCacheDirective() {
        CacheDirective cd = new CacheDirective();
        cacheDirectiveList.add(cd);
        return cd;
    }

    public CacheDirective[] cacheDirectives() {
        return cacheDirectiveList.toArray(Zero_Array);
    }

    public boolean hasCacheDirective(CacheDirectiveSort directiveSort) {
        for (CacheDirective cd : cacheDirectiveList) {
            if (cd.sort() == directiveSort) {
                return true;
            }
        }
        return false;
    }

    public CacheDirective getCacheDirective(CacheDirectiveSort directiveSort) {
        for (CacheDirective cd : cacheDirectiveList) {
            if (cd.sort() == directiveSort) {
                return cd;
            }
        }
        return null;
    }
}