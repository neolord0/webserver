package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;

public class FactoryForHeaderValue {
    public static HeaderValue create(HeaderSort sort) {
        switch (sort) {
            case Accept:
                return new HeaderValueAccept();
            case Accept_Charset:
                return new HeaderValueAcceptCharset();
            case Accept_Encoding:
                return new HeaderValueAcceptEncoding();
            case Accept_Language:
                return new HeaderValueAcceptLanguage();
            case Accept_Ranges:
                return new HeaderValueAcceptRanges();
            case Age:
                return new HeaderValueAge();
            case Allow:
                return new HeaderValueAllow();
            case Authorization:
                return new HeaderValueAuthorization();
            case Cache_Control:
                return new HeaderValueCacheControl();
            case Connection:
                return new HeaderValueConnection();
            case Content_Encoding:
                return new HeaderValueContentEncoding();
            case Content_Language:
                return new HeaderValueContentLanguage();
            case Content_Length:
                return new HeaderValueContentLength();
            case Content_Location:
                return new HeaderValueContentLocation();
            case Content_MD5:
                return new HeaderValueContentMD5();
            case Content_Range:
                return new HeaderValueContentRange();
            case Content_Type:
                return new HeaderValueContentType();
            case Date:
                return new HeaderValueDate();
            case ETag:
                return new HeaderValueETag();
            case Expect:
                return new HeaderValueExpect();
            case Expires:
                return new HeaderValueExpires();
            case From:
                return new HeaderValueFrom();
            case Host:
                return new HeaderValueHost();
            case If_Match:
                return new HeaderValueIfMatch();
            case If_Modified_Since:
                return new HeaderValueIfModifiedSince();
            case If_None_Match:
                return new HeaderValueIfNoneMatch();
            case If_Range:
                return new HeaderValueIfRange();
            case If_Unmodified_Since:
                return new HeaderValueIfUnmodifiedSince();
            case Keep_Alive:
                return new HeaderValueKeepAlive();
            case Last_Modified:
                return new HeaderValueLastModified();
            case Location:
                return new HeaderValueLocation();
            case Max_Forwards:
                return new HeaderValueMaxForwards();
            case Pragma:
                return new HeaderValuePragma();
            case Proxy_Authenticate:
                return new HeaderValueProxyAuthenticate();
            case Proxy_Authorization:
                return new HeaderValueProxyAuthorization();
            case Range:
                return new HeaderValueRange();
            case Referer:
                return new HeaderValueReferer();
            case Retry_After:
                return new HeaderValueRetryAfter();
            case Server:
                return new HeaderValueServer();
            case TE:
                return new HeaderValueTE();
            case Trailer:
                return new HeaderValueTrailer();
            case Transfer_Encoding:
                return new HeaderValueTransferEncoding();
            case Upgrade:
                return new HeaderValueUpgrade();
            case User_Agent:
                return new HeaderValueUserAgent();
            case Vary:
                return new HeaderValueVary();
            case Via:
                return new HeaderValueVia();
            case Warning:
                return new HeaderValueWarning();
            case WWW_Authenticate:
                return new HeaderValueWWWAuthenticate();
        }
        return null;
    }
}
