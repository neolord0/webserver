package kr.dogfoot.webserver.server.resource.performer;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.*;
import kr.dogfoot.webserver.httpMessage.reply.Reply;
import kr.dogfoot.webserver.httpMessage.reply.ReplyMaker;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.server.host.HostObjects;
import kr.dogfoot.webserver.server.resource.ResourceFile;
import kr.dogfoot.webserver.util.bytes.BytesUtil;

public class ConditonalGetPerformer {
    public static Reply perform(Request request, ResourceFile resource, HostObjects hostObjects) {
        if (request.hasHeader(HeaderSort.If_Range) && request.hasHeader(HeaderSort.Range)) {
            ifRange(request, resource);
        }
        Reply reply = null;
        if (request.hasHeader(HeaderSort.If_Unmodified_Since) || request.hasHeader(HeaderSort.If_Match)) {
            reply = ifUnmodifiedSince_ifMatch(request, resource, hostObjects);
        }
        if ((reply == null) &&
                (request.hasHeader(HeaderSort.If_Modified_Since) || request.hasHeader(HeaderSort.If_None_Match))) {
            reply = ifModifiedSince_ifNoneMatch(request, resource, hostObjects);
        }
        if (reply != null) {
            FilePerformer.connection(request, reply, hostObjects);
        }

        return reply;
    }

    private static void ifRange(Request request, ResourceFile resource) {
        HeaderValueIfRange ifRange = (HeaderValueIfRange) request.getHeaderValueObj(HeaderSort.If_Range);
        if (ifRange.date() != null) {
            if (resource.lastModified() != ifRange.date()) {
                request.headerList().remove(HeaderSort.Range);
            }
        } else {
            if (BytesUtil.compare(resource.getETag(), 0, resource.getETag().length, ifRange.entityTag()) != 0) {
                request.headerList().remove(HeaderSort.Range);
            }
        }
    }

    private static Reply ifUnmodifiedSince_ifMatch(Request request, ResourceFile resource, HostObjects hostObjects) {
        HeaderValueIfUnmodifiedSince ifUnmodifiedSince = (HeaderValueIfUnmodifiedSince) request.getHeaderValueObj(HeaderSort.If_Unmodified_Since);
        HeaderValueIfMatch ifMatch = (HeaderValueIfMatch) request.getHeaderValueObj(HeaderSort.If_Match);

        if (ifUnmodifiedSince != null && ifMatch != null) {
            if (ifUnmodifiedSince.date() <= resource.lastModified() &&
                    (ifMatch.isAsterisk() == false && ifMatch.isMatch(resource.getETag()) == false)) {
                return hostObjects.replyMaker().new_412PreconditionFailed(resource);
            }

        } else if (ifUnmodifiedSince != null && ifMatch == null) {
            if (ifUnmodifiedSince.date() <= resource.lastModified()) {
                return hostObjects.replyMaker().new_412PreconditionFailed(resource);
            }

        } else if (ifUnmodifiedSince == null && ifMatch != null) {
            if (ifMatch.isAsterisk() == false && ifMatch.isMatch(resource.getETag()) == false) {
                return hostObjects.replyMaker().new_412PreconditionFailed(resource);
            }
        }
        return null;
    }

    private static Reply ifModifiedSince_ifNoneMatch(Request request, ResourceFile resource, HostObjects hostObjects) {
        HeaderValueIfModifiedSince ifModifiedSince = (HeaderValueIfModifiedSince) request.getHeaderValueObj(HeaderSort.If_Modified_Since);
        HeaderValueIfNoneMatch ifNoneMatch = (HeaderValueIfNoneMatch) request.getHeaderValueObj(HeaderSort.If_None_Match);

        if (ifModifiedSince != null && ifNoneMatch != null) {
            if (ifModifiedSince.date() >= resource.lastModified()
                    && (ifNoneMatch.isAsterisk() == true || ifNoneMatch.isMatch(resource.getETag()) == true)) {
                return hostObjects.replyMaker().new_304NotModified(resource);
            }
        } else if (ifModifiedSince != null && ifNoneMatch == null) {
            if (ifModifiedSince.date() >= resource.lastModified()) {
                return hostObjects.replyMaker().new_304NotModified(resource);
            }
        } else if (ifModifiedSince == null && ifNoneMatch != null) {
            if ((ifNoneMatch.isAsterisk() == true || ifNoneMatch.isMatch(resource.getETag()) == true)) {
                return hostObjects.replyMaker().new_304NotModified(resource);
            }
        }
        return null;
    }
}
