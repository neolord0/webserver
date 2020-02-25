package kr.dogfoot.webserver.httpMessage.util;

import kr.dogfoot.webserver.httpMessage.header.HeaderItem;
import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.*;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.RecipientInfo;
import kr.dogfoot.webserver.httpMessage.request.Request;

import java.util.ArrayList;

public class RequestSetter {
    public static void addETagToIfNoneMatch(Request request, ArrayList<byte[]> etags) {
        HeaderItem item = request.headerList().getHeader(HeaderSort.If_None_Match);
        if (item == null) {
            item = request.headerList().addHeader(HeaderSort.If_None_Match);
        }

        HeaderValueIfNoneMatch ifNoneMatchValue = (HeaderValueIfNoneMatch) item.valueObj();
        for (byte[] etag : etags) {
            if (ifNoneMatchValue.isInclude(etag) == false) {
                ifNoneMatchValue.addEntityTag(etag);
            }
        }

        item.updateValueBytes();
    }

    public static void setIfModifiedSince(Request request, Long date) {
        HeaderItem item = request.headerList().getHeader(HeaderSort.If_Modified_Since);
        if (item == null) {
            item = request.headerList().addHeader(HeaderSort.If_Modified_Since);
        }

        HeaderValueIfModifiedSince ifModifiedSince = (HeaderValueIfModifiedSince) item.valueObj();
        ifModifiedSince.date(date);

        item.updateValueBytes();
    }


    public static void addVia(Request request, String protocolName, String protocolVersion, String receivedBy, String comment) {
        HeaderItem item = request.headerList().getHeader(HeaderSort.Via);
        if (item == null) {
            item = request.headerList().addHeader(HeaderSort.Via);
        }

        HeaderValueVia via = (HeaderValueVia) item.valueObj();
        RecipientInfo ri = via.addNewRecipientInfo();
        ri.protocolName(protocolName);
        ri.protocolVersion(protocolVersion);
        ri.receivedBy(receivedBy);
        ri.comment(comment);
        item.updateValueBytes();
    }

    public static void reduceMaxForwards(Request request) {
        HeaderItem item = request.headerList().getHeader(HeaderSort.Max_Forwards);
        if (item != null) {
            HeaderValueMaxForwards maxForwards = (HeaderValueMaxForwards) item.valueObj();
            maxForwards.value(maxForwards.value() - 1);

            item.updateValueBytes();
        }
    }

    public static void setHost(Request request, String ipOrDomain, int port) {
        HeaderItem item = request.headerList().getHeader(HeaderSort.Host);
        if (item != null) {
            HeaderValueHost host = (HeaderValueHost) item.valueObj();
            host.ipOrDomain(ipOrDomain);
            host.port(port);

            item.updateValueBytes();
        }
    }
}
