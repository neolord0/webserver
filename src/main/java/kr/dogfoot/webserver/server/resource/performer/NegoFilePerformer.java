package kr.dogfoot.webserver.server.resource.performer;


import kr.dogfoot.webserver.httpMessage.header.HeaderItem;
import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValue;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueContentType;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueVary;
import kr.dogfoot.webserver.httpMessage.reply.Reply;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.server.host.HostObjects;
import kr.dogfoot.webserver.server.resource.ResourceNegotiatedFile;
import kr.dogfoot.webserver.server.resource.filter.part.condition.CompareOperator;
import kr.dogfoot.webserver.server.resource.filter.part.condition.HeaderCondition;
import kr.dogfoot.webserver.server.resource.negotiation.NegotiationInfo;
import kr.dogfoot.webserver.server.resource.negotiation.NegotiationVariant;

import java.util.ArrayList;

public class NegoFilePerformer {
    public static Reply perform(Request request, ResourceNegotiatedFile resource, HostObjects hostObjects) {
        NegotiationVariant variant = getBestAvailableVariant(request, resource.negoInfo());
        if (variant != null) {
            Reply reply = variant.perform(request, hostObjects);
            appendHeader(reply, variant, resource.negoInfo());
            return reply;
        } else {
            return hostObjects.replyMaker().new_406NotAcceptable(resource);
        }
    }

    private static NegotiationVariant getBestAvailableVariant(Request request, NegotiationInfo negoInfo) {
        NegotiationVariant[] variants = negoInfo.variants();
        HeaderSort[] compareHeaders = negoInfo.compareHeaders();
        for (HeaderSort headerSort : compareHeaders) {
            if (variants != null) {
                variants = getAvailableVariants(request.getHeaderValueObj(headerSort), variants);
            }
        }

        if (variants == null || variants.length == 0) {
            return null;
        } else {
            return variants[0];
        }
    }

    private static NegotiationVariant[] getAvailableVariants(HeaderValue headerValue, NegotiationVariant[] variants) {
        if (headerValue == null) {
            return variants;
        }
        ArrayList<NegotiationVariant> result = new ArrayList<NegotiationVariant>();
        if (headerValue.hasQvalue()) {
            String bestValue = getBestAvailableValue(headerValue, variants);
            if (bestValue == null) {
                return null;
            } else if (bestValue.equals("*") || bestValue.equals("*/*")) {
                return variants;
            } else {
                for (NegotiationVariant variant : variants) {
                    HeaderCondition condition = variant.getCondition(headerValue.sort());

                    if (condition != null && bestValue.equalsIgnoreCase(condition.value())) {
                        result.add(variant);
                    }
                }
            }
        } else {
            for (NegotiationVariant variant : variants) {
                HeaderCondition condition = variant.getCondition(headerValue.sort());
                if (condition != null && headerValue.compare(CompareOperator.IsInclude, condition.value())) {
                    result.add(variant);
                }
            }
        }
        return result.toArray(new NegotiationVariant[0]);
    }

    private static String getBestAvailableValue(HeaderValue headerValue, NegotiationVariant[] variants) {
        String result = null;
        float max = -1;
        float qvalue = -1;
        String value;

        for (NegotiationVariant variant : variants) {
            HeaderCondition condition = variant.getCondition(headerValue.sort());
            if (condition != null) {
                value = condition.value();
            } else {
                value = null;
            }
            qvalue = headerValue.getQvalue(value);
            if (qvalue > max) {
                max = qvalue;
                result = value;
            }
        }
        return result;
    }


    private static void appendHeader(Reply reply, NegotiationVariant variant, NegotiationInfo negoInfo) {
        HeaderValueVary vary = new HeaderValueVary();

        for (HeaderSort headerSort : negoInfo.compareHeaders()) {
            HeaderCondition condition = variant.getCondition(headerSort);
            if (condition != null) {
                setCorrespondingHeader(reply, headerSort, condition.value());
            }
            vary.addFieldName(headerSort);
        }
        reply.addHeader(HeaderSort.Vary, vary.combineValue());
        reply.addHeader(HeaderSort.Content_Location, variant.pathFromRoot().getBytes());
    }


    private static void setCorrespondingHeader(Reply reply, HeaderSort headerSort, String value) {
        switch (headerSort) {
            case Accept:
                setContentType(reply, value);
                break;
            case Accept_Charset:
                setCharset(reply, value);
                break;
            case Accept_Encoding:
                reply.setHeader(HeaderSort.Content_Encoding, value.getBytes());
                break;
            case Accept_Language:
                reply.setHeader(HeaderSort.Content_Language, value.getBytes());
                break;
        }
    }

    private static void setCharset(Reply reply, String value) {
        if (reply.hasHeader(HeaderSort.Content_Type)) {
            HeaderItem headerItem = reply.getHeaderItem(HeaderSort.Content_Type);
            HeaderValueContentType contentType = null;
            try {
                contentType = (HeaderValueContentType) headerItem.updateValueObj();
                contentType.mediaType().setCharset(value);
            } catch (ParserException e) {
                e.printStackTrace();
            }
        }
    }

    private static void setContentType(Reply reply, String value) {
        if (reply.hasHeader(HeaderSort.Content_Type)) {
            HeaderItem headerItem = reply.getHeaderItem(HeaderSort.Content_Type);
            HeaderValueContentType contentType = null;
            try {
                contentType = (HeaderValueContentType) headerItem.updateValueObj();
                contentType.mediaType().setType(value);
                contentType.mediaType().setSubtype(null);
                headerItem.updateValueBytes();
            } catch (ParserException e) {
                e.printStackTrace();
            }
        } else {
            reply.addHeader(HeaderSort.Content_Type, value.getBytes());
        }
    }

}

