package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.LanguageRange;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

import java.util.ArrayList;

public class HeaderValueAcceptLanguage extends HeaderValue {
    private static final LanguageRange[] Zero_Array = new LanguageRange[0];
    public ArrayList<LanguageRange> languageRangeList;

    public HeaderValueAcceptLanguage() {
        languageRangeList = new ArrayList<LanguageRange>();
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.Accept_Language;
    }

    @Override
    public void reset() {
        languageRangeList.clear();
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.ioff = 0;
        ps.bufend = value.length;
        ps.separator = HttpString.Comma;
        ps.spaceIsSep = false;

        while (ByteParser.nextItem(value, ps) >= 0) {
            LanguageRange lr = new LanguageRange();
            try {
                lr.parse(value, ps);

                languageRangeList.add(lr);
            } catch (ParserException e) {
                e.printStackTrace();
            }
        }
        ParseState.release(ps);
    }

    @Override
    public byte[] combineValue() {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer.appendArray((byte) ',', languageRangeList.toArray());
        return buffer.getBytesAndRelease();
    }

    @Override
    public Float getQvalue(String compare) {
        if (compare == null) {
            return (float) -1;
        }
        float qvalue = -1;

        for (LanguageRange lr : languageRangeList) {
            if (lr.isMatch(compare)) {
                float lr_qvalue = (lr.qvalue() == null) ? 1 : lr.qvalue();
                qvalue = Math.max(lr_qvalue, qvalue);
            }
        }
        return qvalue;
    }

    @Override
    public boolean isEqualValue(HeaderValue other) {
        if (other.sort() == HeaderSort.Accept_Language) {
            HeaderValueAcceptLanguage other2 = (HeaderValueAcceptLanguage) other;
            int includedCount = 0;
            for (LanguageRange lr : other2.languageRangeList) {
                if (isInclude(lr)) {
                    includedCount++;
                }
            }
            if (includedCount == other2.languageRangeList.size()) {
                return true;
            }
        }
        return false;
    }

    private boolean isInclude(LanguageRange other) {
        for (LanguageRange lr : languageRangeList) {
            if (lr.isMatch(other)) {
                return true;
            }
        }
        return false;
    }


    public LanguageRange addNewLanguageRange() {
        LanguageRange lr = new LanguageRange();
        languageRangeList.add(lr);
        return lr;
    }


    public LanguageRange[] languageRanges() {
        return languageRangeList.toArray(Zero_Array);
    }
}
