package kr.dogfoot.webserver.parser;

import kr.dogfoot.webserver.context.connection.http.HttpConnection;
import kr.dogfoot.webserver.context.connection.http.parserstatus.ParserStatus;
import kr.dogfoot.webserver.context.connection.http.parserstatus.ParsingBuffer;
import kr.dogfoot.webserver.context.connection.http.parserstatus.ParsingState;
import kr.dogfoot.webserver.httpMessage.response.Response;
import kr.dogfoot.webserver.httpMessage.response.StatusCode;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.CachedReader;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.BytesUtil;
import kr.dogfoot.webserver.util.http.HttpString;

public class HttpResponseParser extends HttpMessageParser {
    public static void parse(HttpConnection connection) {
        if (skipSpace(connection)) {
            return;
        }
        switch (connection.parserStatus().state()) {
            case FirstCRLF:
                firstCRLF(connection, ParsingState.Version);
                break;
            case Version:
                version(connection);
                break;
            case StatusCode:
                statusCode(connection);
                break;
            case Reason:
                reason(connection);
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

    private static void version(HttpConnection connection) {
        CachedReader r = connection.reader();
        ParserStatus ps = connection.parserStatus();
        r.readAndCache();

        while (r.hasData() && r.peekIsSP() == false) {
            ps.buffer().into((byte) r.pollAndReadAndCache());
        }
        if (ps.buffer().length() > 0 && r.peekIsSP() == true) {
            setVersion(connection);
            ps.changeState(ParsingState.StatusCode);
        }
    }

    private static void setVersion(HttpConnection connection) {
        Response response = connection.context().response();
        ParsingBuffer buffer = connection.parserStatus().buffer();
        if (buffer.length() >= 8) {
            if (BytesUtil.compare(HttpString.Version_Prefix, 0, HttpString.Version_Prefix.length, buffer.data(), 0, HttpString.Version_Prefix.length) == 0) {
                ParseState ps = ParseState.pooledObject();
                ps.ioff = HttpString.Version_Prefix.length;
                ps.bufend = buffer.length();
                ps.separator = '.';
                try {
                    response.majorVersion((short) ByteParser.parseInt(buffer.data(), ps));
                    ps.prepare();
                    response.minorVersion((short) ByteParser.parseInt(buffer.data(), ps));
                } catch (Exception e) {
                    e.printStackTrace();
                    response.majorVersion((short) 0);
                    response.minorVersion((short) 9);
                }
                ParseState.release(ps);
            } else {
                response.majorVersion((short) 0);
                response.minorVersion((short) 9);
            }
        }
    }

    private static void statusCode(HttpConnection connection) {
        CachedReader r = connection.reader();
        ParserStatus ps = connection.parserStatus();
        r.readAndCache();

        while (r.hasData() && r.peekIsSP() == false) {
            ps.buffer().into((byte) r.pollAndReadAndCache());
        }
        if (ps.buffer().length() > 0 && r.peekIsSP()) {
            setStatusCode(connection.context().response(), ps.buffer());

            ps.changeState(ParsingState.Reason);
        }
    }

    private static void setStatusCode(Response response, ParsingBuffer buffer) {
        short code = -1;

        ParseState ps = ParseState.pooledObject();
        ps.ioff = 0;
        ps.bufend = buffer.length();
        try {
            code = (short) ByteParser.parseInt(buffer.data(), ps);
        } catch (ParserException e) {
            e.printStackTrace();
            code = -1;
        }
        ParseState.release(ps);

        if (code != -1) {
            response.code(StatusCode.fromCode(code));
        }
    }

    private static void reason(HttpConnection connection) {
        CachedReader r = connection.reader();
        ParserStatus ps = connection.parserStatus();
        r.readAndCache();

        while (r.hasData() && r.peekIsCRLF() == false) {
            ps.buffer().into((byte) r.pollAndReadAndCache());
        }
        if (r.peekIsCRLF() == true) {
            connection.context().response().reason(ps.buffer().newBytes());

            ps.changeState(ParsingState.CRLF);
        }
    }

    protected static void headerValue(HttpConnection connection) {
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

    protected static void appendHeader(HttpConnection connection) {
        byte[] value = connection.parserStatus().buffer().newBytes();
        connection.context().response().headerList()
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
            connection.context().response()
                    .headerList().continueBytes(connection.parserStatus().parsingHeaderSort(), ps.buffer());
            ps.changeState(ParsingState.CRLF);
        }
    }
}
