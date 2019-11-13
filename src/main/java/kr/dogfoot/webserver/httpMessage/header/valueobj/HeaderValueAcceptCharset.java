package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.Charset;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

import java.util.ArrayList;

public class HeaderValueAcceptCharset extends HeaderValue {
    private static final Charset[] Zero_Array = new Charset[0];
    private ArrayList<Charset> charsetList;

    public HeaderValueAcceptCharset() {
        charsetList = new ArrayList<Charset>();
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.Accept_Charset;
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.ioff = 0;
        ps.bufend = value.length;
        ps.separator = HttpString.Comma;
        ps.spaceIsSep = false;

        while (ByteParser.nextItem(value, ps) >= 0) {
            Charset cs = new Charset();
            try {
                cs.parse(value, ps);

                charsetList.add(cs);
            } catch (ParserException e) {
                e.printStackTrace();
            }
        }
        ParseState.release(ps);
    }

    @Override
    public byte[] combineValue() {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer.appendArray((byte) ',', charsetList.toArray());
        byte[] ret = buffer.getBytes();
        OutputBuffer.release(buffer);
        return ret;
    }

    @Override
    public Float getQvalue(String compare) {
        if (compare == null) {
            return (float) -1;
        }

        float qvalue = -1;
        float qvalue_asterisk = -1;

        for (Charset cs : charsetList) {
            String cd_text = cs.getCharset();
            float cs_qvalue = (cs.getQvalue() == null) ? 1 : cs.getQvalue();

            if (cd_text.equalsIgnoreCase(compare)) {
                qvalue = Math.max(cs_qvalue, qvalue);
            } else if (cd_text.equals(HttpString.Asterisk_String)) {
                qvalue_asterisk = Math.max(cs_qvalue, qvalue_asterisk);
            }
        }
        if (qvalue != -1) {
            return qvalue;
        } else {
            return qvalue_asterisk;
        }
    }

    public Charset addNewCharset() {
        Charset c = new Charset();
        charsetList.add(c);
        return c;
    }

    public Charset[] charsets() {
        return charsetList.toArray(Zero_Array);
    }

}
