package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.BytesUtil;

public class HeaderValueWWWAuthenticate extends HeaderValue {
    private byte[] challenges;

    @Override
    public HeaderSort sort() {
        return HeaderSort.WWW_Authenticate;
    }

    @Override
    public void reset() {
        challenges = null;
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        challenges = value;
    }

    @Override
    public byte[] combineValue() {
        return challenges;
    }

    @Override
    public boolean isEqualValue(HeaderValue other) {
        if (other.sort() == HeaderSort.WWW_Authenticate) {
            HeaderValueWWWAuthenticate other2 = (HeaderValueWWWAuthenticate) other;

            return BytesUtil.compareWithNull(challenges, other2.challenges) == 0;
        }
        return false;
    }

    public byte[] challenges() {
        return challenges;
    }

    public void challenges(byte[] challenges) {
        this.challenges = challenges;
    }
}
