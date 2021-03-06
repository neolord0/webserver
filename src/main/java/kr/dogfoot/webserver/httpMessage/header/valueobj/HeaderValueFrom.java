package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.string.StringUtils;

import java.nio.charset.StandardCharsets;

public class HeaderValueFrom extends HeaderValue {
    private String mailbox;

    public HeaderValueFrom() {
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.From;
    }

    @Override
    public void reset() {
        mailbox = null;
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        mailbox = new String(value, StandardCharsets.ISO_8859_1);
    }

    @Override
    public byte[] combineValue() {
        return mailbox.getBytes();
    }

    @Override
    public boolean isEqualValue(HeaderValue other) {
        if (other.sort() == HeaderSort.From) {
            HeaderValueFrom other2 = (HeaderValueFrom) other;

            return StringUtils.equalsWithNull(mailbox, other2.mailbox);
        }
        return false;
    }
}
