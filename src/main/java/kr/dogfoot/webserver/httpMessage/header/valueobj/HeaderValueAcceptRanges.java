package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;
import kr.dogfoot.webserver.util.string.StringUtils;

import java.util.ArrayList;

public class HeaderValueAcceptRanges extends HeaderValue {
    private static final String[] Zero_Array = new String[0];
    private ArrayList<String> rangeUnitList;

    public HeaderValueAcceptRanges() {
        rangeUnitList = new ArrayList<String>();
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.Accept_Ranges;
    }

    @Override
    public void reset() {
        rangeUnitList.clear();
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.ioff = 0;
        ps.bufend = value.length;
        ps.separator = HttpString.Comma;
        ps.spaceIsSep = false;

        String rangeUnit;
        while (ByteParser.nextItem(value, ps) >= 0) {
            rangeUnit = ps.toString(value);

            if (rangeUnit != null && rangeUnit.equalsIgnoreCase(HttpString.none) == false) {
                rangeUnitList.add(rangeUnit);
            }
        }
        ParseState.release(ps);
    }

    @Override
    public byte[] combineValue() {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        if (rangeUnitList.size() > 0) {
            buffer.appendStringArray((byte) ',', rangeUnitList.toArray());
        } else {
            buffer.append("none");
        }
        return buffer.getBytesAndRelease();
    }

    @Override
    public boolean isEqualValue(HeaderValue other) {
        if (other.sort() == HeaderSort.Accept_Ranges) {
            HeaderValueAcceptRanges other2 = (HeaderValueAcceptRanges) other;
            int includedCount = 0;
            for (String ru : other2.rangeUnitList) {
                if (isInclude(ru)) {
                    includedCount++;
                }
            }
            if (includedCount == other2.rangeUnitList.size()) {
                return true;
            }
        }
        return false;
    }

    private boolean isInclude(String other) {
        for (String ru : rangeUnitList) {
            if (StringUtils.equalsWithNull(ru, other)) {
                return true;
            }
        }
        return false;
    }


    public void addRangeUnit(String rangeUnit) {
        rangeUnitList.add(rangeUnit);
    }

    public String[] rangeUnits() {
        return rangeUnitList.toArray(Zero_Array);
    }
}
