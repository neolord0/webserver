package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.TransferCodingSort;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

import java.util.ArrayList;

public class HeaderValueTransferEncoding extends HeaderValue {
    private static final TransferCodingSort[] Zero_Array = new TransferCodingSort[0];
    private ArrayList<TransferCodingSort> transferCodingSortList;

    public HeaderValueTransferEncoding() {
        transferCodingSortList = new ArrayList<TransferCodingSort>();
    }

    public HeaderValueTransferEncoding(ArrayList<TransferCodingSort> transferCodingSortList) {
        this.transferCodingSortList = transferCodingSortList;
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.Transfer_Encoding;
    }

    @Override
    public void reset() {
        transferCodingSortList.clear();
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
        return buffer.getBytesAndRelease();
    }

    @Override
    public boolean isEqualValue(HeaderValue other) {
        if (other.sort() == HeaderSort.Transfer_Encoding) {
            HeaderValueTransferEncoding other2 = (HeaderValueTransferEncoding) other;
            int includedCount = 0;
            for (TransferCodingSort tcs : other2.transferCodingSortList) {
                if (isInclude(tcs)) {
                    includedCount++;
                }
            }
            if (includedCount == other2.transferCodingSortList.size()) {
                return true;
            }
        }
        return false;

    }

    private boolean isInclude(TransferCodingSort other) {
        for (TransferCodingSort tcs : transferCodingSortList) {
            if (tcs == other) {
                return true;
            }
        }
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
