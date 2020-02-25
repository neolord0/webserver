package kr.dogfoot.webserver.server.resource.filter;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueAcceptCharset;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueContentEncoding;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueContentType;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.httpMessage.response.Response;
import kr.dogfoot.webserver.httpMessage.util.ResponseSetter;
import kr.dogfoot.webserver.server.Server;

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
                ResponseSetter.addFieldNameOfVaryHeader(context.response(), HeaderSort.Accept_Charset);
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
        byte[] originalBody = null;
        if (response.bodyFile() != null) {
            originalBody = readBodyFile(response.bodyFile());
        } else if (response.bodyBytes() != null) {
            originalBody = response.bodyBytes();
        }

        if (originalBody == null) {
            return false;
        }

        byte[] changedBody;
        try {
            changedBody = new String(originalBody, sourceCharset).getBytes(targetCharset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            changedBody = null;
        }
        if (changedBody == null) {
            return false;
        }

        response.bodyBytes(changedBody);
        response.bodyFile(null);
        if (response.isPartial()) {
            ResponseSetter.setLengthOfContentRange(response, changedBody.length);
            ResponseSetter.setContentLength(response, response.calculateContextLength());
        } else {
            ResponseSetter.setContentLength(response, changedBody.length);
        }

        ResponseSetter.setCharsetOfContentType(response, targetCharset);
        return true;
    }

    private byte[] readBodyFile(File bodyFile) {
        byte[] bytes = null;
        try {
            bytes = Files.readAllBytes(bodyFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            ;
            bytes = null;
            e.printStackTrace();
        }
        return bytes;
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

