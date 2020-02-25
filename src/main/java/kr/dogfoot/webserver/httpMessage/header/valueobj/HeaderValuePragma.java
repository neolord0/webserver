package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.BytesUtil;

public class HeaderValuePragma extends HeaderValue {
    private byte[] value;

    @Override
    public HeaderSort sort() {
        return HeaderSort.Pragma;
    }

    @Override
    public void reset() {
        value = null;
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        this.value = value;
    }

    @Override
    public byte[] combineValue() {
        return value;
    }

    @Override
    public boolean isEqualValue(HeaderValue other) {
        if (other.sort() == HeaderSort.Pragma) {
            HeaderValuePragma other2 = (HeaderValuePragma) other;

            return BytesUtil.compareWithNull(value, other2.value) == 0;
        }
        return false;
    }

    public byte[] value() {
        return value;
    }

    public void value(byte[] value) {
        this.value = value;
    }
}
