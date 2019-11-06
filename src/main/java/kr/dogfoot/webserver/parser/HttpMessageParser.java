package kr.dogfoot.webserver.parser;

import kr.dogfoot.webserver.context.connection.http.HttpConnection;
import kr.dogfoot.webserver.context.connection.http.parserstatus.ParserStatus;
import kr.dogfoot.webserver.context.connection.http.parserstatus.ParsingState;
import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.CachedReader;

public class HttpMessageParser {
    protected static boolean skipSpace(HttpConnection connection) {
        ParserStatus ps = connection.parserStatus();
        if (ps.skippingSpace() == true) {
            CachedReader r = connection.reader();
            r.readAndCache();

            while (r.hasData() && r.peekIsSP() == true) {
                r.pollAndReadAndCache();
            }
            if (r.peekIsEnd() == false && r.peekIsSP() == false) {
                ps.skippingSpace(false);
            }
            return true;
        }
        return false;
    }

    protected static void firstCRLF(HttpConnection connection, ParsingState nextState) {
        CachedReader r = connection.reader();
        r.readAndCache();

        while (r.hasData() && r.peekIsCRLF()) {
            r.pollAndReadAndCache();
        }
        if (r.peekIsEnd() == false && r.peekIsCRLF() == false) {
            connection.parserStatus().changeState(nextState);
        }
    }

    protected static void CRLF(HttpConnection connection) {
        CachedReader r = connection.reader();
        r.readAndCache();

        ParserStatus ps = connection.parserStatus();
        if (r.peekIsCR()) {
            r.pollAndReadAndCache();
            ps.doneCR(true);
        }

        if (r.peekIsLF()) {
            r.pollAndReadAndCache();
            ps.doneCR(false);
            ps.doneCRLF(true);
        } else {
            if (ps.doneCR() == true && r.peekIsEnd() == false) {
                ps.doneCR(false);
                ps.doneCRLF(true);
            }
        }

        if (ps.doneCRLF() == true) {
            r.rollbackByCache();
            switch (ps.state()) {
                case CRLF:
                    ps.changeState(ParsingState.HeaderName);
                    break;
                case HeaderEnd:
                    ps.changeState(ParsingState.BodyStart);
                    break;
            }
        }
    }

    protected static void headerName(HttpConnection connection) {
        CachedReader r = connection.reader();
        ParserStatus ps = connection.parserStatus();
        r.readAndCache();

        if (ps.buffer().length() == 0) {
            if (r.peekIsCRLF()) {
                ps.changeState(ParsingState.HeaderEnd);
                return;
            }
            if (r.peekIsSPHT()) {
                ps.changeState(ParsingState.HeaderContinue);
                return;
            }
        }

        while (r.hasData() && r.peekIs(':') == false) {
            ps.buffer().into((byte) r.pollAndReadAndCache());
        }

        if (r.peekIs(':')) {
            r.poll();

            ps.parsingHeaderSort(HeaderSort.fromString(ps.buffer().newString()));
            ps.changeState(ParsingState.HeaderValue);
        }
    }

    protected static void skipWhiteSpace(HttpConnection connection) {
        CachedReader r = connection.reader();
        r.readAndCache();

        while (r.hasData() && r.peekIsSPHT() == true) {
            r.pollAndReadAndCache();
        }
        if (r.peekIsEnd() == false && r.peekIsSPHT() == false) {
            connection.parserStatus().changeState(ParsingState.HeaderValueContinue);
        }
    }
}
