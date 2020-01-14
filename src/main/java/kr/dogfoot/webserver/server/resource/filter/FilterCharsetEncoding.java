package kr.dogfoot.webserver.server.resource.filter;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.*;
import kr.dogfoot.webserver.httpMessage.response.Response;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.util.bytes.ToBytes;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;

public class FilterCharsetEncoding extends Filter {
    private String sourceCharset;
    private String targetCharset;

    public FilterCharsetEncoding() {
    }

    @Override
    public boolean inboundProcess(Context context, Server server) {
        return true;
    }

    @Override
    public boolean outboundProcess(Context context, Server server) {
        if (isAcceptTargetCharset(context.request()) && isNormalText(context.response())) {
            if (changeCharset(context.response())) {
                appendVaryHeader(context.response());
            } else {
                context.response(server.objects().responseMaker().new_500CannotChangeCharset(sourceCharset, targetCharset));
            }
        }
        return true;
    }

    private boolean isAcceptTargetCharset(Request request) {
        HeaderValueAcceptCharset acceptCharset = (HeaderValueAcceptCharset) request.getHeaderValueObj(HeaderSort.Accept_Charset);
        if (acceptCharset != null) {
            return acceptCharset.getQvalue(targetCharset) > 0;
        }
        return false;
    }

    private boolean isNormalText(Response response) {
        HeaderValueContentType contentType = (HeaderValueContentType) response.getHeaderValueObj(HeaderSort.Content_Type);
        if (contentType != null && contentType.isText()) {
            HeaderValueContentEncoding contentEncoding = (HeaderValueContentEncoding) response.getHeaderValueObj(HeaderSort.Content_Encoding);
            return contentEncoding == null || contentEncoding.isIdentity();
        }
        return false;
    }

    private boolean changeCharset(Response response) {
        byte[] sourceBody = null;
        if (response.bodyFile() != null) {
            sourceBody = readBodyFile(response.bodyFile());
        } else if (response.bodyBytes() != null) {
            sourceBody = response.bodyBytes();
        }

        if (sourceBody == null) {
            return false;
        }

        byte[] targetBody = null;
        try {
            targetBody = new String(sourceBody, sourceCharset).getBytes(targetCharset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            targetBody = null;
        }
        if (targetBody == null) {
            return false;
        }

        response.bodyBytes(targetBody);
        response.bodyFile(null);
        if (response.isPartial()) {
            setContentRange(response, targetBody.length);
            response.changeHeader(HeaderSort.Content_Length, ToBytes.fromLong(response.calculateContextLength()));
        } else {
            response.changeHeader(HeaderSort.Content_Length, ToBytes.fromInt(targetBody.length));
        }
        HeaderValueContentType contentType = (HeaderValueContentType) response.getHeaderValueObj(HeaderSort.Content_Type);
        contentType.mediaType().setCharset(targetCharset);
        response.changeHeader(HeaderSort.Content_Type, contentType.combineValue());
        return true;
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

    private byte[] readBodyFile(File bodyFile) {
        byte[] bytes = null;
        try {
            bytes = Files.readAllBytes(bodyFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();;
            bytes = null;
            e.printStackTrace();
        }
        return bytes;
    }


    private void appendVaryHeader(Response Response) {
        HeaderValueVary vary = (HeaderValueVary) Response.getHeaderValueObj(HeaderSort.Vary);
        if (vary == null) {
            vary = new HeaderValueVary();
        }
        vary.addFieldName(HeaderSort.Accept_Charset);

        Response.setHeader(HeaderSort.Vary, vary.combineValue());
    }


    @Override
    public FilterSort sort() {
        return FilterSort.CharsetEncoding;
    }

    public String sourceCharset() {
        return sourceCharset;
    }

    public void sourceCharset(String sourceCharset) {
        this.sourceCharset = sourceCharset;
    }

    public String targetCharset() {
        return targetCharset;
    }

    public void targetCharset(String targetCharset) {
        this.targetCharset = targetCharset;
    }

    public boolean isMoreAppropriate(FilterCharsetEncoding compare, Request request) {
        HeaderValueAcceptCharset acceptCharset = (HeaderValueAcceptCharset) request.getHeaderValueObj(HeaderSort.Accept_Charset);
        if (acceptCharset != null) {
            float thisQValue = acceptCharset.getQvalue(targetCharset);
            if (compare == null) {
                return thisQValue != -1;
            } else {
                float compareQValue = acceptCharset.getQvalue(compare.targetCharset());
                return thisQValue > compareQValue;
            }
        }
        return false;
    }
}

