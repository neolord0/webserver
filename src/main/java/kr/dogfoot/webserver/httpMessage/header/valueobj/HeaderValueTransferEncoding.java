package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.TransferCodingSort;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.server.resource.filter.part.condition.CompareOperator;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

import java.util.ArrayList;

public class HeaderValueTransferEncoding extends HeaderValue {
    private static final TransferCodingSort[] Zero_Array = new TransferCodingSort[0];
    private ArrayList<TransferCodingSort> transferCodingSortList;

    public HeaderValueTransferEncoding() {
        transferCodingSortList = new ArrayList<TransferCodingSort>();
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.Transfer_Encoding;
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.ioff = 0;
        ps.bufend = value.length;
        ps.separator = HttpString.Comma;
        ps.spaceIsSep = false;

        String tcs;
        while (ByteParser.nextItem(value, ps) >= 0) {
            tcs = ps.toString(value);
            if (tcs != null) {
                transferCodingSortList.add(TransferCodingSort.fromString(tcs));
            }
        }
        ParseState.release(ps);
    }

    @Override
    public byte[] combineValue() {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer.appendStringArray(HttpString.Comma, transferCodingSortList.toArray());
        byte[] ret = buffer.getBytes();
        OutputBuffer.release(buffer);
        return ret;
    }

    @Override
    public boolean compare(CompareOperator compareOp, String value) {
        return false;
    }

    public void addTransferCoding(TransferCodingSort transferCoding) {
        transferCodingSortList.add(transferCoding);
    }

    public TransferCodingSort[] transferCodings() {
        return transferCodingSortList.toArray(Zero_Array);
    }

    public boolean isChucked() {
        for (TransferCodingSort tcs : transferCodingSortList) {
            if (tcs == TransferCodingSort.Chunked) {
                return true;
            }
        }
        return false;
    }
}
