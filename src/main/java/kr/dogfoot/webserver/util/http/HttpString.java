package kr.dogfoot.webserver.util.http;

import kr.dogfoot.webserver.util.bytes.OutputBuffer;

import java.util.Random;

public class HttpString {
    public static final byte[] Version_Prefix = "HTTP/".getBytes();

    public static final byte[] CRLF = "\r\n".getBytes();

    public static final byte Separator_Subtract = '-';

    public static final byte Separator_Divide = '/';

    public static final byte[] Separator_Comma = ", ".getBytes();

    public static final byte Question = '?';

    public static final byte Dot = '.';

    public static final byte Space = ' ';

    public static final byte DQuote = '"';

    public static final byte Negative = '-';
    public static final byte Minus = '-';
    public static final String Minus_String = "-";

    public static final byte[] BoundaryPrefix = "--".getBytes();

    public static final byte[] HeaderSeparator = ": ".getBytes();

    public static final String PathSeparator = "/";

    public static final String Text_Type = "text";
    public static final byte[] Text_Plain = "text/plain".getBytes();
    public static final byte[] Text_Html = "text/html".getBytes();
    public static final String Keep_Alive_String = "Keep-Alive";
    public static final byte[] Keep_Alive = Keep_Alive_String.getBytes();
    public static final String Close_String = "Close";
    public static final byte[] Close = Close_String.getBytes();
    public static final byte[] Http_1_1 = "HTTP/1.1".getBytes();
    public static final byte Zero = '0';
    public static final String Timeout_String = "timeout";
    public static final byte[] Timeout = Timeout_String.getBytes();
    public static final String Max_String = "max";
    public static final byte[] Max = Max_String.getBytes();
    public static final byte ReceivePacketHeaderA = 'A';
    public static final byte ReceivePacketHeaderB = 'B';
    public static final byte Equal = '=';
    public static final byte Comma = ',';
    public static final byte Colon = ':';
    public static final byte Semicolon = ';';
    public static final String Slash_String = "/";
    public static final byte Slash = '/';
    public static final byte[] Bytes = "bytes".getBytes();
    public static final String Asterisk_String = "*";
    public static final byte Asterisk = '*';
    public static final String Ajp_1_3 = "ajp/1.3";
    public static final String Http = "http";
    public static final String Version_1_1 = "1.1";
    public static final String Https = "https";
    public static final String HttpWithPostFix = "http://";
    public static final String HttpsWithPostFix = "https://";
    public static final byte[] EndChunk = "0\r\n\r\n".getBytes();
    public static final String Q = "q";
    public static final String none = "none";
    public static final String Continue100 = "100-continue";
    public static final byte[] Response100Continue = "HTTP/1.1 100 Continue\r\n\n".getBytes();
    public static final byte[] Realm = "realm".getBytes();
    public static final String Basic_Auth = "Basic";
    public static final String No_Cache_String = "no_cache";
    public static final byte[] No_Cache = No_Cache_String.getBytes();
    public static final String Charset_String = "charset";
    public static final byte[] Charset = "charset".getBytes();
    public static final byte[] WeakValidator_Prefix = "W/".getBytes();
    private static final byte[] Multipart_Boundar_Chars =
            "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
                    .getBytes();
    private static final byte[] MultiPart_Byteranges_Prefix = "multipart/byteranges; boundary=".getBytes();

    public static byte[] newBoundary() {
        Random rand = new Random();
        int count = rand.nextInt(11) + 30; // a random size from 30 to 40
        byte[] boundary = new byte[count];
        for (int i = 0; i < count; i++) {
            boundary[i] = Multipart_Boundar_Chars[rand.nextInt(Multipart_Boundar_Chars.length)];
        }
        return boundary;
    }

    public static byte[] mulitPart_Byteranges(byte[] boundary) {
        byte[] ret = new byte[MultiPart_Byteranges_Prefix.length + boundary.length + 2];
        System.arraycopy(MultiPart_Byteranges_Prefix, 0, ret, 0, MultiPart_Byteranges_Prefix.length);
        ret[MultiPart_Byteranges_Prefix.length] = '"';
        System.arraycopy(boundary, 0, ret, MultiPart_Byteranges_Prefix.length + 1, boundary.length);
        ret[ret.length - 1] = '"';
        return ret;
    }


    public static byte[] keepAliveValue(int timeout, int max) {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer.append(Timeout)
                .append(Equal)
                .appendInt(timeout)
                .append(Separator_Comma)
                .append(Max)
                .append(Equal)
                .appendInt(max);
        return buffer.getBytesAndRelease();
    }
}
