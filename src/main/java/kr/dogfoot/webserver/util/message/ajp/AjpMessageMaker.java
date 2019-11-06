package kr.dogfoot.webserver.util.message.ajp;

import kr.dogfoot.webserver.httpMessage.header.HeaderItem;
import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueHost;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.util.http.HttpString;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class AjpMessageMaker {
    public static final byte[] PreFix = {0x12, 0x34};

    public static void forForwardRequest(ByteBuffer buffer, Request request, InetSocketAddress remoteAddress, boolean ssl) {
        fillPreFix(buffer);                                     // packet_prefix
        fillByte(buffer, AjpPacketType.ForwardRequest.code());  // prefix_code
        fillByte(buffer, request.method().ajpCode());           // method

        fillString(buffer, HttpString.Http_1_1);        // protocol
        fillString(buffer, request.requestURI().toString(false));    // req_uri

        String remoteAddr = null;
        if (remoteAddress != null) {
            remoteAddr = remoteAddress.toString();
        }
        fillString(buffer, remoteAddr);                         // remote_addr
        fillString(buffer, (byte[]) null);                      // remote_host

        HeaderValueHost host = (HeaderValueHost) request.getHeaderValueObj(HeaderSort.Host);
        if (host != null) {
            fillString(buffer, host.ipOrDomain());                    // server_name
            fillInteger(buffer, (short) host.port());           // server_port
        } else {
            fillString(buffer, (byte[]) null);                  // server_name
            fillInteger(buffer, (short) 0);                     // server_port
        }
        fillBoolean(buffer, ssl);                        // is_ssl

        HeaderItem[] headers = request.headerList().getHeaderByteArray();
        fillInteger(buffer, (short) headers.length);            // num_headers
        for (HeaderItem item : headers) {
            fillHeader(buffer, item);
        }

        fillAttributes(buffer, request);
        fillByte(buffer, AjpAttribute.Are_Done.code());

        fillPacketSize(buffer);                                 // packet_size
    }

    private static void fillAttributes(ByteBuffer buffer, Request request) {
        if (request.requestURI().queryString() != null && request.requestURI().queryString().length() > 0) {
            fillByte(buffer, AjpAttribute.Query_String.code());
            fillString(buffer, request.requestURI().queryString());
        }
    }

    private static void fillHeader(ByteBuffer buffer, HeaderItem item) {
        if (item.sort().getAjpSendCode() != 0) {              // header_name
            fillInteger(buffer, item.sort().getAjpSendCode());
        } else {
            fillString(buffer, item.sort().toString());
        }
        fillString(buffer, item.valueBytes());                           // header_value
    }

    private static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder();
        for (final byte b : a) {
            sb.append(String.format("%02x ", b & 0xff));
        }
        return sb.toString();
    }

    private static void fillPreFix(ByteBuffer buffer) {
        buffer.put(PreFix);
        buffer.position(4); // jump 2 bytes for packet size
    }

    private static void fillByte(ByteBuffer buffer, byte b) {
        buffer.put(b);
    }

    private static void fillString(ByteBuffer buffer, byte[] bytes) {
        if (bytes == null) {
            buffer.putShort((short) 0xffff);
        } else {
            buffer
                    .putShort((short) bytes.length)
                    .put(bytes)
                    .put((byte) 0);
        }
    }

    private static void fillString(ByteBuffer buffer, String str) {
        if (str == null) {
            buffer.putShort((short) 0xffff);
        } else {
            buffer
                    .putShort((short) str.length())
                    .put(str.getBytes())
                    .put((byte) 0);
        }
    }

    private static void fillInteger(ByteBuffer buffer, short value) {
        buffer.putShort(value);
    }

    private static void fillBoolean(ByteBuffer buffer, boolean value) {
        buffer.put(value ? (byte) 1 : (byte) 0);
    }

    private static void fillPacketSize(ByteBuffer buffer) {
        int pos = buffer.position();
        short size = (short) (pos - 4);
        buffer.position(2);
        fillInteger(buffer, size);
        buffer.position(pos);
    }

    public static void forRequestBodyChunk(ByteBuffer buffer, int chunkSize) {
        int oldPos = buffer.position();
        buffer.position(0);
        buffer.put(PreFix);
        buffer.putShort((short) (chunkSize + 2));
        buffer.putShort((short) chunkSize);
        buffer.position(oldPos);
    }

    public static void forRequestBodyChunk(ByteBuffer buffer, ByteBuffer readBuffer, int chunkSize) {
        buffer.put(PreFix);
        buffer.putShort((short) (chunkSize + 2));
        buffer.putShort((short) chunkSize);
        buffer.put(readBuffer.array(), readBuffer.position(), chunkSize);
        readBuffer.position(readBuffer.position() + chunkSize);
    }

    public static void forEmptyBodyChunk(ByteBuffer buffer) {
        buffer.put(PreFix);
        buffer.putShort((short) 0);
    }
}
