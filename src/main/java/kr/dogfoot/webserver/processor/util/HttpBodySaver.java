package kr.dogfoot.webserver.processor.util;

import kr.dogfoot.webserver.context.connection.http.client.HttpClientConnection;
import kr.dogfoot.webserver.context.connection.http.parserstatus.ChunkParsingState;
import kr.dogfoot.webserver.context.connection.http.parserstatus.ParserStatus;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.parser.HttpChunkedBodyParser;
import kr.dogfoot.webserver.parser.util.CachedReader;

import java.nio.ByteBuffer;

public class HttpBodySaver {
    public static void saveAsMuchContentLength(HttpClientConnection source, Request request) {
        ByteBuffer readBuffer = source.readBuffer();
        ParserStatus ps = source.parserStatus();
        do {
            int length = 0;
            boolean includeEnding = includeEnding(readBuffer, ps);
            if (includeEnding) {
                length = ps.remainingReadBodySize();
            } else {
                length = readBuffer.remaining();
            }

            if (length > 0) {
                request.appendBodyBytes(readBuffer.array(), readBuffer.position(), length);
                readBuffer.position(readBuffer.position() + length);
                ps.addReadBodySize(length);
            }
        } while (readBuffer.remaining() > 0 && ps.hasRemainingReadBodySize());
    }

    private static boolean includeEnding(ByteBuffer readBuffer, ParserStatus ps) {
        if (ps.contentLength() != -1) {
            return readBuffer.remaining() >= ps.remainingReadBodySize();
        }
        return false;
    }

    public static void saveUtilChunkEnd(HttpClientConnection source, Request request) {
        ByteBuffer readBuffer = source.readBuffer();
        ParserStatus ps = source.parserStatus();
        CachedReader reader = source.reader();

        while (readBuffer.hasRemaining() && ps.chunkState() != ChunkParsingState.ChunkEnd) {
            if (ps.isBeforeChunkData()) {
                HttpChunkedBodyParser.parseChunkSize(reader, ps);
            }

            if (ps.chunkState() == ChunkParsingState.ChunkData) {
                saveChunkData(readBuffer, request, ps);

                if (ps.remainingChunkSize() == 0) {
                    ps.changeChunkState(ChunkParsingState.ChunkDataCRLF);
                }
            }

            if (ps.chunkState() == ChunkParsingState.ChunkDataCRLF) {
                HttpChunkedBodyParser.parseChunkBodyCRLF(reader, ps);
            }
        }
    }

    private static void saveChunkData(ByteBuffer readBuffer, Request request, ParserStatus ps) {
        int length = Math.min(readBuffer.remaining(), ps.remainingChunkSize());
        if (length > 0) {
            request.appendBodyBytes(readBuffer.array(), readBuffer.position(), length);
            readBuffer.position(readBuffer.position() + length);

            ps.addPositionInChunk(length);
        }
    }
}
