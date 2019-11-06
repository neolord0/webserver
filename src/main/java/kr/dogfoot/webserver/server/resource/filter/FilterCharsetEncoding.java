package kr.dogfoot.webserver.server.resource.filter;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.*;
import kr.dogfoot.webserver.httpMessage.reply.Reply;
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
        if (isAcceptTargetCharset(context.request()) && isNormalText(context.reply())) {
            if (changeCharset(context.reply())) {
                appendVaryHeader(context.reply());
            } else {
                context.reply(server.objects().replyMaker().new_500CannotChangeCharset(sourceCharset, targetCharset));
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

    private boolean isNormalText(Reply reply) {
        HeaderValueContentType contentType = (HeaderValueContentType) reply.getHeaderValueObj(HeaderSort.Content_Type);
        if (contentType != null && contentType.isText()) {
            HeaderValueContentEncoding contentEncoding = (HeaderValueContentEncoding) reply.getHeaderValueObj(HeaderSort.Content_Encoding);
            return contentEncoding == null || contentEncoding.isIdentity();
        }
        return false;
    }

    private boolean changeCharset(Reply reply) {
        byte[] sourceBody = null;
        if (reply.bodyFile() != null) {
            sourceBody = readBodyFile(reply.bodyFile());
        } else if (reply.bodyBytes() != null) {
            sourceBody = reply.bodyBytes();
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

        reply.bodyBytes(targetBody);
        reply.bodyFile(null);
        if (reply.isPartial()) {
            setContentRange(reply, targetBody.length);
            reply.changeHeader(HeaderSort.Content_Length, ToBytes.fromLong(reply.calculateContextLength()));
        } else {
            reply.changeHeader(HeaderSort.Content_Length, ToBytes.fromInt(targetBody.length));
        }
        HeaderValueContentType contentType = (HeaderValueContentType) reply.getHeaderValueObj(HeaderSort.Content_Type);
        contentType.mediaType().setCharset(targetCharset);
        reply.changeHeader(HeaderSort.Content_Type, contentType.combineValue());
        return true;
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

    private byte[] readBodyFile(File bodyFile) {
        byte[] bytes = null;
        try {
            bytes = Files.readAllBytes(bodyFile.toPath());
        } catch (IOException e) {
            bytes = null;
            e.printStackTrace();
        }
        return bytes;
    }


    private void appendVaryHeader(Reply reply) {
        HeaderValueVary vary = (HeaderValueVary) reply.getHeaderValueObj(HeaderSort.Vary);
        if (vary == null) {
            vary = new HeaderValueVary();
        }
        vary.addFieldName(HeaderSort.Accept_Charset);

        reply.setHeader(HeaderSort.Vary, vary.combineValue());
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

