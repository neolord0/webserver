package kr.dogfoot.webserver.httpMessage.response;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValue;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.server.resource.performer.util.ContentRange;
import kr.dogfoot.webserver.util.http.HttpString;

import java.io.File;
import java.util.Date;

public class Response extends EachRangePart {
    private static final int Default_RangePart_Count = 3;
    public boolean isPartial;
    private short majorVersion;
    private short minorVersion;
    private StatusCode code;
    private byte[] reason;
    private byte[] bodyBytes;
    private File bodyFile;
    private byte[] boundary;
    private EachRangePart[] rangeParts;
    private int rangePartCount;

    private Request request;
    private long responseTime;

    public Response() {
        super();

        majorVersion = 1;
        minorVersion = 1;
        code = null;
        reason = null;

        bodyBytes = null;
        bodyFile = null;

        isPartial = false;
        boundary = null;
        rangeParts = new EachRangePart[Default_RangePart_Count];
        rangePartCount = 0;

        request = null;
        responseTime = 0;
    }

    public Response setHeader(HeaderSort sort, byte[] value) {
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

    public Response addHeader(HeaderSort sort, byte[] value) {
        super.addHeader(sort, value);
        return this;
    }

    public Response changeHeader(HeaderSort sort, byte[] value) {
        super.changeHeader(sort, value);
        return this;
    }

    public HeaderValue getHeaderValueObj(HeaderSort sort) {
        return headerList.getValueObj(sort);
    }

    public Response removeHeader(HeaderSort sort) {
        super.removeHeader(sort);
        return this;
    }

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

    public StatusCode code() {
        return code;
    }

    public Response code(StatusCode code) {
        this.code = code;
        return this;
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
        return isPartial;
    }

    public void isPartial(boolean isPartial) {
        this.isPartial = isPartial;
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
        return headerList.getContentLength() != -1;
    }

    public int contentLength() {
        return headerList.getContentLength();
    }

    public boolean hasKeepAlive() {
        return headerList.has(HeaderSort.Keep_Alive);
    }

    @Override
    public long calculateContextLength() {
        if (isMultiPart() == false) {
            return super.calculateContextLength();
        } else {
            long size = 0;
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

    public void setResponseTimeToNow() {
        responseTime = new Date().getTime();
    }

    public long response_delay() {
        if (request != null) {
            return responseTime - request.requestTime();
        }
        return -1;
    }
}
