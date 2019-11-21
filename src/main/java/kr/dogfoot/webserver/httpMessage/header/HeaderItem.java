package kr.dogfoot.webserver.httpMessage.header;

import kr.dogfoot.webserver.httpMessage.header.valueobj.FactoryForHeaderValue;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValue;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.server.resource.filter.part.condition.CompareOperator;
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
        return valueObj;
    }

    public HeaderValue createValueObj() {
        if (valueObj == null) {
            valueObj = FactoryForHeaderValue.create(sort);
        }
        return valueObj;
    }

    public byte[] updateValueBytes() {
        if (valueObj != null) {
            valueBytes = valueObj.combineValue();
        }
        return valueBytes;
    }

    public HeaderValue updateValueObj() throws ParserException {
        if (valueObj == null) {
            valueObj = FactoryForHeaderValue.create(sort);
        }
        if (valueBytes != null && valueObj != null) {
            valueObj.parseValue(valueBytes);
        }
        return valueObj;
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
