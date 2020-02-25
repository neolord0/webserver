package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.BytesUtil;

public class HeaderValueContentMD5 extends HeaderValue {
    private byte[] md5Digest;

    @Override
    public HeaderSort sort() {
        return HeaderSort.Content_MD5;
    }

    @Override
    public void reset() {
        md5Digest = null;
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        md5Digest = value;
    }

    @Override
    public byte[] combineValue() {
        return md5Digest;
    }

    @Override
    public boolean isEqualValue(HeaderValue other) {
        if (other.sort() == HeaderSort.Content_MD5) {
            HeaderValueContentMD5 other2 = (HeaderValueContentMD5) other;

            return BytesUtil.compareWithNull(md5Digest, other2.md5Digest) == 0;
        }
        return false;
    }

    public byte[] md5Digest() {
        return md5Digest;
    }

    public void md5Digest(byte[] md5Digest) {
        this.md5Digest = md5Digest;
    }
}
