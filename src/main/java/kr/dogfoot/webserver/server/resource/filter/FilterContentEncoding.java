package kr.dogfoot.webserver.server.resource.filter;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueAcceptEncoding;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueContentRange;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueVary;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.ContentCodingSort;
import kr.dogfoot.webserver.httpMessage.reply.Reply;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.server.object.BufferManager;
import kr.dogfoot.webserver.server.resource.filter.part.condition.HeaderConditionList;
import kr.dogfoot.webserver.util.bytes.Compressor;
import kr.dogfoot.webserver.util.bytes.ToBytes;

public class FilterContentEncoding extends Filter {
    private HeaderConditionList applyCondition;
    private ContentCodingSort coding;

    public FilterContentEncoding() {
        applyCondition = new HeaderConditionList();
    }

    @Override
    public boolean inboundProcess(Context context, Server server) {
        return true;
    }

    @Override
    public boolean outboundProcess(Context context, Server server) {
        if (!isAcceptEncoding(context.request())) {
            return true;
        }
        switch (coding) {
            case GZip:
                compressBody(ContentCodingSort.GZip, context.reply(), server.objects().bufferManager());
                break;
            case Deflate:
                compressBody(ContentCodingSort.Deflate, context.reply(), server.objects().bufferManager());
                break;
            case Unknown:
            case Compress:
            case Identity:
            case BR:
            case Asterisk:
                context.reply(server.objects().replyMaker().new_500NotSupportedEncoding(coding));
                break;
        }

        return true;
    }

    private boolean isAcceptEncoding(Request request) {
        HeaderValueAcceptEncoding acceptEncoding = (HeaderValueAcceptEncoding) request.getHeaderValueObj(HeaderSort.Accept_Encoding);
        if (acceptEncoding != null) {
            return acceptEncoding.getQvalue(coding) > 0;
        } else {
            return false;
        }
    }

    private void compressBody(ContentCodingSort contentCoding, Reply reply, BufferManager bufferManager) {
        byte[] compressed = null;
        if (reply.bodyFile() != null) {
            compressed = Compressor.compress(contentCoding, reply.bodyFile(), bufferManager);
        } else if (reply.bodyBytes() != null) {
            compressed = Compressor.compress(contentCoding, reply.bodyBytes());
        }

        if (compressed != null && compressed.length > 0) {
            reply.bodyBytes(compressed);
            reply.bodyFile(null);
            if (reply.isPartial()) {
                setContentRange(reply, compressed.length);
                if (reply.rangePartCount() != 0) {
                    reply.changeHeader(HeaderSort.Content_Length, ToBytes.fromLong(reply.calculateContextLength()));
                }
            } else {
                reply.range().lastPos(compressed.length - 1);
                reply.changeHeader(HeaderSort.Content_Length, ToBytes.fromInt(compressed.length));
            }
            reply.setHeader(HeaderSort.Content_Encoding, contentCoding.toString().getBytes());

            appendVaryHeader(reply);
        }
    }

    private void setContentRange(Reply reply, int length) {
        HeaderValueContentRange contentRange = (HeaderValueContentRange) reply.getHeaderValueObj(HeaderSort.Content_Range);
        if (contentRange != null) {
            contentRange.instanceLength().setLength(length);
            reply.changeHeader(HeaderSort.Content_Range, contentRange.combineValue());
        }

        for (int index = 0; index < reply.rangePartCount(); index++) {
            contentRange = (HeaderValueContentRange) reply.rangePart(index).getHeaderValueObj(HeaderSort.Content_Range);
            if (contentRange != null) {
                contentRange.instanceLength().setLength(length);
                reply.rangePart(index).changeHeader(HeaderSort.Content_Range, contentRange.combineValue());
            }
        }
    }

    private void appendVaryHeader(Reply reply) {
        HeaderValueVary vary = (HeaderValueVary) reply.getHeaderValueObj(HeaderSort.Vary);
        if (vary == null) {
            vary = new HeaderValueVary();
        }
        vary.addFieldName(HeaderSort.Accept_Encoding);

        reply.setHeader(HeaderSort.Vary, vary.combineValue());
    }

    @Override
    public FilterSort sort() {
        return FilterSort.ContentEncoding;
    }

    public HeaderConditionList applyCondition() {
        return applyCondition;
    }

    public ContentCodingSort coding() {
        return coding;
    }

    public void coding(ContentCodingSort coding) {
        this.coding = coding;
    }

    public boolean isMoreAppropriate(FilterContentEncoding compare, Request request) {
        HeaderValueAcceptEncoding acceptEncoding = (HeaderValueAcceptEncoding) request.getHeaderValueObj(HeaderSort.Accept_Encoding);
        if (acceptEncoding != null) {
            if (acceptEncoding.getMostAppropriateCodingSort() == ContentCodingSort.Identity) {
                return false;
            }
            float thisQValue = acceptEncoding.getQvalue(coding);
            if (compare == null) {
                return thisQValue != -1;
            } else {
                float compareQValue = acceptEncoding.getQvalue(compare.coding());
                return thisQValue > compareQValue;
            }
        }
        return false;
    }
}
