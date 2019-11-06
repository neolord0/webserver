package kr.dogfoot.webserver.parser;

import kr.dogfoot.webserver.context.connection.http.HttpConnection;
import kr.dogfoot.webserver.context.connection.http.parserstatus.ParserStatus;
import kr.dogfoot.webserver.context.connection.http.parserstatus.ParsingBuffer;
import kr.dogfoot.webserver.context.connection.http.parserstatus.ParsingState;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.CachedReader;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.httpMessage.request.MethodType;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.util.bytes.BytesUtil;
import kr.dogfoot.webserver.util.http.HttpString;

public class HttpRequestParser extends HttpMessageParser {
    public static void parse(HttpConnection connection) {
        if (skipSpace(connection)) {
            return;
        }
        switch (connection.parserStatus().state()) {
            case FirstCRLF:
                firstCRLF(connection, ParsingState.Method);
                break;
            case Method:
                method(connection);
                break;
            case RequestURI:
                requestURI(connection);
                break;
            case Version:
                version(connection);
                break;
            case HeaderName:
                headerName(connection);
                break;
            case HeaderValue:
                headerValue(connection);
                break;
            case HeaderContinue:
                skipWhiteSpace(connection);
                break;
            case HeaderValueContinue:
                continueValue(connection);
                break;
            case CRLF:
            case HeaderEnd:
                CRLF(connection);
                break;
        }
    }

    private static void method(HttpConnection connection) {
        CachedReader r = connection.reader();
        ParserStatus ps = connection.parserStatus();
        r.readAndCache();
        while (r.hasData() && r.peekIsSP() == false) {
            ps.buffer().into((byte) r.pollAndReadAndCache());
        }

        if (ps.buffer().length() > 0 && r.peekIsSP()) {
            MethodType mt = MethodType.fromBuffer(ps.buffer());
            connection.context().request().method(mt);

            ps.changeState(ParsingState.RequestURI);
        }
    }

    private static void requestURI(HttpConnection connection) {
        CachedReader r = connection.reader();
        ParserStatus ps = connection.parserStatus();
        r.readAndCache();

        while (r.hasData() && r.peekIsSP() == false) {
            ps.buffer().into((byte) r.pollAndReadAndCache());
        }

        if (ps.buffer().length() > 0 && r.peekIsSP()) {
            connection.context().request()
                    .requestURI().parse(connection.parserStatus().buffer().newString());

            connection.parserStatus().changeState(ParsingState.Version);
        }
    }

    private static void version(HttpConnection connection) {
        CachedReader r = connection.reader();
        ParserStatus ps = connection.parserStatus();
        r.readAndCache();

        while (r.hasData() && r.peekIsCRLF() == false) {
            ps.buffer().into((byte) r.pollAndReadAndCache());
        }
        if (r.peekIsCRLF() == true) {
            setVersion(connection);
            ps.changeState(ParsingState.CRLF);
        }
    }

    private static void setVersion(HttpConnection connection) {
        Request request = connection.context().request();
        ParsingBuffer buffer = connection.parserStatus().buffer();
        if (buffer.length() >= 8) {
            if (BytesUtil.compare(HttpString.Version_Prefix, 0, HttpString.Version_Prefix.length, buffer.data(), 0, HttpString.Version_Prefix.length) == 0) {
                ParseState ps = ParseState.pooledObject();
                ps.ioff = 5;   // 'http/'
                ps.bufend = buffer.length();
                ps.separator = '.';
                try {
                    request.majorVersion((short) ByteParser.parseInt(buffer.data(), ps));
                    ps.prepare();
                    request.minorVersion((short) ByteParser.parseInt(buffer.data(), ps));
                } catch (Exception e) {
                    request.majorVersion((short) 0);
                    request.minorVersion((short) 9);
                }
                ParseState.release(ps);
            } else {
                request.majorVersion((short) 0);
                request.minorVersion((short) 9);
            }
        }
    }

    private static void headerValue(HttpConnection connection) {
        CachedReader r = connection.reader();
        ParserStatus ps = connection.parserStatus();
        r.readAndCache();

        while (r.hasData() && r.peekIsCRLF() == false) {
            ps.buffer().into((byte) r.pollAndReadAndCache());
        }
        if (r.peekIsCRLF() == true) {
            appendHeader(connection);

            ps.changeState(ParsingState.CRLF);
        }
    }

    private static void appendHeader(HttpConnection connection) {
        byte[] value = connection.parserStatus().buffer().newBytes();
        connection.context().request().headerList()
                .addHeaderFromBytes(connection.parserStatus().parsingHeaderSort(), value);
    }

    private static void continueValue(HttpConnection connection) {
        CachedReader r = connection.reader();
        ParserStatus ps = connection.parserStatus();

        r.readAndCache();

        while (r.hasData() && r.peekIsCRLF() == false) {
            ps.buffer().into((byte) r.pollAndReadAndCache());
        }
        if (r.peekIsCRLF() == true) {
            connection.context().request()
                    .headerList().continueBytes(connection.parserStatus().parsingHeaderSort(), ps.buffer());
            ps.changeState(ParsingState.CRLF);
        }
    }
}
