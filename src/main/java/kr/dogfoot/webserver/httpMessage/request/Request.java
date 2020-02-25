package kr.dogfoot.webserver.httpMessage.request;

import kr.dogfoot.webserver.httpMessage.header.HeaderItem;
import kr.dogfoot.webserver.httpMessage.header.HeaderList;
import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.*;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.CacheDirectiveSort;
import kr.dogfoot.webserver.util.bytes.BytesUtil;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

import java.util.Date;

public class Request {
    private MethodType method;
    private URI requestURI;
    private short majorVersion;
    private short minorVersion;

    private HeaderList headerList;
    private OutputBuffer bodyBuffer;

    private long requestTime;

    public Request() {
        method = MethodType.Unknown;
        requestURI = new URI();
        majorVersion = -1;
        minorVersion = -1;

        headerList = new HeaderList();
        bodyBuffer = OutputBuffer.pooledObject();

        requestTime = 0;
    }

    public void reset() {
        method = MethodType.Unknown;
        requestURI.reset();
        majorVersion = -1;
        minorVersion = -1;

        headerList.reset();
        bodyBuffer.reset();

        requestTime = 0;
    }

    public boolean isSafe() {
        return method.isSafe();
    }

    public MethodType method() {
        return method;
    }

    public void method(MethodType method) {
        this.method = method;
    }

    public URI requestURI() {
        return requestURI;
    }

    public short majorVersion() {
        return majorVersion;
    }

    public void majorVersion(short majorVersion) {
        this.majorVersion = majorVersion;
    }

    public short minorVersion() {
        return minorVersion;
    }

    public void minorVersion(short minorVersion) {
        this.minorVersion = minorVersion;
    }

    public HeaderList headerList() {
        return headerList;
    }

    @Override
    public String toString() {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer.append(method.getBytes()).append(HttpString.Space)
                .append(requestURI.toString()).append(HttpString.Space)
                .appendInt(majorVersion).append(HttpString.Dot).appendInt(minorVersion)
                .append(HttpString.CRLF);
        headerList.appendTo(buffer);

        return new String(buffer.getBytesAndRelease());
    }

    public boolean hasBody() {
        return headerList.hasBody();
    }

    public int contentLength() {
        return headerList.contentLength();
    }

    public boolean hasContentLength() {
        return headerList.contentLength() != -1;
    }

    public boolean isChunked() {
        return headerList.isChunked();
    }

    public boolean hasHeader(HeaderSort sort) {
        return headerList.has(sort);
    }

    public HeaderValue getHeaderValueObj(HeaderSort sort) {
        HeaderItem item = headerList.getHeader(sort);
        if (item != null) {
            return item.valueObj();
        }
        return null;
    }

    public boolean isPersistentConnection() {
        HeaderValueConnection connection = (HeaderValueConnection) getHeaderValueObj(HeaderSort.Connection);
        if (majorVersion > 2 || majorVersion == 1 && minorVersion >= 1) {
            return connection == null || connection.isClose() == false;
        } else if (majorVersion == 1 && minorVersion == 0) {
            return connection != null && connection.isKeepAlive() == true;
        }
        return false;
    }

    public boolean hasExpect100Continue() {
        HeaderValueExpect expect = (HeaderValueExpect) getHeaderValueObj(HeaderSort.Expect);
        return expect != null && expect.is100Continue();
    }

    public boolean isEmptyBody() {
        return bodyBuffer.getLength() == 0;
    }

    public byte[] bodyBytes() {
        return bodyBuffer.getBytes();
    }

    public void appendBodyBytes(byte[] bytes, int position, int length) {
        bodyBuffer.append(bytes, position, length);
    }

    public long requestTime() {
        return requestTime;
    }

    public void requestTime(long requestTime) {
        this.requestTime = requestTime;
    }

    public void setRequestTimeToNow() {
        requestTime = new Date().getTime();
    }

    public boolean hasCacheDirective(CacheDirectiveSort directiveSort) {
        HeaderValueCacheControl cacheControl = (HeaderValueCacheControl) getHeaderValueObj(HeaderSort.Cache_Control);
        if (cacheControl != null) {
            return cacheControl.hasCacheDirective(directiveSort);
        }
        return false;
    }

    public boolean hasNoCache() {
        if (hasCacheDirective(CacheDirectiveSort.NoCache) || hasNoCachePragma()) {
            return true;
        }
        return false;
    }

    private boolean hasNoCachePragma() {
        HeaderValuePragma pragma = (HeaderValuePragma) getHeaderValueObj(HeaderSort.Pragma);
        if (pragma != null) {
            if (BytesUtil.compare(pragma.value(), HttpString.No_Cache) == 0) {
                return true;
            }
        }
        return false;
    }

    public Request clone() {
        Request cloned = new Request();
        cloned.method = method;
        cloned.requestURI.copyFrom(requestURI);
        cloned.majorVersion = majorVersion;
        cloned.minorVersion = minorVersion;
        cloned.headerList.copyFrom(headerList);
        cloned.bodyBuffer.copyFrom(bodyBuffer);
        cloned.requestTime = requestTime;
        return cloned;
    }
}
