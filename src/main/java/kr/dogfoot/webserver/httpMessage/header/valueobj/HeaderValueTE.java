package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.TransferEncoding;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
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
    public void reset() {
        transferEncodingList.clear();
        ;
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
        return buffer.getBytesAndRelease();
    }

    @Override
    public Float getQvalue(String compare) {
        if (compare == null) {
            return (float) -1;
        }

        float qvalue = -1;

        for (TransferEncoding te : transferEncodingList) {
            float te_qvalue = (te.qvalue() == null) ? 1 : te.qvalue();

            if (compare.equalsIgnoreCase(te.sort().toString())) {
                qvalue = Math.max(te_qvalue, qvalue);
            }
        }

        return qvalue;
    }

    @Override
    public boolean isEqualValue(HeaderValue other) {
        if (other.sort() == HeaderSort.TE) {
            HeaderValueTE other2 = (HeaderValueTE) other;
            int includedCount = 0;
            for (TransferEncoding te : other2.transferEncodingList) {
                if (isInclude(te)) {
                    includedCount++;
                }
            }
            if (includedCount == other2.transferEncodingList.size()) {
                return true;
            }
        }
        return false;
    }

    private boolean isInclude(TransferEncoding other) {
        for (TransferEncoding te : transferEncodingList) {
            if (te.isMatch(te)) {
                return true;
            }
        }
        return false;
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
