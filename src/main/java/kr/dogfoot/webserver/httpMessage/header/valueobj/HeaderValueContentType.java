package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.MediaType;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.server.resource.filter.part.condition.CompareOperator;
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
        byte[] ret = buffer.getBytes();
        OutputBuffer.release(buffer);
        return ret;
    }

    @Override
    public boolean compare(CompareOperator compareOp, String value) {
        if (compareOp == CompareOperator.Equal) {
            return mediaType.isMatch(value);
        } else if (compareOp == CompareOperator.NotEqual) {
            return !mediaType.isMatch(value);
        } else {
            return false;
        }
    }

    public MediaType mediaType() {
        return mediaType;
    }

    public boolean isText() {
        return mediaType.getType().equalsIgnoreCase(HttpString.Text_Type);
    }
}
