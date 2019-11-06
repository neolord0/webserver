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
            }
        }
        ParseState.release(ps);
    }

    @Override
    public byte[] combineValue() {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer.appendArray(HttpString.Comma, codingList.toArray());
        byte[] ret = buffer.getBytes();
        OutputBuffer.release(buffer);
        return ret;
    }

    @Override
    public Float getQvalue(String compare) {
        if (compare == null) {
            return (float) -1;
        }
        return getQvalue(ContentCodingSort.fromString(compare));
    }

    public float getQvalue(ContentCodingSort coding) {
        float qvalue = -1;
        float qvalue_asterisk = -1;

        for (Coding cd : codingList) {
            float cd_qvalue = (cd.getQvalue() == null) ? 1 : cd.getQvalue();
            if (cd.getContentCoding() == coding) {
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
            float qvalue = (c.getQvalue() == null) ? 1 : c.getQvalue();
            if (maxQvalue < qvalue) {
                result = c.getContentCoding();
                maxQvalue = qvalue;
            }
        }
        return result;
    }
}
