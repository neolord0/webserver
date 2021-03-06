package kr.dogfoot.webserver.httpMessage.header.valueobj.part;

import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.AppendableToByte;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

import java.util.ArrayList;

public class TransferEncoding implements AppendableToByte {
    private TransferCodingSort sort;

    private Float qvalue;
    private ArrayList<Parameter> parameterList;

    public TransferEncoding() {
        parameterList = new ArrayList<Parameter>();
    }

    public void parse(byte[] value, ParseState parentPS) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.prepare(parentPS);
        ps.separator = HttpString.Semicolon;
        ps.spaceIsSep = false;

        boolean isFirst = true;
        while (ByteParser.nextItem(value, ps) >= 0) {
            if (isFirst) {
                if (value.equals(HttpString.Asterisk_String)) {

                }
                this.sort = TransferCodingSort.fromString(ps.toString(value));
                isFirst = false;
            } else {
                Parameter p = new Parameter();
                try {
                    p.parse(value, ps);

                    if (p.name().equalsIgnoreCase(HttpString.Q)) {
                        qvalue = new Float(p.value());
                    } else {
                        parameterList.add(p);
                    }
                } catch (ParserException e) {
                    e.printStackTrace();
                }
            }
        }
        ParseState.release(ps);
    }

    @Override
    public void append(OutputBuffer buffer) {
        buffer.append(sort.toString());
        if (qvalue != null) {
            buffer.append(HttpString.Semicolon).appendQValue(qvalue);
        }
        for (Parameter p : parameterList) {
            buffer.append(HttpString.Semicolon);
            p.append(buffer);
        }
    }

    public boolean isMatch(TransferEncoding other) {
        return sort == other.sort;
    }

    public TransferCodingSort sort() {
        return sort;
    }

    public void sort(TransferCodingSort sort) {
        this.sort = sort;
    }

    public Float qvalue() {
        return qvalue;
    }

    public void qvalue(Float qvalue) {
        this.qvalue = qvalue;
    }

    public ArrayList<Parameter> getParameterList() {
        return parameterList;
    }
}
