package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.server.resource.filter.part.condition.CompareOperator;

public class HeaderValueProxyAuthenticate extends HeaderValue {
    private byte[] challenges;

    public HeaderValueProxyAuthenticate() {
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.Proxy_Authenticate;
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
    public boolean compare(CompareOperator compareOp, String value) {
        return false;
    }

    public byte[] challenges() {
        return challenges;
    }

    public void challenges(byte[] challenges) {
        this.challenges = challenges;
    }
}
