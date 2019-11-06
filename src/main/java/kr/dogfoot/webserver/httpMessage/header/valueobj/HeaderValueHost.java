package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.server.resource.filter.part.condition.CompareOperator;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

public class HeaderValueHost extends HeaderValue {
    private String ipOrDomain;
    private int port;

    public HeaderValueHost() {
        ipOrDomain = null;
        port = -1;
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.Host;
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.ioff = 0;
        ps.bufend = value.length;
        ps.separator = HttpString.Colon;

        if (ByteParser.nextItem(value, ps) < 0) {
            ipOrDomain = null;
            port = -1;
        } else {
            ipOrDomain = ps.toString(value);
            if (ps.isEnd() == false) {
                ps.rest();
                this.port = ByteParser.parseInt(value, ps);
            }
        }
        ParseState.release(ps);
    }

    @Override
    public byte[] combineValue() {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer.append(ipOrDomain);
        if (port != -1) {
            buffer
                    .append(HttpString.Colon)
                    .append(port);
        }
        byte[] ret = buffer.getBytes();
        OutputBuffer.release(buffer);
        return ret;
    }

    @Override
    public boolean compare(CompareOperator compareOp, String value) {
        return false;
    }

    public String ipOrDomain() {
        return ipOrDomain;
    }

    public int port() {
        return port;
    }
}
