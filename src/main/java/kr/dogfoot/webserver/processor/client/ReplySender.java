package kr.dogfoot.webserver.processor.client;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.context.ContextState;
import kr.dogfoot.webserver.context.connection.http.senderstatus.SenderStatus;
import kr.dogfoot.webserver.context.connection.http.senderstatus.SendingState;
import kr.dogfoot.webserver.httpMessage.reply.Reply;
import kr.dogfoot.webserver.processor.GeneralProcessor;
import kr.dogfoot.webserver.processor.util.ToClientCommon;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.server.resource.performer.util.ContentRange;
import kr.dogfoot.webserver.util.Message;
import kr.dogfoot.webserver.util.http.HttpString;
import kr.dogfoot.webserver.util.message.http.ReplyToBuffer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ReplySender extends GeneralProcessor {
    private static final String Error_TooManyFielsOpen = "(Too many open files)";

    public ReplySender(Server server) {
        super(server);
    }

    public void start() throws Exception {
        Message.debug("start Reply Sender ...");

        super.start();
    }

    @Override
    protected void onNewContext(Context context) {
        context.changeState(ContextState.SendingReply);
        sendReply(context);
    }

    private void sendReply(Context context) {
        if (context.clientConnection().senderStatus().stateIsBeforeBody()) {
            ToClientCommon.sendStatusLine_Headers(context, context.reply(), server);
        }

        sendBodyBlock(context);
    }

    private void sendBodyBlock(Context context) {
        SenderStatus ss = context.clientConnection().senderStatus();

        if (context.reply().isBodyFile() && ss.openedResourceFile() == false) {
            openResourceFile(context);
        }

        if (context.reply().isBodyFile()) {
            sendBodyBlockFile(context);
        } else if (context.reply().bodyBytes() != null) {
            sendBodyBytes(context);
        } else {
            onEndRequest(context);
        }
    }

    private void openResourceFile(Context context) {
        try {
            FileInputStream is = new FileInputStream(context.reply().bodyFile());
            context.clientConnection().senderStatus().resourceFileCh(is.getChannel());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            context.clientConnection().senderStatus().resourceFileCh(null);
            if (e.getMessage().endsWith(Error_TooManyFielsOpen)) {
                context.reply(replyMaker().get_500TooManyFileOpen());
            } else {
                context.reply(replyMaker().new_404NotFound(context.request()));
            }
        }
    }

    private void sendBodyBytes(Context context) {
        if (context.reply().isPartial() == false || context.reply().rangePartCount() < 2) {
            sendBodyBytesRange(context, context.reply().bodyBytes(), context.reply().range());
            sendBodyCRLF(context);
        } else {
            SenderStatus ss = context.clientConnection().senderStatus();
            ss.rangeCount(context.reply().rangePartCount());

            for (int index = 0; index < context.reply().rangePartCount(); index++) {
                ss.range(context.reply().rangePart(index).range());
                sendBoundaryAndPartHeader(context);
                sendBodyBytesRange(context, context.reply().bodyBytes(), context.reply().rangePart(index).range());
            }

            sendEndBoundary(context);
        }

        onEndRequest(context);
    }

    private void sendBodyBytesRange(Context context, byte[] bodyBytes, ContentRange range) {
        ByteBuffer temp;
        if (range != null) {
            temp = ByteBuffer.wrap(bodyBytes, (int) range.firstPos(), ((int) range.lastPos() - (int) range.firstPos() + 1));
        } else {
            temp = ByteBuffer.wrap(bodyBytes);
        }
        server.sendBufferToClient(context, temp, false);
    }

    private void sendBodyCRLF(Context context) {
        ByteBuffer temp = ByteBuffer.wrap(HttpString.CRLF);
        server.sendBufferToClient(context, temp, false);
    }

    private void onEndRequest(Context context) {
        if (context.reply().code().isError()) {
            server.sendCloseSignalForClient(context);
        } else {
            if (context.reply().hasKeepAlive() == true) {
                Message.debug(context, "Persistent Connection");
            }

            context.resetForNextRequest();
            server.gotoRequestReceiver(context);
        }
    }

    private void sendBodyBlockFile(Context context) {
        SenderStatus ss = context.clientConnection().senderStatus();
        ss.changeState(SendingState.Body);

        if (ss.isStartRange()) {
            setRange(ss, context.reply());

            if (ss.isMultipart()) {
                sendBoundaryAndPartHeader(context);
            }
        }

        sendResourceFileBlock(context);
    }

    private void setRange(SenderStatus ss, Reply reply) {
        ss.rangeCount(reply.rangePartCount());

        if (ss.isMultipart()) {
            ss.range(reply.rangePart(ss.rangeIndex()).range());
        } else {
            ss.range(reply.range());
        }
    }

    private void sendBoundaryAndPartHeader(Context context) {
        ByteBuffer buffer = bufferManager().pooledSmallBuffer();
        ReplyToBuffer.forPartBoundary(buffer, context.reply());
        ReplyToBuffer.forPartHeader(buffer, context.clientConnection().senderStatus(), context.reply());
        buffer.flip();

        server.sendBufferToClient(context, buffer, true);
    }

    private void sendResourceFileBlock(Context context) {
        readResourceFile(context, new MyCompletionHandler<Integer, Context, ByteBuffer>() {
            @Override
            public void completed(Integer result, Context context, ByteBuffer buffer) {
                buffer.flip();

                server.sendBufferToClient(context, buffer, true);

                SenderStatus ss = context.clientConnection().senderStatus();
                ss.addSentSizeInRange(result);

                if (ss.isEndRange()) {
                    onEndRange(context);

                    if (ss.stateIsEndBody()) {
                        onEndBody(context, ss);
                    } else {
                        prepareContext(context);
                    }
                } else {
                    prepareContext(context);
                }
            }

            @Override
            public void failed(Throwable exc, Context context) {

            }
        });
    }

    private void readResourceFile(final Context context, final MyCompletionHandler<Integer, Context, ByteBuffer> handler) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    SenderStatus ss = context.clientConnection().senderStatus();
                    ByteBuffer buffer = bufferManager().pooledLargeBuffer();
                    if (buffer.capacity() > ss.remainingSendSize()) {
                        buffer.limit(ss.remainingSendSize());
                    }
                    int read = ss.resourceFileCh().read(buffer, ss.readingPosition());

                    handler.completed(read, context, buffer);
                } catch (IOException e) {
                    handler.failed(e, context);
                }

            }
        };
        server.objects().ioExecutorService().execute(r);
    }

    private void onEndRange(Context context) {
        SenderStatus ss = context.clientConnection().senderStatus();
        if (ss.isMultipart()) {
            if (ss.isLastRange() == false) {
                ss.nextRange();
            } else {
                ss.changeState(SendingState.BodyEnd);
            }
        } else {
            ss.changeState(SendingState.BodyEnd);
        }
    }


    private void onEndBody(Context context, SenderStatus ss) {
        if (ss.isMultipart()) {
            sendEndBoundary(context);
        }

        closeResourceFile(context);

        onEndRequest(context);
    }

    private void sendEndBoundary(Context context) {
        ByteBuffer buffer = bufferManager().pooledVarySmallBuffer();
        ReplyToBuffer.forEndBoundary(buffer, context.reply());
        buffer.flip();

        server.sendBufferToClient(context, buffer, true);
    }

    private void closeResourceFile(Context context) {
        try {
            context.clientConnection().senderStatus().resourceFileCh().close();
        } catch (IOException e) {
        }
    }

    @Override
    public void terminate() throws Exception {
        super.terminate();

        Message.debug("terminate Reply Sender ...");
    }

    private interface MyCompletionHandler<V, A, A2> {
        void completed(V result, A attachment, A2 attachment2);

        void failed(Throwable exc, A attachment);
    }
}
