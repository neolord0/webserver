package kr.dogfoot.webserver.parser;

import kr.dogfoot.webserver.httpMessage.header.HeaderItem;
import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.reply.Reply;
import kr.dogfoot.webserver.httpMessage.reply.ReplyCode;
import kr.dogfoot.webserver.httpMessage.reply.ReplyMaker;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.TransferCodingSort;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;

import java.nio.ByteBuffer;

public class AjpReplyParser {
    public static Reply sendHeadersToReply(ByteBuffer buffer, ReplyMaker replyMaker) {
        Reply reply = new Reply()
                .code(ReplyCode.fromCode(readInt(buffer)))
                .reason(readBytesInString(buffer));

        int headerCount = readInt(buffer);
        for (int index = 0; index < headerCount; index++) {
            HeaderSort headerSort = readHeaderSort(buffer);
            byte[] value = readBytesInString(buffer);

            reply.addHeader(headerSort, value);
        }
        return adjustHeader(reply, replyMaker);
    }


    private static Reply adjustHeader(Reply reply, ReplyMaker replyMaker) {
        if (reply.getHeaderItem(HeaderSort.Date) == null) {
            replyMaker.addHeader_Date(reply);
        }
        if (reply.getHeaderItem(HeaderSort.Server) == null) {
            replyMaker.addHeader_Server(reply);
        }

        if (reply.hasContentLength() == false) {
            HeaderItem transferEncoding = reply.getHeaderItem(HeaderSort.Transfer_Encoding);
            if (transferEncoding == null) {
                reply.addHeader(HeaderSort.Transfer_Encoding,
                        TransferCodingSort.Chunked.toString().getBytes());
            } else {
                OutputBuffer buffer = OutputBuffer.pooledObject();
                buffer.append(transferEncoding.valueBytes())
                        .appendComma()
                        .appendSP()
                        .append(TransferCodingSort.Chunked.toString());
                transferEncoding.valueBytes(buffer.getBytes());
                OutputBuffer.release(buffer);
            }
        }
        return reply;
    }


    private static HeaderSort readHeaderSort(ByteBuffer buffer) {
        short length = readInt(buffer);
        if ((length & 0xA0000) == 0xA0000) {
            return HeaderSort.fromAjpReceiveCode(length);
        }

        byte[] bytes = new byte[length];
        buffer.get(bytes);
        buffer.get();
        return HeaderSort.fromString(new String(bytes));
    }


    public static short readInt(ByteBuffer buffer) {
        return buffer.getShort();
    }


    public static String readString(ByteBuffer buffer) {
        short length = buffer.getShort();
        if (length == 0xffff) {
            return null;
        } else if (length == 0) {
            buffer.get();   // '0'
            return null;
        }
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        buffer.get();   // '0'

        return new String(bytes);
    }


    public static byte[] readBytesInString(ByteBuffer buffer) {
        short length = buffer.getShort();
        if (length == 0xffff) {
            return null;
        } else if (length == 0) {
            buffer.get();   // '0'
            return null;
        }

        byte[] bytes = new byte[length];
        buffer.get(bytes);
        buffer.get();   // '0'

        return bytes;
    }


    public static boolean readBool(ByteBuffer buffer) {
        return buffer.get() == 1;
    }
}
