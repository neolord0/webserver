package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ParserException;

public class HeaderValueContentMD5 extends HeaderValue {
    private byte[] md5Digest;

    @Override
    public HeaderSort sort() {
        return HeaderSort.Content_MD5;
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        md5Digest = value;
    }

    @Override
    public byte[] combineValue() {
        return md5Digest;
    }

    public byte[] md5Digest() {
        return md5Digest;
    }

    public void md5Digest(byte[] md5Digest) {
        this.md5Digest = md5Digest;
    }
}
