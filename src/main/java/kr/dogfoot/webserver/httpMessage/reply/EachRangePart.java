package kr.dogfoot.webserver.httpMessage.reply;

import kr.dogfoot.webserver.httpMessage.header.HeaderItem;
import kr.dogfoot.webserver.httpMessage.header.HeaderList;
import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValue;
import kr.dogfoot.webserver.server.resource.performer.util.ContentRange;
import kr.dogfoot.webserver.util.http.HttpString;

public class EachRangePart {
    protected HeaderList headerList;
    private ContentRange range;

    public EachRangePart() {
        headerList = new HeaderList();
        range = null;
    }


    public EachRangePart setHeader(HeaderSort sort, byte[] value) {
        if (hasHeader(sort)) {
            changeHeader(sort, value);
        } else {
            addHeader(sort, value);
        }
        return this;
    }

    public boolean hasHeader(HeaderSort sort) {
        return headerList.has(sort);
    }

    public EachRangePart addHeader(HeaderSort sort, byte[] value) {
        headerList.addHeaderFromBytes(sort, value);
        return this;
    }

    public EachRangePart changeHeader(HeaderSort sort, byte[] value) {
        HeaderItem item = getHeaderItem(sort);
        if (item != null) {
            item.valueBytes(value);
            headerList.pre_parse(item);
        }
        return this;
    }


    public HeaderValue getHeaderValueObj(HeaderSort sort) {
        return headerList.getValueObj(sort);
    }

    public EachRangePart removeHeader(HeaderSort sort) {
        headerList.remove(sort);
        return this;
    }

    public HeaderItem getHeaderItem(int index) {
        return headerList.getHeader(index);
    }

    public HeaderItem getHeaderItem(HeaderSort sort) {
        return headerList.getHeader(sort);
    }

    public int headerCount() {
        return headerList.count();
    }

    public HeaderList headerList() {
        return headerList;
    }

    public boolean hasBody() {
        return headerList.hasBody();
    }

    public boolean isChunked() {
        return headerList.isChunked();
    }

    public ContentRange range() {
        return range;
    }

    public EachRangePart range(ContentRange range) {
        this.range = range;
        return this;
    }

    public long calculateContextLength() {
        long size = 0;
        size += headerList.calculateContextLength();
        size += HttpString.CRLF.length;
        size += range.length();
        return size;
    }
}
