package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.RecipientInfo;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

import java.util.ArrayList;

public class HeaderValueVia extends HeaderValue {
    private static final RecipientInfo[] Zero_Array = new RecipientInfo[0];
    private ArrayList<RecipientInfo> recipientInfoList;

    public HeaderValueVia() {
        recipientInfoList = new ArrayList<RecipientInfo>();
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.Via;
    }

    @Override
    public void reset() {
        recipientInfoList.clear();
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.ioff = 0;
        ps.bufend = value.length;
        ps.separator = HttpString.Comma;
        ps.spaceIsSep = false;

        while (ByteParser.nextItem(value, ps) >= 0) {
            RecipientInfo ri = new RecipientInfo();

            ri.parse(value, ps);
            recipientInfoList.add(ri);
        }
        ParseState.release(ps);
    }

    @Override
    public byte[] combineValue() {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer.appendArray((byte) ',', recipientInfoList.toArray());
        return buffer.getBytesAndRelease();
    }

    @Override
    public boolean isEqualValue(HeaderValue other) {
        if (other.sort() == HeaderSort.Via) {
            HeaderValueVia other2 = (HeaderValueVia) other;
            int includedCount = 0;
            for (RecipientInfo ri : other2.recipientInfoList) {
                if (isInclude(ri)) {
                    includedCount++;
                }
            }
            if (includedCount == other2.recipientInfoList.size()) {
                return true;
            }
        }
        return false;
    }

    private boolean isInclude(RecipientInfo other) {
        for (RecipientInfo ri : recipientInfoList) {
            if (ri.isMatch(other)) {
                return true;
            }
        }
        return false;

    }

    public RecipientInfo addNewRecipientInfo() {
        RecipientInfo ri = new RecipientInfo();
        recipientInfoList.add(ri);
        return ri;
    }

    public RecipientInfo[] recipientInfos() {
        return recipientInfoList.toArray(Zero_Array);
    }
}
