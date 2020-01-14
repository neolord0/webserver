package kr.dogfoot.webserver.processor.util;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.util.message.http.ResponseToBuffer;

import java.nio.ByteBuffer;

public class ToClientCommon {
    public static void sendStatusLine_Headers(Context context, Server server) {
        ByteBuffer buffer = server.objects().bufferManager().pooledNormalBuffer();
        ResponseToBuffer.forStatusLine(buffer, context.response());
        ResponseToBuffer.forHeaders(buffer, context.response());
        buffer.flip();
        server.bufferSender().sendBufferToClient(context, buffer, true);
    }
}
