package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.MediaType;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

public class HeaderValueContentType extends HeaderValue {
    private MediaType mediaType;

    public HeaderValueContentType() {
        mediaType = new MediaType();
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.Content_Type;
    }

    @Override
    public void reset() {
        mediaType.reset();
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.start = 0;
        ps.end = value.length;

        mediaType.parse(value, ps);
        ParseState.release(ps);
    }

    @Override
    public byte[] combineValue() {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        mediaType.append(buffer);
        return buffer.getBytesAndRelease();
    }

    @Override
    public boolean isEqualValue(HeaderValue other) {
        if (other.sort() == HeaderSort.Content_Type) {
            HeaderValueContentType other2 = (HeaderValueContentType) other;
            if (mediaType.isMatch(other2.mediaType)) {
                return true;
            }
        }
        return false;
    }

    public MediaType mediaType() {
        return mediaType;
    }

    public boolean isText() {
        return mediaType.type().equalsIgnoreCase(HttpString.Text_Type);
    }
}
