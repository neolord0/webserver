package kr.dogfoot.webserver.parser;

import kr.dogfoot.webserver.context.connection.http.parserstatus.ChunkParsingState;
import kr.dogfoot.webserver.context.connection.http.parserstatus.ParserStatus;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.CachedReader;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;

public class HttpChunkedBodyParser {
    public static void parseChunkSize(CachedReader r, ParserStatus ps) {
        switch (ps.chunkState()) {
            case ChunkSize:
                chunkSize(r, ps);
                break;
            case ChunkSizeCRLF:
                CRLF(r, ps);
                break;
        }
    }

    private static void chunkSize(CachedReader r, ParserStatus ps) {
        r.readAndCache();

        while (r.hasData() && r.peekIsCRLF() == false) {
            ps.buffer().into((byte) r.pollAndReadAndCache());
        }

        if (ps.buffer().length() > 0 && r.peekIsCRLF() == true) {
            ParseState ps2 = ParseState.pooledObject();
            ps2.ioff = 0;
            ps2.bufend = ps.buffer().length();

            int size = 0;
            try {
                size = ByteParser.parseInt(ps.buffer().data(), 16, ps2);
            } catch (ParserException e) {
                size = -1;
            }
            ps.chunkSize(size);
            ps.changeChunkState(ChunkParsingState.ChunkSizeCRLF);

            ParseState.release(ps2);
        }
    }

    protected static void CRLF(CachedReader r, ParserStatus ps) {
        r.readAndCache();

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
            switch (ps.chunkState()) {
                case ChunkSizeCRLF:
                    if (ps.chunkSize() == 0) {
                        r.rollbackByCache();
                        ps.changeChunkState(ChunkParsingState.ChunkEnd);
                    } else if (ps.chunkSize() > 0) {
                        r.rollbackByCache();
                        ps.changeChunkState(ChunkParsingState.ChunkData);
                    }
                    break;
                case ChunkDataCRLF:
                    ps.buffer().reset();
                    ps.changeChunkState(ChunkParsingState.ChunkSize);
                    break;
            }
        }
    }

    public static void parseChunkBodyCRLF(CachedReader r, ParserStatus ps) {
        CRLF(r, ps);
    }
}
