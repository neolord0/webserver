package kr.dogfoot.webserver.server.resource.performer;


import kr.dogfoot.webserver.httpMessage.header.HeaderItem;
import kr.dogfoot.webserver.httpMessage.header.HeaderList;
import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValue;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.httpMessage.response.Response;
import kr.dogfoot.webserver.httpMessage.util.ResponseSetter;
import kr.dogfoot.webserver.server.host.HostObjects;
import kr.dogfoot.webserver.server.resource.ResourceNegotiatedFile;
import kr.dogfoot.webserver.server.resource.filter.part.condition.CompareOperator;
import kr.dogfoot.webserver.server.resource.filter.part.condition.HeaderCondition;
import kr.dogfoot.webserver.server.resource.negotiation.NegotiationInfo;
import kr.dogfoot.webserver.server.resource.negotiation.NegotiationVariant;

import java.util.ArrayList;

public class NegoFilePerformer {
    public static Response perform(Request request, ResourceNegotiatedFile resource, HostObjects hostObjects) {
        NegotiationVariant variant = getBestAvailableVariant(request, resource.negoInfo());
        if (variant != null) {
            Response response = variant.perform(request, hostObjects);
            appendHeader(response, variant, resource.negoInfo());
            return response;
        } else {
            return hostObjects.responseMaker().new_406NotAcceptable(resource);
        }
    }

    private static NegotiationVariant getBestAvailableVariant(Request request, NegotiationInfo negoInfo) {
        NegotiationVariant[] variants = negoInfo.variants();
        HeaderSort[] compareHeaders = negoInfo.compareHeaders();
        for (HeaderSort headerSort : compareHeaders) {
            if (variants != null) {
                variants = getAvailableVariants(request.headerList(), headerSort, variants);
            }
        }

        if (variants == null || variants.length == 0) {
            return null;
        } else {
            return variants[0];
        }
    }

    private static NegotiationVariant[] getAvailableVariants(HeaderList headerList, HeaderSort sort, NegotiationVariant[] variants) {
        ArrayList<NegotiationVariant> result = new ArrayList<NegotiationVariant>();
        HeaderItem item = headerList.getHeader(sort);
        if (item != null && item.valueObj().hasQvalue()) {
            String bestValue = getBestAvailableValue(item.valueObj(), variants);
            if (bestValue == null) {
                return null;
            } else if (bestValue.equals("*") || bestValue.equals("*/*")) {
                return variants;
            } else {
                for (NegotiationVariant variant : variants) {
                    HeaderCondition condition = variant.getCondition(sort);
                    if (condition != null && bestValue.equalsIgnoreCase(condition.value())) {
                        result.add(variant);
                    }
                }
            }
        } else {
            for (NegotiationVariant variant : variants) {
                HeaderCondition condition = variant.getCondition(sort);
                if (condition != null && headerList.compare(sort, CompareOperator.Include, condition.value())) {
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


    private static void appendHeader(Response response, NegotiationVariant variant, NegotiationInfo negoInfo) {
        ArrayList<HeaderSort> fieldNames = new ArrayList<HeaderSort>();
        for (HeaderSort headerSort : negoInfo.compareHeaders()) {
            HeaderCondition condition = variant.getCondition(headerSort);
            if (condition != null) {
                setCorrespondingHeader(response, headerSort, condition.value());
            }
            fieldNames.add(headerSort);
        }

        ResponseSetter.addFieldNameOfVaryHeader(response, fieldNames);
        ResponseSetter.setContentLocation(response, variant.pathFromRoot());
    }


    private static void setCorrespondingHeader(Response response, HeaderSort headerSort, String value) {
        switch (headerSort) {
            case Accept:
                ResponseSetter.setContentType(response, value, null);
                break;
            case Accept_Charset:
                ResponseSetter.setCharsetOfContentType(response, value);
                break;
            case Accept_Encoding:
                ResponseSetter.setHeader(response, HeaderSort.Content_Encoding, value.getBytes());
                break;
            case Accept_Language:
                ResponseSetter.setHeader(response, HeaderSort.Content_Language, value.getBytes());
                break;
        }
    }
}

