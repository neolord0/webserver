package kr.dogfoot.webserver.server.cache;

import kr.dogfoot.webserver.httpMessage.header.valueobj.part.CacheDirectiveSort;
import kr.dogfoot.webserver.httpMessage.request.Request;

import java.util.Set;


public class SelectedResourceInfo {

    private static SelectedResourceInfo nullØbject = new SelectedResourceInfo(null, PostSelectProcessing.Null, null, null);
    private CacheEntry entry;
    private PostSelectProcessing postSelectProcessing;
    private Request originalRequest;
    private Set<StoredResponse> storedResponses;


    public SelectedResourceInfo(CacheEntry entry, PostSelectProcessing postSelectProcessing, Request originalRequest, Set<StoredResponse> storedResponses) {
        this.entry = entry;
        this.postSelectProcessing = postSelectProcessing;
        this.originalRequest = originalRequest;
        this.storedResponses = storedResponses;
    }

    public static SelectedResourceInfo select(CacheEntry entry, Request request) {
        StoredResponseSet selectedResponses = entry.getResponseByMethodAndSelectedFields(request);

        if (selectedResponses.size() == 0) {
            return new SelectedResourceInfo(entry, PostSelectProcessing.SendRequest, request, null);
        } else {
            PostSelectProcessing postSelectProcessing;
            StoredResponseSet responses;

            if (request.hasNoCache()) {
                postSelectProcessing = PostSelectProcessing.SendValidationRequest;
                responses = selectedResponses;
            } else {
                StoredResponseSet filtered = filterNoCache(selectedResponses);
                if (filtered.size() == 0) {
                    postSelectProcessing = PostSelectProcessing.SendValidationRequest;
                    responses = selectedResponses;
                } else {
                    StoredResponseSet filtered2 = filterFresh(request, filtered);
                    if (filtered2.size() == 0) {
                        postSelectProcessing = PostSelectProcessing.SendValidationRequest;
                        responses = filtered;
                    } else {
                        postSelectProcessing = PostSelectProcessing.UseStoredResponse;
                        responses = filtered2;
                    }
                }
            }

            return new SelectedResourceInfo(entry, postSelectProcessing, request, responses);
        }
    }

    private static StoredResponseSet filterNoCache(Set<StoredResponse> responses) {
        StoredResponseSet filtered = new StoredResponseSet();
        for (StoredResponse storedResponse : responses) {
            if (storedResponse.response().hasCacheDirective(CacheDirectiveSort.NoCache) == false) {
                filtered.add(storedResponse);
            }
        }
        return filtered;
    }

    private static StoredResponseSet filterFresh(Request request, Set<StoredResponse> responses) {
        StoredResponseSet filtered = new StoredResponseSet();

        for (StoredResponse storedResponse : responses) {
            if (storedResponse.isFresh(request)) {
                filtered.add(storedResponse);
            }
        }
        return filtered;
    }

    public static SelectedResourceInfo nullObject() {
        return nullØbject;
    }

    public CacheEntry entry() {
        return entry;
    }

    public PostSelectProcessing postSelectProcessing() {
        return postSelectProcessing;
    }

    public Request originalRequest() {
        return originalRequest;
    }

    public Set<StoredResponse> storedResponses() {
        return storedResponses;
    }

    public StoredResponse mostRecentResponses() {
        if (storedResponses != null) {
            return storedResponses.iterator().next();
        }
        return null;
    }

    public void lockUsing() {
        for (StoredResponse sr : storedResponses) {
            sr.lockUsing();
        }
    }

    public void freeUsing() {
        for (StoredResponse sr : storedResponses) {
            sr.freeUsing();
        }
    }
}
