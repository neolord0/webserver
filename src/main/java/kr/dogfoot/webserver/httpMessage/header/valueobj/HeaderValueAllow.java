package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.request.MethodType;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

import java.util.ArrayList;

public class HeaderValueAllow extends HeaderValue {
    private static final MethodType[] Zero_Array = new MethodType[0];
    private ArrayList<MethodType> methodTypeList;

    public HeaderValueAllow() {
        methodTypeList = new ArrayList<MethodType>();
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.Allow;
    }

    @Override
    public void reset() {
        methodTypeList.clear();
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.ioff = 0;
        ps.bufend = value.length;
        ps.separator = HttpString.Comma;
        ps.spaceIsSep = false;

        byte[] mt;
        while (ByteParser.nextItem(value, ps) >= 0) {
            mt = ps.toNewBytes(value);
            if (mt != null) {
                methodTypeList.add(MethodType.fromBytes(mt, 0, mt.length));
            }
        }
    }

    @Override
    public byte[] combineValue() {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        boolean first = true;
        for (MethodType mt : methodTypeList) {
            if (first) {
                first = false;
            } else {
                buffer.append(HttpString.Comma).append(HttpString.Space);
            }
            buffer.append(mt.getBytes());
        }
        return buffer.getBytesAndRelease();
    }

    @Override
    public boolean isEqualValue(HeaderValue other) {
        if (other.sort() == HeaderSort.Allow) {
            HeaderValueAllow other2 = (HeaderValueAllow) other;
            int includedCount = 0;
            for (MethodType mt : other2.methodTypeList) {
                if (isInclude(mt)) {
                    includedCount++;
                }
            }
            if (includedCount == other2.methodTypeList.size()) {
                return true;
            }
        }
        return false;
    }

    public boolean isInclude(MethodType other) {
        for (MethodType mt : methodTypeList) {
            if (mt == other) {
                return true;
            }
        }
        return false;
    }

    public void addMethodType(MethodType mt) {
        methodTypeList.add(mt);
    }

    public MethodType[] methodTypes() {
        return methodTypeList.toArray(Zero_Array);
    }
}
