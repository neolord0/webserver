package kr.dogfoot.webserver.server.resource.filter;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueAcceptEncoding;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueContentRange;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueVary;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.ContentCodingSort;
import kr.dogfoot.webserver.httpMessage.response.Response;
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
                compressBody(ContentCodingSort.GZip, context.response(), server.objects().bufferManager());
                break;
            case Deflate:
                compressBody(ContentCodingSort.Deflate, context.response(), server.objects().bufferManager());
                break;
            case Unknown:
            case Compress:
            case Identity:
            case BR:
            case Asterisk:
                context.response(server.objects().responseMaker().new_500NotSupportedEncoding(coding));
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

    private void compressBody(ContentCodingSort contentCoding, Response response, BufferManager bufferManager) {
        byte[] compressed = null;
        if (response.bodyFile() != null) {
            compressed = Compressor.compress(contentCoding, response.bodyFile(), bufferManager);
        } else if (response.bodyBytes() != null) {
            compressed = Compressor.compress(contentCoding, response.bodyBytes());
        }

        if (compressed != null && compressed.length > 0) {
            response.bodyBytes(compressed);
            response.bodyFile(null);
            if (response.isPartial()) {
                setContentRange(response, compressed.length);
                if (response.rangePartCount() != 0) {
                    response.changeHeader(HeaderSort.Content_Length, ToBytes.fromLong(response.calculateContextLength()));
                }
            } else {
                response.range().lastPos(compressed.length - 1);
                response.changeHeader(HeaderSort.Content_Length, ToBytes.fromInt(compressed.length));
            }
            response.setHeader(HeaderSort.Content_Encoding, contentCoding.toString().getBytes());

            appendVaryHeader(response);
        }
    }

    private void setContentRange(Response response, int length) {
        HeaderValueContentRange contentRange = (HeaderValueContentRange) response.getHeaderValueObj(HeaderSort.Content_Range);
        if (contentRange != null) {
            contentRange.instanceLength().setLength(length);
            response.changeHeader(HeaderSort.Content_Range, contentRange.combineValue());
        }

        for (int index = 0; index < response.rangePartCount(); index++) {
            contentRange = (HeaderValueContentRange) response.rangePart(index).getHeaderValueObj(HeaderSort.Content_Range);
            if (contentRange != null) {
                contentRange.instanceLength().setLength(length);
                response.rangePart(index).changeHeader(HeaderSort.Content_Range, contentRange.combineValue());
            }
        }
    }

    private void appendVaryHeader(Response response) {
        HeaderValueVary vary = (HeaderValueVary) response.getHeaderValueObj(HeaderSort.Vary);
        if (vary == null) {
            vary = new HeaderValueVary();
        }
        vary.addFieldName(HeaderSort.Accept_Encoding);

        response.setHeader(HeaderSort.Vary, vary.combineValue());
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
