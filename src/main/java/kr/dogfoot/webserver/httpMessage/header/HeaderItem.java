package kr.dogfoot.webserver.httpMessage.header;

import kr.dogfoot.webserver.httpMessage.header.valueobj.FactoryForHeaderValue;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValue;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.http.HttpString;

public class HeaderItem {
    private HeaderSort sort;
    private byte[] valueBytes;
    private HeaderValue valueObj;

    public HeaderItem() {
    }

    public HeaderSort sort() {
        return sort;
    }

    public HeaderItem sort(HeaderSort sort) {
        this.sort = sort;
        return this;
    }

    public byte[] valueBytes() {
        return valueBytes;
    }

    public HeaderItem valueBytes(byte[] valueBytes) {
        this.valueBytes = valueBytes;
        return this;
    }

    public HeaderValue valueObj() {
        if (valueObj == null) {
            valueObj = FactoryForHeaderValue.create(sort);
            if (valueBytes != null) {
                try {
                    valueObj.parseValue(valueBytes);
                } catch (ParserException e) {
                    valueObj.reset();
                }
            }
        }

        return valueObj;
    }

    public void updateValueBytes() {
        if (valueObj != null) {
            valueBytes = valueObj.combineValue();
        }
    }

    public void updateValueObj() {
        if (valueBytes != null && valueObj != null) {
            valueObj.reset();

            try {
                valueObj.parseValue(valueBytes);
            } catch (ParserException e) {
                valueObj.reset();
            }
        }
    }

    public long calculateContextLength() {
        long size = 0;
        size += sort.toString().getBytes().length;
        size += HttpString.HeaderSeparator.length;
        size += valueBytes.length;
        size += HttpString.CRLF.length;
        return size;
    }

}
