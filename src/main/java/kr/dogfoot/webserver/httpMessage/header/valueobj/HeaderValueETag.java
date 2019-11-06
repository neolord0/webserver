package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.server.resource.filter.part.condition.CompareOperator;

public class HeaderValueETag extends HeaderValue {
    private byte[] etag;

    public HeaderValueETag() {
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.ETag;
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        etag = value;
    }

    @Override
    public byte[] combineValue() {
        return etag;
    }

    @Override
    public boolean compare(CompareOperator compareOp, String value) {
        return false;
    }

    public byte[] etag() {
        return etag;
    }

    public void etag(byte[] etag) {
        this.etag = etag;
    }
}
