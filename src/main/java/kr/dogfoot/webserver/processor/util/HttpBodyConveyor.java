package kr.dogfoot.webserver.processor.util;

import kr.dogfoot.webserver.context.connection.http.HttpConnection;
import kr.dogfoot.webserver.context.connection.http.parserstatus.ChunkParsingState;
import kr.dogfoot.webserver.context.connection.http.parserstatus.ParserStatus;
import kr.dogfoot.webserver.context.connection.http.senderstatus.ChunkedBodySendState;
import kr.dogfoot.webserver.context.connection.http.senderstatus.SenderStatus;
import kr.dogfoot.webserver.parser.HttpChunkedBodyParser;
import kr.dogfoot.webserver.parser.util.CachedReader;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.util.bytes.ToBytes;
import kr.dogfoot.webserver.util.http.HttpString;

import java.nio.ByteBuffer;

public class HttpBodyConveyor {
    public static void conveyAsMuchContentLength(HttpConnection source, HttpConnection target, Server server) {
        ByteBuffer readBuffer = source.readBuffer();
        ParserStatus ps = source.parserStatus();
        do {
            if (target.bodyBuffer() == null) {
                target.bodyBuffer(server.objects().bufferManager().pooledNormalBuffer());
            }
            ByteBuffer writeBuffer = target.bodyBuffer();

            int length = 0;
            boolean includeEnding = includeEnding(readBuffer, ps);
            if (includeEnding) {
                length = Math.min(writeBuffer.remaining(), ps.remainingReadBodySize());
            } else {
                length = Math.min(writeBuffer.remaining(), readBuffer.remaining());
            }

            if (length > 0) {
                writeBuffer.put(readBuffer.array(), readBuffer.position(), length);
                readBuffer.position(readBuffer.position() + length);
                ps.addReadBodySize(length);

                if (writeBuffer.hasRemaining() == false || ps.hasRemainingReadBodySize() == false) {
                    target.bodyBuffer(null);
                    writeBuffer.flip();

                    if (target.isClientConnection()) {
                        server.bufferSender().sendBufferToClient(target.context(), writeBuffer, true);
                    } else if (target.isHttpProxyConnection()) {
                        server.bufferSender().sendBufferToHttpServer(target.context(), writeBuffer, true);
                    }
                }
            }
        } while (readBuffer.remaining() > 0 && ps.hasRemainingReadBodySize());
    }

    private static boolean includeEnding(ByteBuffer readBuffer, ParserStatus ps) {
        if (ps.contentLength() != -1) {
            return readBuffer.remaining() >= ps.remainingReadBodySize();
        }
        return false;
    }

    public static void conveyUtilChunkEnd(HttpConnection source, HttpConnection target, Server server) {
        ByteBuffer readBuffer = source.readBuffer();
        ParserStatus ps = source.parserStatus();
        CachedReader reader = source.reader();

        if (target.bodyBuffer() == null) {
            target.bodyBuffer(server.objects().bufferManager().pooledNormalBuffer());
        }
        SenderStatus ss = target.senderStatus();
        while (readBuffer.hasRemaining() && ps.chunkState() != ChunkParsingState.ChunkEnd) {
            if (ps.isBeforeChunkData()) {
                HttpChunkedBodyParser.parseChunkSize(reader, ps);
            }

            if (ps.chunkState() == ChunkParsingState.ChunkData) {
                if (ss.chunkedBodySendState() == ChunkedBodySendState.ChunkSize) {
                    writeChunkSize(target, server, ps.chunkSize());
                    ss.changeChunkedBodySendState(ChunkedBodySendState.ChunkBody);
                }
                writeChunkData(target, ps, server, readBuffer);

                if (ps.remainingChunkSize() == 0) {
                    ps.changeChunkState(ChunkParsingState.ChunkDataCRLF);
                    ss.changeChunkedBodySendState(ChunkedBodySendState.ChunkDataCRLF);
                }
            }

            if (ps.chunkState() == ChunkParsingState.ChunkDataCRLF) {
                HttpChunkedBodyParser.parseChunkBodyCRLF(reader, ps);

                if (ss.chunkedBodySendState() == ChunkedBodySendState.ChunkDataCRLF) {
                    writeCRLF(target, server);
                    ss.changeChunkedBodySendState(ChunkedBodySendState.ChunkSize);
                }
            }

            if (ps.chunkState() == ChunkParsingState.ChunkEnd) {
                writeEndChuckEnd(target, server);
            }
        }
    }

    private static void writeChunkSize(HttpConnection target, Server server, int chunkSize) {
        ByteBuffer writeBuffer = target.bodyBuffer();
        if (writeBuffer.remaining() >= sizeForChunkSize(chunkSize)) {
            writeBuffer
                    .put(Integer.toHexString(chunkSize).getBytes())
                    .put(HttpString.CRLF);
        } else {
            sendBuffer(target, server, writeBuffer);

            target.bodyBuffer(server.objects().bufferManager().pooledNormalBuffer());
            writeChunkSize(target, server, chunkSize);
        }
    }

    private static int sizeForChunkSize(int chunkSize) {
        return ToBytes.fromInt(chunkSize).length + HttpString.CRLF.length;
    }

    private static void sendBuffer(HttpConnection target, Server server, ByteBuffer writeBuffer) {
        writeBuffer.flip();

        if (target.isClientConnection()) {
            server.bufferSender().sendBufferToClient(target.context(), writeBuffer, true);
        } else if (target.isHttpProxyConnection()) {
            server.bufferSender().sendBufferToHttpServer(target.context(), writeBuffer, true);
        }
    }

    private static void writeChunkData(HttpConnection target, ParserStatus ps, Server server, ByteBuffer readBuffer) {
        ByteBuffer writeBuffer = target.bodyBuffer();

        if (writeBuffer.remaining() > 0) {
            int length = Math.min(readBuffer.remaining(), ps.remainingChunkSize());
            length = Math.min(length, writeBuffer.remaining());
            if (length > 0) {
                writeBuffer.put(readBuffer.array(), readBuffer.position(), length);
                readBuffer.position(readBuffer.position() + length);
                ps.addPositionInChunk(length);
            }
        } else {
            sendBuffer(target, server, writeBuffer);

            target.bodyBuffer(server.objects().bufferManager().pooledNormalBuffer());
            writeChunkData(target, ps, server, readBuffer);
        }
    }

    private static void writeCRLF(HttpConnection target, Server server) {
        ByteBuffer writeBuffer = target.bodyBuffer();

        if (writeBuffer.remaining() >= HttpString.CRLF.length) {
            writeBuffer.put(HttpString.CRLF);
        } else {
            sendBuffer(target, server, writeBuffer);
            target.bodyBuffer(server.objects().bufferManager().pooledNormalBuffer());
            writeCRLF(target, server);
        }
    }

    private static void writeEndChuckEnd(HttpConnection target, Server server) {
        ByteBuffer writeBuffer = target.bodyBuffer();

        if (writeBuffer.remaining() >= HttpString.EndChunk.length) {
            writeBuffer.put(HttpString.EndChunk);

            sendBuffer(target, server, writeBuffer);
            target.bodyBuffer(null);
        } else {
            sendBuffer(target, server, writeBuffer);
            target.bodyBuffer(server.objects().bufferManager().pooledVarySmallBuffer());

            writeEndChuckEnd(target, server);
        }
    }
}
