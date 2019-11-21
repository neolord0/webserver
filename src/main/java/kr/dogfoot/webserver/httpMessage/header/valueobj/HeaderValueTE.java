package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.TransferCodingSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.TransferEncoding;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.server.resource.filter.part.condition.CompareOperator;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

import java.util.ArrayList;

public class HeaderValueTE extends HeaderValue {
    private static final TransferEncoding[] Zero_Array = new TransferEncoding[0];
    private ArrayList<TransferEncoding> transferEncodingList;

    public HeaderValueTE() {
        transferEncodingList = new ArrayList<TransferEncoding>();
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.TE;
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.ioff = 0;
        ps.bufend = value.length;
        ps.separator = HttpString.Comma;
        ps.spaceIsSep = false;
        ps.spaceIsSep = false;

        while (ByteParser.nextItem(value, ps) >= 0) {
            TransferEncoding te = new TransferEncoding();
            try {
                te.parse(value, ps);

                transferEncodingList.add(te);
            } catch (ParserException e) {
                e.printStackTrace();
            }
        }
        ParseState.release(ps);
    }

    @Override
    public byte[] combineValue() {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer.appendArray(HttpString.Comma, transferEncodingList.toArray());
        byte[] ret = buffer.getBytes();
        OutputBuffer.release(buffer);
        return ret;
    }

    @Override
    public Float getQvalue(String compare){
        if (compare == null) {
            return (float) -1;
        }

        float qvalue = -1;

        for (TransferEncoding te : transferEncodingList) {
            float te_qvalue = (te.getQvalue() == null) ? 1 : te.getQvalue();

            if (compare.equalsIgnoreCase(te.getSort().toString())) {
                qvalue = Math.max(te_qvalue, qvalue);
            }
        }

        return qvalue;
    }

    public TransferEncoding addNewTransferEncoding() {
        TransferEncoding te = new TransferEncoding();
        transferEncodingList.add(te);
        return te;
    }

    public TransferEncoding[] transferEncodings() {
        return transferEncodingList.toArray(Zero_Array);
    }
}
