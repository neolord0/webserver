package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.WarningValue;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

import java.util.ArrayList;

public class HeaderValueWarning extends HeaderValue {
    private static final WarningValue[] Zero_Array = new WarningValue[0];
    private ArrayList<WarningValue> warningValueList;

    public HeaderValueWarning() {
        warningValueList = new ArrayList<WarningValue>();
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.Warning;
    }

    @Override
    public void reset() {
        warningValueList.clear();
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.ioff = 0;
        ps.bufend = value.length;
        ps.separator = HttpString.Comma;
        ps.spaceIsSep = false;

        while (ByteParser.nextItem(value, ps) >= 0) {
            WarningValue wv = new WarningValue();
            try {
                wv.parse(value, ps);

                warningValueList.add(wv);
            } catch (ParserException e) {
                e.printStackTrace();
            }
        }
        ParseState.release(ps);
    }

    @Override
    public byte[] combineValue() {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer.appendArray(HttpString.Comma, warningValueList.toArray());
        return buffer.getBytesAndRelease();
    }

    @Override
    public boolean isEqualValue(HeaderValue other) {
        if (other.sort() == HeaderSort.Warning) {
            HeaderValueWarning other2 = (HeaderValueWarning) other;
            int includedCount = 0;
            for (WarningValue wv : other2.warningValueList) {
                if (isInclude(wv)) {
                    includedCount++;
                }
            }
            if (includedCount == other2.warningValueList.size()) {
                return true;
            }
        }
        return false;
    }

    private boolean isInclude(WarningValue other) {
        for (WarningValue wv : warningValueList) {
            if (wv.isMatch(other)) {
                return true;
            }
        }
        return false;
    }

    public WarningValue addNewWarningValue() {
        WarningValue wv = new WarningValue();
        warningValueList.add(wv);
        return wv;
    }

    public WarningValue[] warningValues() {
        return warningValueList.toArray(Zero_Array);
    }

    public ArrayList<WarningValue> warningValueList() {
        return warningValueList;
    }
}
