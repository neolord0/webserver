package kr.dogfoot.webserver.httpMessage.request;

import kr.dogfoot.webserver.httpMessage.header.HeaderList;
import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValue;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueConnection;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueExpect;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;

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

    public boolean valid() {
        return method != MethodType.Unknown && requestURI != null;
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
        StringBuffer sb = new StringBuffer();
        sb.append(method).append(' ')
                .append(requestURI.toString()).append(' ')
                .append(majorVersion).append('.').append(minorVersion)
                .append("\r\n");

        sb.append(headerList);
        return sb.toString();
    }

    public boolean hasBody() {
        return headerList.hasBody();
    }

    public int contentLength() {
        return headerList.getContentLength();
    }

    public boolean hasContentLength() {
        return headerList.getContentLength() != -1;
    }

    public boolean isChunked() {
        return headerList.isChunked();
    }

    public boolean hasHeader(HeaderSort sort) {
        return headerList.has(sort);
    }

    public HeaderValue getHeaderValueObj(HeaderSort sort) {
        return headerList.getValueObj(sort);
    }

    public boolean isPersistentConnection() {
        HeaderValueConnection connection = (HeaderValueConnection) headerList.getValueObj(HeaderSort.Connection);
        if (majorVersion > 2 || majorVersion == 1 && minorVersion >= 1) {
            return connection == null || connection.isClose() == false;
        } else if (majorVersion == 1 && minorVersion == 0) {
            return connection != null && connection.isKeepAlive() == true;
        }
        return false;
    }

    public boolean hasExpect100Continue() {
        HeaderValueExpect expect = (HeaderValueExpect) headerList.getValueObj(HeaderSort.Expect);
        return expect != null && expect.is100Continue();
    }

    public boolean emptyBody() {
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
}
