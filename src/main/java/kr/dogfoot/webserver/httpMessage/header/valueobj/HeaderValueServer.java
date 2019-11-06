package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.server.resource.filter.part.condition.CompareOperator;

public class HeaderValueServer extends HeaderValue {
    private byte[] infos;

    public HeaderValueServer() {
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.Server;
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        infos = value;
    }

    @Override
    public byte[] combineValue() {
        return infos;
    }

    @Override
    public boolean compare(CompareOperator compareOp, String value) {
        return false;
    }

    public byte[] infos() {
        return infos;
    }

    public void infos(byte[] infos) {
        this.infos = infos;
    }
}
