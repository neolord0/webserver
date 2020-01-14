package kr.dogfoot.webserver.parser;

import kr.dogfoot.webserver.httpMessage.header.HeaderItem;
import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.TransferCodingSort;
import kr.dogfoot.webserver.httpMessage.response.Response;
import kr.dogfoot.webserver.httpMessage.response.StatusCode;
import kr.dogfoot.webserver.httpMessage.response.maker.ResponseMaker;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;

import java.nio.ByteBuffer;

public class AjpResponseParser {
    public static Response sendHeadersToResponse(ByteBuffer buffer, ResponseMaker responseMaker) {
        Response response = new Response()
                .code(StatusCode.fromCode(readInt(buffer)))
                .reason(readBytesInString(buffer));

        int headerCount = readInt(buffer);
        for (int index = 0; index < headerCount; index++) {
            HeaderSort headerSort = readHeaderSort(buffer);
            byte[] value = readBytesInString(buffer);

            response.addHeader(headerSort, value);
        }
        return adjustHeader(response, responseMaker);
    }

    private static Response adjustHeader(Response response, ResponseMaker responseMaker) {
        if (response.getHeaderItem(HeaderSort.Date) == null) {
            responseMaker.addHeader_Date(response);
        }
        if (response.getHeaderItem(HeaderSort.Server) == null) {
            responseMaker.addHeader_Server(response);
        }

        if (response.hasContentLength() == false) {
            HeaderItem transferEncoding = response.getHeaderItem(HeaderSort.Transfer_Encoding);
            if (transferEncoding == null) {
                response.addHeader(HeaderSort.Transfer_Encoding,
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
        return response;
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
