package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.ContentCodingSort;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

import java.util.ArrayList;

public class HeaderValueContentEncoding extends HeaderValue {
    private static final ContentCodingSort[] Zero_Array = new ContentCodingSort[0];
    private ArrayList<ContentCodingSort> contentCodingList;

    public HeaderValueContentEncoding() {
        contentCodingList = new ArrayList<ContentCodingSort>();
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.Content_Encoding;
    }

    @Override
    public void reset() {
        contentCodingList.clear();
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.ioff = 0;
        ps.bufend = value.length;
        ps.separator = HttpString.Comma;
        ps.spaceIsSep = false;

        String contentCoding;
        while (ByteParser.nextItem(value, ps) >= 0) {
            contentCoding = ps.toString(value);

            if (contentCoding != null) {
                contentCodingList.add(ContentCodingSort.fromString(contentCoding));
            }
        }
        ParseState.release(ps);
    }

    @Override
    public byte[] combineValue() {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer.appendStringArray(HttpString.Comma, contentCodingList.toArray());
        return buffer.getBytesAndRelease();
    }

    @Override
    public boolean isEqualValue(HeaderValue other) {
        if (other.sort() == HeaderSort.Content_Encoding) {
            HeaderValueContentEncoding other2 = (HeaderValueContentEncoding) other;
            int includedCount = 0;
            for (ContentCodingSort cc : other2.contentCodingList) {
                if (isInclude(cc)) {
                    includedCount++;
                }
            }
            if (includedCount == other2.contentCodingList.size()) {
                return true;
            }
        }
        return false;
    }

    private boolean isInclude(ContentCodingSort other) {
        for (ContentCodingSort cc : contentCodingList) {
            if (cc == other) {
                return true;
            }
        }
        return false;
    }

    public void addContentCoding(ContentCodingSort contentCoding) {
        contentCodingList.add(contentCoding);
    }

    public ContentCodingSort[] contentCodings() {
        return contentCodingList.toArray(Zero_Array);
    }

    public boolean isIdentity() {
        for (ContentCodingSort coding : contentCodingList) {
            if (coding != ContentCodingSort.Identity) {
                return false;
            }
        }
        return true;
    }
}
