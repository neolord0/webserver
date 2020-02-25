package kr.dogfoot.webserver.httpMessage.util;

import kr.dogfoot.webserver.httpMessage.header.HeaderItem;
import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.*;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.ContentCodingSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.TransferCodingSort;
import kr.dogfoot.webserver.httpMessage.request.MethodType;
import kr.dogfoot.webserver.httpMessage.response.EachRangePart;
import kr.dogfoot.webserver.httpMessage.response.Response;

import java.util.ArrayList;

public class ResponseSetter {
    public static void setContentLength(Response response, int length) {
        HeaderItem item = response.getHeaderItem(HeaderSort.Content_Length);
        if (item == null) {
            item = response.headerList().addHeader(HeaderSort.Content_Length);
        }

        HeaderValueContentLength contentLength = (HeaderValueContentLength) item.valueObj();
        contentLength.value(length);

        item.updateValueBytes();
    }

    public static void setCharsetOfContentType(Response response, String charset) {
        HeaderItem item = response.getHeaderItem(HeaderSort.Content_Type);
        if (item != null) {
            HeaderValueContentType contentType = (HeaderValueContentType) item.valueObj();
            contentType.mediaType().charset(charset);

            item.updateValueBytes();
        }
    }

    public static void setLengthOfContentRange(Response response, int length) {
        setContentRangeForEachRangePart(response, length);

        for (int index = 0; index < response.rangePartCount(); index++) {
            setContentRangeForEachRangePart(response.rangePart(index), length);
        }
    }

    private static void setContentRangeForEachRangePart(EachRangePart rangePart, int length) {
        HeaderItem item = rangePart.getHeaderItem(HeaderSort.Content_Range);
        if (item != null) {
            HeaderValueContentRange contentRange = (HeaderValueContentRange) item.valueObj();
            contentRange.instanceLength().length(length);
            item.updateValueBytes();
        }
    }

    public static void addFieldNameOfVaryHeader(Response response, ArrayList<HeaderSort> fieldNames) {
        HeaderItem item = response.getHeaderItem(HeaderSort.Vary);
        if (item == null) {
            item = response.headerList().addHeader(HeaderSort.Vary);
        }

        HeaderValueVary vary = (HeaderValueVary) item.valueObj();
        for (HeaderSort fieldName : fieldNames) {
            vary.addFieldName(fieldName);
        }

        item.updateValueBytes();
    }

    public static void addFieldNameOfVaryHeader(Response response, HeaderSort fieldName) {
        HeaderItem item = response.getHeaderItem(HeaderSort.Vary);
        if (item == null) {
            item = response.headerList().addHeader(HeaderSort.Vary);
        }

        HeaderValueVary vary = (HeaderValueVary) item.valueObj();
        vary.addFieldName(fieldName);

        item.updateValueBytes();
    }

    public static void addContentEncoding(Response response, ContentCodingSort contentCoding) {
        HeaderItem item = response.getHeaderItem(HeaderSort.Content_Encoding);
        if (item == null) {
            item = response.headerList().addHeader(HeaderSort.Content_Encoding);
        }

        HeaderValueContentEncoding contentEncoding = (HeaderValueContentEncoding) item.valueObj();
        contentEncoding.addContentCoding(contentCoding);

        item.updateValueBytes();
    }

    public static void setAge(Response response, long ageValue) {
        HeaderItem item = response.getHeaderItem(HeaderSort.Age);
        if (item == null) {
            item = response.headerList().addHeader(HeaderSort.Age);
        }

        HeaderValueAge age = (HeaderValueAge) item.valueObj();
        age.ageValue(ageValue);

        item.updateValueBytes();
    }

    public static void addTransferEncoding(Response response, TransferCodingSort transferCodingSort) {
        HeaderItem item = response.getHeaderItem(HeaderSort.Transfer_Encoding);
        if (item == null) {
            item = response.headerList().addHeader(HeaderSort.Transfer_Encoding);
        }

        HeaderValueTransferEncoding transferEncoding = (HeaderValueTransferEncoding) item.valueObj();
        transferEncoding.addTransferCoding(TransferCodingSort.Chunked);

        item.updateValueBytes();
    }

    public static void setContentType(Response response, String type, String subtype) {
        HeaderItem item = response.getHeaderItem(HeaderSort.Content_Type);
        if (item == null) {
            item = response.headerList().addHeader(HeaderSort.Content_Type);
        }

        HeaderValueContentType contentType = (HeaderValueContentType) item.valueObj();
        contentType.mediaType().type(type);
        contentType.mediaType().subtype(subtype);

        item.updateValueBytes();
    }

    public static void setHeader(Response response, HeaderSort sort, byte[] value) {
        if (response.hasHeader(sort)) {
            response.changeHeader(sort, value);
        } else {
            response.addHeader(sort, value);
        }
    }

    public static void setAllow(Response response, ArrayList<MethodType> methodList) {
        HeaderItem item = response.getHeaderItem(HeaderSort.Allow);
        if (item == null) {
            item = response.headerList().addHeader(HeaderSort.Allow);
        }

        HeaderValueAllow allow = (HeaderValueAllow) item.valueObj();
        for (MethodType mt : methodList) {
            if (allow.isInclude(mt) == false) {
                allow.addMethodType(mt);
            }
        }

        item.updateValueBytes();
    }

    public static void setContentLocation(Response response, String path) {
        HeaderItem item = response.getHeaderItem(HeaderSort.Content_Location);
        if (item == null) {
            item = response.headerList().addHeader(HeaderSort.Content_Location);
        }

        HeaderValueContentLocation contentLocation = (HeaderValueContentLocation) item.valueObj();
        contentLocation.uri(path);

        item.updateValueBytes();
    }

}
