package kr.dogfoot.webserver.processor.util;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.util.http.HttpString;

import java.nio.ByteBuffer;

public class ToClient {
    public static void sendBodyChunkSize(Context context, short chunkSize, Server server) {
        ByteBuffer buffer = server.objects().bufferManager().pooledVarySmallBuffer();
        buffer
                .put(Integer.toHexString(chunkSize).getBytes())
                .put(HttpString.CRLF);
        buffer.flip();

        server.bufferSender().sendBufferToClient(context, buffer, true);
    }

    public static void sendBodyChunk(Context context, short chunkSize, Server server) {
        ByteBuffer buffer = server.objects().bufferManager().pooledBufferForAjpPacket();
        ByteBuffer readBuffer = context.ajpProxy().receiveBuffer();
        buffer.put(readBuffer.array(), readBuffer.position(), chunkSize);
        readBuffer.position(readBuffer.position() + chunkSize + 1);
        buffer.put(HttpString.CRLF);
        buffer.flip();

        server.bufferSender().sendBufferToClient(context, buffer, true);
    }

    public static void sendLastBodyChunk(Context context, Server server) {
        ByteBuffer buffer = server.objects().bufferManager().pooledVarySmallBuffer();
        buffer.put(HttpString.EndChunk);
        buffer.flip();

        server.bufferSender().sendBufferToClient(context, buffer, true);
    }
}
