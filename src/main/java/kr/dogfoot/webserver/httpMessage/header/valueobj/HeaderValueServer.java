package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.BytesUtil;

public class HeaderValueServer extends HeaderValue {
    private byte[] infos;

    @Override
    public HeaderSort sort() {
        return HeaderSort.Server;
    }

    @Override
    public void reset() {
        infos = null;
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
    public boolean isEqualValue(HeaderValue other) {
        if (other.sort() == HeaderSort.Server) {
            HeaderValueServer other2 = (HeaderValueServer) other;

            return BytesUtil.compareWithNull(infos, other2.infos) == 0;
        }
        return false;
    }

    public byte[] infos() {
        return infos;
    }

    public void infos(byte[] infos) {
        this.infos = infos;
    }
}
