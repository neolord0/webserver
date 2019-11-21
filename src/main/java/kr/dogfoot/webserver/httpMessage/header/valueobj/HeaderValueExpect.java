package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.server.resource.filter.part.condition.CompareOperator;
import kr.dogfoot.webserver.util.http.HttpString;

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
    public void parseValue(byte[] value) throws ParserException {
        token = new String(value, StandardCharsets.ISO_8859_1);
    }

    @Override
    public byte[] combineValue() {
        return token.getBytes();
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
