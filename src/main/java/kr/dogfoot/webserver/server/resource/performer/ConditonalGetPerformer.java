package kr.dogfoot.webserver.server.resource.performer;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.*;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.httpMessage.response.Response;
import kr.dogfoot.webserver.server.cache.SelectedResourceInfo;
import kr.dogfoot.webserver.server.cache.StoredResponse;
import kr.dogfoot.webserver.server.host.HostObjects;
import kr.dogfoot.webserver.server.resource.ResourceFile;
import kr.dogfoot.webserver.util.bytes.BytesUtil;

public class ConditonalGetPerformer {
    public static Response perform(Request request, ResourceFile resource, HostObjects hostObjects) {
        if (request.hasHeader(HeaderSort.If_Range) && request.hasHeader(HeaderSort.Range)) {
            ifRange(request, resource);
        }
        Response response = null;
        if (request.hasHeader(HeaderSort.If_Unmodified_Since) || request.hasHeader(HeaderSort.If_Match)) {
            response = ifUnmodifiedSince_ifMatch(request, resource, hostObjects);
        }
        if ((response == null) &&
                (request.hasHeader(HeaderSort.If_Modified_Since) || request.hasHeader(HeaderSort.If_None_Match))) {
            response = ifModifiedSince_ifNoneMatch(request, resource, hostObjects);
        }
        if (response != null) {
            FilePerformer.connection(request, response, hostObjects);
        }

        return response;
    }

    private static void ifRange(Request request, ResourceFile resource) {
        HeaderValueIfRange ifRange = (HeaderValueIfRange) request.getHeaderValueObj(HeaderSort.If_Range);
        if (ifRange.date() != null) {
            if (resource.lastModified() != ifRange.date()) {
                request.headerList().remove(HeaderSort.Range);
            }
        } else {
            if (BytesUtil.compare(resource.etag(), 0, resource.etag().length, ifRange.entityTag()) != 0) {
                request.headerList().remove(HeaderSort.Range);
            }
        }
    }

    private static Response ifUnmodifiedSince_ifMatch(Request request, ResourceFile resource, HostObjects hostObjects) {
        HeaderValueIfUnmodifiedSince ifUnmodifiedSince = (HeaderValueIfUnmodifiedSince) request.getHeaderValueObj(HeaderSort.If_Unmodified_Since);
        HeaderValueIfMatch ifMatch = (HeaderValueIfMatch) request.getHeaderValueObj(HeaderSort.If_Match);

        if (ifUnmodifiedSince != null && ifMatch != null) {
            if (ifUnmodifiedSince.date() <= resource.lastModified() &&
                    (ifMatch.isAsterisk() == false && ifMatch.isInclude(resource.etag()) == false)) {
                return hostObjects.responseMaker().new_412PreconditionFailed(resource);
            }

        } else if (ifUnmodifiedSince != null && ifMatch == null) {
            if (ifUnmodifiedSince.date() <= resource.lastModified()) {
                return hostObjects.responseMaker().new_412PreconditionFailed(resource);
            }

        } else if (ifUnmodifiedSince == null && ifMatch != null) {
            if (ifMatch.isAsterisk() == false && ifMatch.isInclude(resource.etag()) == false) {
                return hostObjects.responseMaker().new_412PreconditionFailed(resource);
            }
        }
        return null;
    }

    private static Response ifModifiedSince_ifNoneMatch(Request request, ResourceFile resource, HostObjects hostObjects) {
        HeaderValueIfModifiedSince ifModifiedSince = (HeaderValueIfModifiedSince) request.getHeaderValueObj(HeaderSort.If_Modified_Since);
        HeaderValueIfNoneMatch ifNoneMatch = (HeaderValueIfNoneMatch) request.getHeaderValueObj(HeaderSort.If_None_Match);

        if (ifModifiedSince != null && ifNoneMatch != null) {
            if (ifModifiedSince.date() != null && ifModifiedSince.date()  >= resource.lastModified()
                    && (ifNoneMatch.isAsterisk() == true || ifNoneMatch.isInclude(resource.etag()) == true)) {
                return hostObjects.responseMaker().new_304NotModified(resource);
            }
        } else if (ifModifiedSince != null && ifNoneMatch == null) {
            if (ifModifiedSince.date() >= resource.lastModified()) {
                return hostObjects.responseMaker().new_304NotModified(resource);
            }
        } else if (ifModifiedSince == null && ifNoneMatch != null) {
            if ((ifNoneMatch.isAsterisk() == true || ifNoneMatch.isInclude(resource.etag()) == true)) {
                return hostObjects.responseMaker().new_304NotModified(resource);
            }
        }
        return null;
    }

    public static Response performForCache(Request request, SelectedResourceInfo selectedResourceInfo, HostObjects hostObjects) {
        HeaderValueIfNoneMatch ifNoneMatch = (HeaderValueIfNoneMatch) request.getHeaderValueObj(HeaderSort.If_None_Match);
        if (ifNoneMatch != null) {
            if (ifNoneMatch.isAsterisk()) {
                return hostObjects.responseMaker().new_304NotModified(selectedResourceInfo.mostRecentResponses());
            } else {
                for (StoredResponse storedResponse : selectedResourceInfo.storedResponses()) {
                    HeaderValueETag etag = (HeaderValueETag) storedResponse.response().getHeaderValueObj(HeaderSort.ETag);
                    if (etag != null && ifNoneMatch.isInclude(etag.etag()) == true) {
                        return hostObjects.responseMaker().new_304NotModified(storedResponse);
                    }
               }
            }
        } else {
            HeaderValueIfModifiedSince ifModifiedSince = (HeaderValueIfModifiedSince) request.getHeaderValueObj(HeaderSort.If_Modified_Since);
            if (ifModifiedSince != null) {
                for (StoredResponse storedResponse : selectedResourceInfo.storedResponses()) {
                    HeaderValueLastModified lastModified = (HeaderValueLastModified) storedResponse.response().getHeaderValueObj(HeaderSort.Last_Modified);
                    if (lastModified != null) {
                        if (ifModifiedSince.date() >= lastModified.date()) {
                            return hostObjects.responseMaker().new_304NotModified(storedResponse);
                        }
                    } else {
                        HeaderValueDate date = (HeaderValueDate) storedResponse.response().getHeaderValueObj(HeaderSort.Date);
                        if (date != null) {
                            if (ifModifiedSince.date() >= date.date()) {
                                return hostObjects.responseMaker().new_304NotModified(storedResponse);
                            }
                        } else {
                            if (ifModifiedSince.date() >= storedResponse.responseTime()) {
                                return hostObjects.responseMaker().new_304NotModified(storedResponse);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public static Response performForCache(Request request, StoredResponse storedResponse, HostObjects hostObjects) {
        HeaderValueIfNoneMatch ifNoneMatch = (HeaderValueIfNoneMatch) request.getHeaderValueObj(HeaderSort.If_None_Match);
        if (ifNoneMatch != null) {
            if (ifNoneMatch.isAsterisk()) {
                return hostObjects.responseMaker().new_304NotModified(storedResponse);
            } else {

               HeaderValueETag etag = (HeaderValueETag) storedResponse.response().getHeaderValueObj(HeaderSort.ETag);
                if (etag != null && ifNoneMatch.isInclude(etag.etag()) == true) {
                    return hostObjects.responseMaker().new_304NotModified(storedResponse);
                }
            }
        } else {
            HeaderValueIfModifiedSince ifModifiedSince = (HeaderValueIfModifiedSince) request.getHeaderValueObj(HeaderSort.If_Modified_Since);
            if (ifModifiedSince != null) {
                HeaderValueLastModified lastModified = (HeaderValueLastModified) storedResponse.response().getHeaderValueObj(HeaderSort.Last_Modified);
                if (lastModified != null) {
                    if (ifModifiedSince.date() >= lastModified.date()) {
                        return hostObjects.responseMaker().new_304NotModified(storedResponse);
                    }
                } else {
                    HeaderValueDate date = (HeaderValueDate) storedResponse.response().getHeaderValueObj(HeaderSort.Date);
                    if (date != null) {
                        if (ifModifiedSince.date() >= date.date()) {
                            return hostObjects.responseMaker().new_304NotModified(storedResponse);
                        }
                    } else {
                        if (ifModifiedSince.date() >= storedResponse.responseTime()) {
                            return hostObjects.responseMaker().new_304NotModified(storedResponse);
                        }
                    }
                }
            }
        }
        return null;
    }
}
