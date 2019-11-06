package kr.dogfoot.webserver.processor.util;

import kr.dogfoot.webserver.httpMessage.reply.Reply;
import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.util.message.http.ReplyToBuffer;

import java.nio.ByteBuffer;

public class ToClientCommon {
    public static void sendStatusLine_Headers(Context context, Reply reply, Server server) {
        ByteBuffer buffer = server.objects().bufferManager().pooledNormalBuffer();
        ReplyToBuffer.forStatusLine(buffer, reply);
        ReplyToBuffer.forHeaders(buffer, reply);
        buffer.flip();
        server.sendBufferToClient(context, buffer, true);
    }
}
