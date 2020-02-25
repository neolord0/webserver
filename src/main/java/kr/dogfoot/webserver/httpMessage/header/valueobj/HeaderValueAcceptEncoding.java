package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.Coding;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.ContentCodingSort;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

import java.util.ArrayList;

public class HeaderValueAcceptEncoding extends HeaderValue {
    private static final Coding[] Zero_Array = new Coding[0];
    private ArrayList<Coding> codingList;

    public HeaderValueAcceptEncoding() {
        codingList = new ArrayList<Coding>();
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.Accept_Encoding;
    }

    @Override
    public void reset() {
        codingList.clear();
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.ioff = 0;
        ps.bufend = value.length;
        ps.separator = HttpString.Comma;
        ps.spaceIsSep = false;

        while (ByteParser.nextItem(value, ps) >= 0) {
            Coding c = new Coding();
            try {
                c.parse(value, ps);

                codingList.add(c);
            } catch (ParserException e) {
                e.printStackTrace();
            }
        }
        ParseState.release(ps);
    }

    @Override
    public byte[] combineValue() {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer.appendArray(HttpString.Comma, codingList.toArray());
        return buffer.getBytesAndRelease();
    }

    @Override
    public Float getQvalue(String compare) {
        if (compare == null) {
            return (float) -1;
        }
        return getQvalue(ContentCodingSort.fromString(compare));
    }

    @Override
    public boolean isEqualValue(HeaderValue other) {
        if (other.sort() == HeaderSort.Accept_Encoding) {
            HeaderValueAcceptEncoding other2 = (HeaderValueAcceptEncoding) other;
            int includedCount = 0;
            for (Coding c : other2.codingList) {
                if (isInclude(c)) {
                    includedCount++;
                }
            }
            if (includedCount == other2.codingList.size()) {
                return true;
            }
        }
        return false;
    }

    private boolean isInclude(Coding other) {
        for (Coding c : codingList) {
            if (c.isMatch(other)) {
                return true;
            }
        }
        return false;
    }

    public float getQvalue(ContentCodingSort coding) {
        float qvalue = -1;
        float qvalue_asterisk = -1;

        for (Coding cd : codingList) {
            float cd_qvalue = (cd.qvalue() == null) ? 1 : cd.qvalue();
            if (cd.contentCoding() == coding) {
                qvalue = Math.max(cd_qvalue, qvalue);
            } else if (cd.isAsterisk()) {
                qvalue_asterisk = Math.max(cd_qvalue, qvalue_asterisk);
            }
        }

        if (qvalue != -1) {
            return qvalue;
        } else {
            return qvalue_asterisk;
        }
    }

    public Coding addNewCoding() {
        Coding c = new Coding();
        codingList.add(c);
        return c;
    }

    public Coding[] codings() {
        return codingList.toArray(Zero_Array);
    }


    public ContentCodingSort getMostAppropriateCodingSort() {
        ContentCodingSort result = ContentCodingSort.Unknown;
        float maxQvalue = -1;
        for (Coding c : codingList) {
            float qvalue = (c.qvalue() == null) ? 1 : c.qvalue();
            if (maxQvalue < qvalue) {
                result = c.contentCoding();
                maxQvalue = qvalue;
            }
        }
        return result;
    }
}
