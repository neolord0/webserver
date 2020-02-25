package kr.dogfoot.webserver.httpMessage.response;

import kr.dogfoot.webserver.httpMessage.header.HeaderItem;
import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueCacheControl;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueETag;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.CacheDirectiveSort;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.server.resource.performer.util.ContentRange;
import kr.dogfoot.webserver.util.http.HttpString;
import kr.dogfoot.webserver.util.message.http.ResponseToBuffer;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Date;

public class Response extends EachRangePart {
    private static final int Default_RangePart_Count = 3;

    private short majorVersion;
    private short minorVersion;
    private StatusCode statusCode;
    private byte[] reason;

    private byte[] boundary;
    private EachRangePart[] rangeParts;
    private int rangePartCount;

    private byte[] bodyBytes;
    private File bodyFile;

    private Request request;
    private long responseTime;

    public Response() {
        super();

        majorVersion = 1;
        minorVersion = 1;
        statusCode = null;
        reason = null;

        bodyBytes = null;
        bodyFile = null;

        boundary = null;
        rangeParts = new EachRangePart[Default_RangePart_Count];
        rangePartCount = 0;

        request = null;
        responseTime = 0;
    }


    @Override
    public boolean hasHeader(HeaderSort sort) {
        return headerList.has(sort);
    }

    @Override
    public Response addHeader(HeaderSort sort, byte[] value) {
        super.addHeader(sort, value);
        return this;
    }

    @Override
    public Response changeHeader(HeaderSort sort, byte[] value) {
        super.changeHeader(sort, value);
        return this;
    }

    @Override
    public Response removeHeader(HeaderSort sort) {
        super.removeHeader(sort);
        return this;
    }

    @Override
    public Response removeHeader(HeaderItem item) {
        super.removeHeader(item);
        return this;
    }

    @Override
    public Response range(ContentRange range) {
        super.range(range);
        return this;
    }

    public short majorVersion() {
        return majorVersion;
    }

    public Response majorVersion(short majorVersion) {
        this.majorVersion = majorVersion;
        return this;
    }

    public short minorVersion() {
        return minorVersion;
    }

    public Response minorVersion(short minorVersion) {
        this.minorVersion = minorVersion;
        return this;
    }

    public StatusCode statusCode() {
        return statusCode;
    }

    public Response statusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public boolean isError() {
        return statusCode.isError();
    }

    public byte[] reason() {
        return reason;
    }

    public Response reason(byte[] reason) {
        this.reason = reason;
        return this;
    }

    public byte[] bodyBytes() {
        return bodyBytes;
    }

    public Response bodyBytes(byte[] bodyBytes) {
        this.bodyBytes = bodyBytes;
        return this;
    }

    public File bodyFile() {
        return bodyFile;
    }

    public Response bodyFile(File bodyFile) {
        this.bodyFile = bodyFile;
        return this;
    }

    public boolean isBodyFile() {
        return bodyFile != null;
    }

    public boolean isPartial() {
        return statusCode == StatusCode.Code206;
    }

    public byte[] boundary() {
        return boundary;
    }

    public Response boundary(byte[] boundary) {
        this.boundary = boundary;
        return this;
    }

    public EachRangePart addNewRangePart() {
        if (rangeParts.length <= rangePartCount) {
            EachRangePart[] newArray = new EachRangePart[rangeParts.length * 2];
            System.arraycopy(rangeParts, 0, newArray, 0, rangeParts.length);
            rangeParts = newArray;
        }
        rangeParts[rangePartCount++] = new EachRangePart();
        return rangeParts[rangePartCount - 1];
    }

    public int rangePartCount() {
        return rangePartCount;
    }

    public EachRangePart rangePart(int index) {
        return rangeParts[index];
    }

    public boolean hasContentLength() {
        return headerList.contentLength() != -1;
    }

    public int contentLength() {
        return headerList.contentLength();
    }

    public boolean hasKeepAlive() {
        return headerList.has(HeaderSort.Keep_Alive);
    }

    public boolean hasValidator() {
        HeaderValueETag eTag = (HeaderValueETag) getHeaderValueObj(HeaderSort.ETag);
        if (eTag != null && eTag.etag() != null && eTag.etag().length != 0) {
            return true;
        }
        return false;
    }

    public boolean hasWeakValidator() {
        HeaderValueETag eTag = (HeaderValueETag) getHeaderValueObj(HeaderSort.ETag);
        if (eTag != null && eTag.etag() != null && eTag.isWeak()) {
            return true;
        }
        return false;
    }

    @Override
    public int calculateContextLength() {
        if (isMultiPart() == false) {
            return super.calculateContextLength();
        } else {
            int size = 0;
            for (int index = 0; index < rangePartCount; index++) {
                size += sizeForStartBoundary();
                size += rangePart(index).calculateContextLength();
            }
            size += sizeForEndBoundary();
            return size;
        }
    }

    private boolean isMultiPart() {
        return rangePartCount >= 2;
    }

    public long sizeForStartBoundary() {
        return HttpString.CRLF.length
                + HttpString.BoundaryPrefix.length
                + boundary.length
                + HttpString.CRLF.length;
    }

    public long sizeForEndBoundary() {
        return HttpString.CRLF.length
                + HttpString.BoundaryPrefix.length
                + boundary.length
                + HttpString.BoundaryPrefix.length;
    }

    public Request request() {
        return request;
    }

    public void request(Request request) {
        this.request = request;
    }

    public long responseTime() {
        return responseTime;
    }

    public void responseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public void setResponseTimeToNow() {
        responseTime = new Date().getTime();
    }

    public long response_delay() {
        if (request != null) {
            return responseTime - request.requestTime();
        }
        return -1;
    }

    public boolean hasCacheDirective(CacheDirectiveSort directiveSort) {
        HeaderValueCacheControl cacheControl = (HeaderValueCacheControl) getHeaderValueObj(HeaderSort.Cache_Control);
        if (cacheControl != null) {
            return cacheControl.hasCacheDirective(directiveSort);
        }
        return false;
    }

    @Override
    public Response clone() {
        Response cloned = new Response();
        ((EachRangePart) cloned).copyFrom((EachRangePart) this);

        cloned.majorVersion = majorVersion;
        cloned.minorVersion = minorVersion;
        cloned.statusCode = statusCode;
        cloned.reason = (reason != null) ? reason.clone() : null;

        cloned.boundary = (boundary != null) ? boundary.clone() : null;
        for (int index = 0; index < rangePartCount; index++) {
            if (rangeParts[index] != null) {
                cloned.addNewRangePart().copyFrom(rangeParts[index]);
            }
        }

        cloned.bodyBytes = (bodyBytes != null) ? bodyBytes.clone() : null;
        cloned.bodyFile = bodyFile;

        cloned.request = request;
        cloned.responseTime = responseTime;

        return cloned;
    }

    @Override
    public String toString() {
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        ResponseToBuffer.forStatusLine(buffer, this);
        ResponseToBuffer.forHeaders(buffer, this);
        buffer.flip();

        return new String(buffer.array(), 0, buffer.limit());
    }

}
