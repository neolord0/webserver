package kr.dogfoot.webserver.processor.client;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.context.ContextState;
import kr.dogfoot.webserver.context.connection.http.senderstatus.SenderStatus;
import kr.dogfoot.webserver.context.connection.http.senderstatus.SendingState;
import kr.dogfoot.webserver.httpMessage.response.Response;
import kr.dogfoot.webserver.processor.GeneralProcessor;
import kr.dogfoot.webserver.processor.util.ToClientCommon;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.server.resource.performer.util.ContentRange;
import kr.dogfoot.webserver.util.Message;
import kr.dogfoot.webserver.util.http.HttpString;
import kr.dogfoot.webserver.util.message.http.ResponseToBuffer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ResponseSender extends GeneralProcessor {
    private static final String Error_TooManyFielsOpen = "(Too many open files)";
    private static int ResponseSenderID = 0;

    public ResponseSender(Server server) {
        super(server, ResponseSenderID++);
    }

    @Override
    protected void onNewContext(Context context) {
        server.objects().executorForResponseSending()
                .execute(() -> {
                    context.changeState(ContextState.SendingResponse);
                    sendResponse(context);
                });
    }

    private void sendResponse(Context context) {
        if (context.clientConnection().senderStatus().stateIsBeforeBody()) {
            Message.debug(context, "Send response");

            // test
            System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            try {
                System.out.println(context.clientConnection().channel().getRemoteAddress() + " <== " + context.clientConnection().channel().getLocalAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(context.response());
            System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

            if (context.usingStoredResponse() != null) {
                context.usingStoredResponse().lockUsing();
            }

            ToClientCommon.sendStatusLine_Headers(context, server);

            SenderStatus ss = context.clientConnection().senderStatus();
            ss.changeState(SendingState.Body);

        }

        sendBodyBlock(context);
    }

    private void sendBodyBlock(Context context) {
        SenderStatus ss = context.clientConnection().senderStatus();
        if (context.response().isBodyFile() && ss.isOpenedResourceFile() == false) {
            openResourceFile(context);
        }
        if (context.response().isBodyFile()) {
            sendBodyBlockFile(context);
        } else if (context.response().bodyBytes() != null) {
            sendBodyBytes(context);
        } else {
            onEndRequest(context);
        }
    }

    private void openResourceFile(Context context) {
        try {
            FileInputStream is = new FileInputStream(context.response().bodyFile());
            context.clientConnection().senderStatus().resourceFileCh(is.getChannel());
        } catch (FileNotFoundException e) {
            e.printStackTrace();

            context.clientConnection().senderStatus().resourceFileCh(null);
            if (e.getMessage().endsWith(Error_TooManyFielsOpen)) {
                context.response(responseMaker().get_500TooManyFileOpen());
            } else {
                context.response(responseMaker().new_404NotFound(context.request()));
            }
        }
    }

    private void sendBodyBytes(Context context) {
        if (context.response().isPartial() == false || context.response().rangePartCount() < 2) {
            sendBodyBytesRange(context, context.response().bodyBytes(), context.response().range());
            sendBodyCRLF(context);
        } else {
            SenderStatus ss = context.clientConnection().senderStatus();
            ss.rangeCount(context.response().rangePartCount());

            for (int index = 0; index < context.response().rangePartCount(); index++) {
                ss.range(context.response().rangePart(index).range());
                sendBoundaryAndPartHeader(context);
                sendBodyBytesRange(context, context.response().bodyBytes(), context.response().rangePart(index).range());
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
        bufferSender().sendBufferToClient(context, temp, false);
    }

    private void sendBodyCRLF(Context context) {
        ByteBuffer temp = ByteBuffer.wrap(HttpString.CRLF);
        bufferSender().sendBufferToClient(context, temp, false);
    }

    private void onEndRequest(Context context) {
        if (context.usingStoredResponse() != null) {
            context.usingStoredResponse().freeUsing();
            context.usingStoredResponse(null);
        }

        if (context.response().statusCode().isError()) {
            bufferSender().sendCloseSignalForClient(context);
        } else {
            if (context.response().hasKeepAlive() == true) {
                Message.debug(context, "Persistent Connection");

                context.resetForNextRequest();
                server.gotoRequestReceiver(context);
            } else {
                bufferSender().sendCloseSignalForClient(context);
            }
        }
    }

    private void sendBodyBlockFile(Context context) {
        SenderStatus ss = context.clientConnection().senderStatus();

        if (ss.isStartRange()) {
            setRange(ss, context.response());

            if (ss.isMultipart()) {
                sendBoundaryAndPartHeader(context);
            }
        }

        sendResourceFileBlock(context);
    }

    private void setRange(SenderStatus ss, Response response) {
        ss.rangeCount(response.rangePartCount());

        if (ss.isMultipart()) {
            ss.range(response.rangePart(ss.rangeIndex()).range());
        } else {
            ss.range(response.range());
        }
    }

    private void sendBoundaryAndPartHeader(Context context) {
        ByteBuffer buffer = bufferManager().pooledSmallBuffer();
        ResponseToBuffer.forPartBoundary(buffer, context.response());
        ResponseToBuffer.forPartHeader(buffer, context.clientConnection().senderStatus(), context.response());
        buffer.flip();

        bufferSender().sendBufferToClient(context, buffer, true);
    }

    private void sendResourceFileBlock(Context context) {
        readResourceFile(context, new MyCompletionHandler<Integer, Context, ByteBuffer>() {
            @Override
            public void completed(Integer result, Context context, ByteBuffer buffer) {
                buffer.flip();
                bufferSender().sendBufferToClient(context, buffer, true);

                SenderStatus ss = context.clientConnection().senderStatus();
                ss.addSentSizeInRange(result);

                if (ss.isEndRange()) {
                    onEndRange(context);

                    if (ss.stateIsEndBody()) {
                        onEndBody(context, ss);
                    } else {
                        gotoSelf(context);
                    }
                } else {
                    gotoSelf(context);
                }
            }

            @Override
            public void failed(Throwable exc, Context context) {
                bufferSender().sendCloseSignalForClient(context);
            }
        });
    }


    private void readResourceFile(final Context context, final MyCompletionHandler<Integer, Context, ByteBuffer> handler) {
        server.objects().executorForFileReading()
                .execute(() -> {
                    try {
                        SenderStatus ss = context.clientConnection().senderStatus();
                        ByteBuffer buffer = bufferManager().pooledLargeBuffer();
                        if (buffer.capacity() > ss.remainingSendSize()) {
                            buffer.limit(ss.remainingSendSize());
                        }
                        int read = ss.resourceFileCh().read(buffer, ss.readingPosition());

                        handler.completed(read, context, buffer);
                    } catch (IOException e) {
                        e.printStackTrace();
                        handler.failed(e, context);
                    }
                });
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
        ResponseToBuffer.forEndBoundary(buffer, context.response());
        buffer.flip();

        bufferSender().sendBufferToClient(context, buffer, true);
    }

    private void closeResourceFile(Context context) {
        try {
            context.clientConnection().senderStatus().resourceFileCh().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private interface MyCompletionHandler<V, A, A2> {
        void completed(V result, A attachment, A2 attachment2);

        void failed(Throwable exc, A attachment);
    }
}
