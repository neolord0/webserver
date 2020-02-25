package kr.dogfoot.webserver.processor.util;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.context.connection.ajp.AjpProxyConnection;
import kr.dogfoot.webserver.context.connection.http.client.HttpClientConnection;
import kr.dogfoot.webserver.context.connection.http.parserstatus.ChunkParsingState;
import kr.dogfoot.webserver.context.connection.http.parserstatus.ParserStatus;
import kr.dogfoot.webserver.parser.HttpChunkedBodyParser;
import kr.dogfoot.webserver.parser.util.CachedReader;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.util.Message;
import kr.dogfoot.webserver.util.message.ajp.AjpMessageMaker;

import java.nio.ByteBuffer;

public class ToAjpServer {
    public static void sendForwardRequest(Context context, Server server) {
        ByteBuffer buffer = server.objects().bufferManager().pooledBufferForAjpPacket();
        AjpMessageMaker.forForwardRequest(buffer, context.request(), context.clientConnection().remoteAddress(), context.clientConnection().isAdjustSSL());
        buffer.flip();

        server.bufferSender().sendBufferToAjpServer(context, buffer, true);
    }

    public static void sendEmptyBodyChunk(Context context, Server server) {
        ByteBuffer buffer = server.objects().bufferManager().pooledVarySmallBuffer();
        AjpMessageMaker.forEmptyBodyChunk(buffer);
        buffer.flip();

        server.bufferSender().sendBufferToAjpServer(context, buffer, true);
    }

    public static boolean sendBodyChunkAsMuchContentLength(Context context, Server server) {
        HttpClientConnection clientConn = context.clientConnection();
        ByteBuffer readBuffer = clientConn.readBuffer();

        boolean willSend;
        int length = 0;
        if (includeEnding(context)) {
            length = Math.min(AjpProxyConnection.MaxRequestBodyChunkSize, clientConn.parserStatus().remainingReadBodySize());
            willSend = true;
        } else if (readBuffer.remaining() >= AjpProxyConnection.MaxRequestBodyChunkSize) {
            length = AjpProxyConnection.MaxRequestBodyChunkSize;
            willSend = true;
        } else {
            willSend = false;
        }

        if (willSend) {
            Message.debug(context.ajpProxy(), "Send Request Body Chunk");

            ByteBuffer buffer = server.objects().bufferManager().pooledBufferForAjpPacket();
            AjpMessageMaker.forRequestBodyChunk(buffer, readBuffer, length);
            buffer.flip();

            server.bufferSender().sendBufferToAjpServer(context, buffer, true);
            clientConn.parserStatus().addReadBodySize(length);
        }
        return willSend;
    }

    private static boolean includeEnding(Context context) {
        if (context.request().contentLength() != -1) {
            return context.clientConnection().readBuffer().remaining() >= context.clientConnection().parserStatus().remainingReadBodySize();
        }
        return false;
    }

    public static boolean sendBodyChunkUntilChunkEnd(Context context, Server server) {
        HttpClientConnection clientConn = context.clientConnection();
        ByteBuffer readBuffer = clientConn.readBuffer();
        CachedReader reader = clientConn.reader();
        ParserStatus ps = clientConn.parserStatus();

        AjpProxyConnection ajpProxyConn = context.ajpProxy();
        if (ajpProxyConn.bodyBuffer() == null) {
            ByteBuffer buffer = server.objects().bufferManager().pooledBufferForAjpPacket();
            buffer.position(AjpProxyConnection.RequestBodyChunk_HeaderSize);

            ajpProxyConn.bodyBuffer(buffer);
            ajpProxyConn.bodyChunkSize(0);
        }
        ByteBuffer writeBuffer = ajpProxyConn.bodyBuffer();

        boolean willSend = false;

        while (readBuffer.hasRemaining() && willSend == false) {
            if (ps.isBeforeChunkData()) {
                HttpChunkedBodyParser.parseChunkSize(reader, ps);
            }

            if (ps.chunkState() == ChunkParsingState.ChunkData) {
                int length = Math.min(readBuffer.remaining(), ps.remainingChunkSize());
                length = Math.min(length, writeBuffer.remaining());
                writeBuffer.put(readBuffer.array(), readBuffer.position(), length);
                readBuffer.position(readBuffer.position() + length);
                ajpProxyConn.addBodyChunkSize(length);

                ps.addPositionInChunk(length);

                if (ps.remainingChunkSize() == 0) {
                    ps.changeChunkState(ChunkParsingState.ChunkDataCRLF);
                }

                if (writeBuffer.remaining() == 0) {
                    willSend = true;
                }
            }

            if (ps.chunkState() == ChunkParsingState.ChunkDataCRLF) {
                HttpChunkedBodyParser.parseChunkBodyCRLF(reader, ps);
            }

            if (ps.chunkState() == ChunkParsingState.ChunkEnd) {
                willSend = true;
            }
        }

        if (willSend) {
            Message.debug(ajpProxyConn, "Send Request Body Chunk");

            AjpMessageMaker.forRequestBodyChunk(writeBuffer, ajpProxyConn.bodyChunkSize());
            writeBuffer.flip();

            server.bufferSender().sendBufferToAjpServer(context, writeBuffer, true);

            ajpProxyConn.bodyBuffer(null);
        }

        return willSend;
    }
}
