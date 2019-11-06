package kr.dogfoot.webserver.httpMessage.header;

import kr.dogfoot.webserver.context.connection.http.parserstatus.ParsingBuffer;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValue;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueContentLength;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueTransferEncoding;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.BytesUtil;
import kr.dogfoot.webserver.util.http.HttpString;

import java.util.ArrayList;

public class HeaderList {
    private final static HeaderItem[] Zero_Array = new HeaderItem[0];
    private ArrayList<HeaderItem> headers;
    private int contentLength;

    public HeaderList() {
        headers = new ArrayList<HeaderItem>();
        contentLength = -1;
    }

    public void reset() {
        headers.clear();
        contentLength = -1;
    }

    public int count() {
        return headers.size();
    }

    public HeaderItem getHeader(int index) {
        if (index < headers.size()) {
            return headers.get(index);
        }
        return null;
    }

    public boolean has(HeaderSort sort) {
        for (HeaderItem item : headers) {
            if (item.sort() == sort) {
                return true;
            }
        }
        return false;
    }

    public HeaderItem getHeader(HeaderSort sort) {
        for (HeaderItem item : headers) {
            if (item.sort() == sort) {
                return item;
            }
        }
        return null;
    }

    public void addHeaderFromBytes(HeaderSort sort, byte[] value) {
        HeaderItem item = new HeaderItem()
                .sort(sort)
                .valueBytes(value);
        headers.add(item);

        pre_parse(item);
    }

    public void pre_parse(HeaderItem item) {
        switch (item.sort()) {
            case Content_Length:
                parseContentLength(item);
                break;
            case Transfer_Encoding:
                parseTransferEncoding(item);
                break;
        }
    }

    private void parseContentLength(HeaderItem item) {
        try {
            HeaderValueContentLength contentLength = (HeaderValueContentLength) item.updateValueObj();
            this.contentLength = contentLength.value();
        } catch (ParserException e) {
            e.printStackTrace();
        }

    }

    private void parseTransferEncoding(HeaderItem item) {
        try {
            item.updateValueObj();
        } catch (ParserException e) {
            e.printStackTrace();
        }
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
            pre_parse(item);
        }
    }

    public HeaderValue getValueObj(HeaderSort sort) {
        HeaderValue valueObj = null;

        HeaderItem item = getHeader(sort);
        if (item != null) {
            try {
                valueObj = item.updateValueObj();
            } catch (ParserException e) {
                e.printStackTrace();
                valueObj = null;
            }
        }
        return valueObj;
    }

    public byte[] getValueBytes(HeaderSort sort) {
        HeaderItem item = getHeader(sort);
        if (item != null) {
            return item.valueBytes();
        }
        return null;
    }

    public int getContentLength() {
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

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        for (HeaderItem item : headers) {
            sb
                    .append(item.sort())
                    .append(HttpString.Equal)
                    .append(new String(item.valueBytes()))
                    .append(HttpString.CRLF);
        }
        return sb.toString();
    }

    public HeaderItem[] getHeaderByteArray() {
        return headers.toArray(Zero_Array);
    }

    public void remove(HeaderSort sort) {
        for (HeaderItem item : headers) {
            if (item.sort() == sort) {
                headers.remove(item);
                break;
            }
        }
    }

    public long calculateContextLength() {
        long size = 0;
        for (HeaderItem item : headers) {
            size += item.calculateContextLength();
        }
        return size;
    }

    public boolean hasBody() {
        return getContentLength() > 0 || isChunked();
    }
}


