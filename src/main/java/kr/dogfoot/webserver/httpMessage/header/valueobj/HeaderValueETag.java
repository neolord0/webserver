package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.BytesUtil;
import kr.dogfoot.webserver.util.http.HttpString;

public class HeaderValueETag extends HeaderValue {
    private byte[] etag;

    public HeaderValueETag() {
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.ETag;
    }

    @Override
    public void reset() {
        etag = null;
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        etag = BytesUtil.removeDQuote(value);
    }


    @Override
    public byte[] combineValue() {
        return etag;
    }

    @Override
    public boolean isEqualValue(HeaderValue other) {
        if (other.sort() == HeaderSort.ETag) {
            HeaderValueETag other2 = (HeaderValueETag) other;

            return BytesUtil.compareWithNull(etag, other2.etag) == 0;
        }
        return false;
    }

    public byte[] etag() {
        return etag;
    }

    public void etag(byte[] etag) {
        this.etag = etag;
    }

    public boolean isWeak() {
        return BytesUtil.startWith(etag, HttpString.WeakValidator_Prefix);
    }
}
