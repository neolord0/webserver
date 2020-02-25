package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;
import kr.dogfoot.webserver.util.string.StringUtils;

public class HeaderValueProxyAuthorization extends HeaderValue {
    private String type;
    private String credentials;

    @Override
    public HeaderSort sort() {
        return HeaderSort.Proxy_Authorization;
    }

    @Override
    public void reset() {
        type = null;
        credentials = null;
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.ioff = 0;
        ps.bufend = value.length;
        ps.separator = HttpString.Space;
        ps.spaceIsSep = false;
        ps.permitNoSeparator = false;

        if (ByteParser.nextItem(value, ps) < 0) {
            throw new ParserException("Invalidate parameter.");
        }

        this.type = ps.toString(value);
        ps.rest();
        this.credentials = ps.toString(value);
        ParseState.release(ps);
    }

    @Override
    public byte[] combineValue() {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer.append(type).append(HttpString.Space).append(credentials);
        return buffer.getBytesAndRelease();
    }

    @Override
    public boolean isEqualValue(HeaderValue other) {
        if (other.sort() == HeaderSort.Proxy_Authorization) {
            HeaderValueProxyAuthorization other2 = (HeaderValueProxyAuthorization) other;

            return StringUtils.equalsIgnoreCaseWithNull(type, other2.type)
                    && StringUtils.equalsIgnoreCaseWithNull(credentials, other2.credentials);
        }
        return false;
    }

    public String type() {
        return type;
    }

    public void type(String type) {
        this.type = type;
    }

    public String credentials() {
        return credentials;
    }

    public void credentials(String credentials) {
        this.credentials = credentials;
    }
}
