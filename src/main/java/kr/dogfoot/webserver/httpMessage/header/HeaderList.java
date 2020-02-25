package kr.dogfoot.webserver.httpMessage.header;

import kr.dogfoot.webserver.context.connection.http.parserstatus.ParsingBuffer;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValue;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueContentLength;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueTransferEncoding;
import kr.dogfoot.webserver.server.resource.filter.part.condition.CompareOperator;
import kr.dogfoot.webserver.util.bytes.BytesUtil;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

import java.util.ArrayList;


public class HeaderList {
    private final static HeaderItem[] Zero_Array = new HeaderItem[0];
    private ArrayList<HeaderItem> headerList;
    private int contentLength;

    public HeaderList() {
        headerList = new ArrayList<HeaderItem>();
        contentLength = -1;
    }

    public void reset() {
        headerList.clear();
        contentLength = -1;
    }

    public int count() {
        return headerList.size();
    }

    public HeaderItem getHeader(int index) {
        if (index < headerList.size()) {
            return headerList.get(index);
        }
        return null;
    }

    public boolean has(HeaderSort sort) {
        return getHeader(sort) != null;
    }

    public HeaderItem getHeader(HeaderSort sort) {
        for (HeaderItem item : headerList) {
            if (item.sort() == sort) {
                return item;
            }
        }
        return null;
    }

    public HeaderItem addHeaderFromBytes(HeaderSort sort, byte[] value) {
        HeaderItem item = new HeaderItem()
                .sort(sort)
                .valueBytes(value);
        headerList.add(item);
        pre_parse(item);

        return item;
    }

    public HeaderItem addHeader(HeaderSort sort) {
        HeaderItem item = new HeaderItem()
                .sort(sort);
        headerList.add(item);
        return item;
    }

    public HeaderItem changeHeader(HeaderSort sort, byte[] value) {
        HeaderItem item = getHeader(sort);
        if (item != null) {
            item.valueBytes(value);
            item.updateValueObj();

            pre_parse(item);
        }
        return item;
    }

    private void pre_parse(HeaderItem item) {
        switch (item.sort()) {
            case Content_Length:
                parseContentLength(item);
                break;
        }
    }

    private void parseContentLength(HeaderItem item) {
        HeaderValueContentLength contentLength = (HeaderValueContentLength) item.valueObj();
        this.contentLength = contentLength.value();
    }

    public void continueBytes(HeaderSort sort, ParsingBuffer buffer) {
        HeaderItem item = getHeader(sort);
        if (item != null && buffer.length() > 0) {
            if (item.valueBytes() == null) {
                item.valueBytes(buffer.newBytes());
            } else {
                item.valueBytes(
                        BytesUtil.merge(
                                item.valueBytes(), 0, item.valueBytes().length,
                                buffer.newBytes(), 0, buffer.length())
                );
            }
            item.updateValueObj();

            pre_parse(item);
        }
    }

    public int contentLength() {
        return contentLength;
    }

    public boolean isChunked() {
        HeaderItem item = getHeader(HeaderSort.Transfer_Encoding);
        if (item != null) {
            HeaderValueTransferEncoding te = (HeaderValueTransferEncoding) item.valueObj();
            return te.isChucked();
        }
        return false;
    }

    public HeaderItem[] getHeaderItemArray() {
        return headerList.toArray(Zero_Array);
    }

    public void remove(HeaderSort sort) {
        for (HeaderItem item : headerList) {
            if (item.sort() == sort) {
                headerList.remove(item);
                break;
            }
        }
    }

    public void remove(HeaderItem item) {
        headerList.remove(item);
    }


    public long calculateContextLength() {
        long size = 0;
        for (HeaderItem item : headerList) {
            size += item.calculateContextLength();
        }
        return size;
    }

    public boolean hasBody() {
        return contentLength() > 0 || isChunked();
    }

    public boolean compare(HeaderSort headerSort, CompareOperator operator, String value) {
        HeaderItem item = getHeader(headerSort);
        if (operator == CompareOperator.Exist) {
            return (item != null);
        } else if (operator == CompareOperator.NotExist) {
            return (item == null);
        }
        if (item == null) {
            return false;
        }

        if (operator.compareWithBytes()) {
            return operator.compareπWithByte(item.valueBytes(), value.getBytes());
        }

        HeaderValue headerValue = item.valueObj();
        if (headerValue.getNumberValue() != null) {
            return operator.compareWithNumber(headerValue.getNumberValue(), Long.parseLong(value));
        } else if (headerValue.getDateValue() != null) {
            return operator.compareWithDate(headerValue.getDateValue(), value.getBytes());
        }
        return false;
    }

    public void copyFrom(HeaderList source) {
        for (HeaderItem hi : source.headerList) {
            addHeaderFromBytes(hi.sort(), hi.valueBytes());
        }
    }

    public void appendTo(OutputBuffer buffer) {
        for (HeaderItem item : headerList) {
            buffer.append(item.sort().toString().getBytes())
                    .append(HttpString.HeaderSeparator)
                    .append(item.valueBytes())
                    .append(HttpString.CRLF);
        }
    }
}
