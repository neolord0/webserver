package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.http.HttpString;
import kr.dogfoot.webserver.util.string.StringUtils;

import java.nio.charset.StandardCharsets;

public class HeaderValueExpect extends HeaderValue {
    private String token;

    public HeaderValueExpect() {
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.Expect;
    }

    @Override
    public void reset() {
        token = null;
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        token = new String(value, StandardCharsets.ISO_8859_1);
    }

    @Override
    public byte[] combineValue() {
        return token.getBytes();
    }

    @Override
    public boolean isEqualValue(HeaderValue other) {
        if (other.sort() == HeaderSort.Expect) {
            HeaderValueExpect other2 = (HeaderValueExpect) other;

            return StringUtils.equalsWithNull(token, other2.token);
        }
        return false;
    }

    public String token() {
        return token;
    }

    public void token(String token) {
        this.token = token;
    }

    public boolean is100Continue() {
        return HttpString.Continue100.equalsIgnoreCase(token);
    }
}
