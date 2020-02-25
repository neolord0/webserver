package kr.dogfoot.webserver.server.cache;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueETag;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.httpMessage.response.Response;
import kr.dogfoot.webserver.server.resource.look.LookState;
import kr.dogfoot.webserver.server.timer.TimerEventHandler;
import kr.dogfoot.webserver.util.bytes.BytesUtil;

import java.io.File;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CacheEntry implements TimerEventHandler {
    private CacheManagerImp manager;
    private CacheEntry parentEntry;

    private String path;
    private File directoryFile;

    private ConcurrentLinkedQueue<CacheEntry> childEntrys;
    private ConcurrentLinkedQueue<StoredResponse> responses;

    public CacheEntry(CacheManagerImp manager, File parentDirectoryFile, String path) {
        this.manager = manager;
        this.parentEntry = null;

        this.path = path;
        directoryFile = StoredResponseStorer.openDirectory(parentDirectoryFile, path);

        childEntrys = new ConcurrentLinkedQueue<CacheEntry>();
        responses = new ConcurrentLinkedQueue<StoredResponse>();
    }

    public CacheEntry(CacheManagerImp manager, CacheEntry parentEntry, String path) {
        this.manager = manager;
        this.parentEntry = parentEntry;

        this.path = path;
        directoryFile = StoredResponseStorer.openDirectory(parentEntry.directoryFile, path);

        childEntrys = new ConcurrentLinkedQueue<CacheEntry>();
        responses = new ConcurrentLinkedQueue<StoredResponse>();
    }

    public CacheEntry find(LookState ls, boolean createChildItem) {
        String path = ls.getNextPathItem();
        for (CacheEntry childEntry : childEntrys) {
            if (childEntry.path.equals(path)) {
                if (ls.isLastPathItem() == false) {
                    return childEntry.find(ls, createChildItem);
                } else {
                    return childEntry;
                }
            }
        }
        if (createChildItem == true) {
            if (ls.isLastPathItem() == false) {
                return addNewEntry(path).find(ls, createChildItem);
            } else {
                return addNewEntry(path);
            }
        } else {
            return null;
        }
    }

    private CacheEntry addNewEntry(String path) {
        CacheEntry newEntry = new CacheEntry(manager, this, path);
        addEntry(newEntry);
        return newEntry;
    }

    public void addEntry(CacheEntry entry) {
        childEntrys.add(entry);
    }

    public StoredResponse addNewResponse(Request request, Response response) {
        StoredResponse newResponse = StoredResponse.pooledObject();
        newResponse.set(request, response, this);
        addResponse(newResponse);
        return newResponse;
    }

    public synchronized void addResponse(StoredResponse response) {
        response.startTimerForCheckingInactive();
        responses.add(response);
    }

    public StoredResponse getResponseByValidator(Response response) {
        if (response.hasValidator() == false) {
            if (responses.size() == 1) {
                StoredResponse item = responses.peek();
                if (item.response().hasValidator() == false) {
                    return item;
                }
            }
        } else {
            HeaderValueETag eTag = (HeaderValueETag) response.getHeaderValueObj(HeaderSort.ETag);
            StoredResponseSet storedResponses = getResponseByETag(eTag.etag());
            if (storedResponses.size() > 0) {
                return storedResponses.iterator().next();
            }
        }
        return null;
    }

    public StoredResponseSet getResponseByMethodAndSelectedFields(Request request) {
        StoredResponseSet foundResponses = new StoredResponseSet();
        for (StoredResponse response : responses) {
            if (response.isMatchMethodAndSelectedFields(request)) {
                foundResponses.add(response);
            }
        }
        return foundResponses;
    }

    private StoredResponseSet getResponseByETag(byte[] etag) {
        StoredResponseSet foundResponses = new StoredResponseSet();

        for (StoredResponse storedResponse : responses) {
            if (BytesUtil.compareWithNull(storedResponse.etag(), etag) == 0) {
                foundResponses.add(storedResponse);
            }
        }
        return foundResponses;
    }

    public synchronized void invalidate() {
        for (StoredResponse response : responses) {
            reserveResourceToRemove(response);
        }
        responses.clear();
        if (childEntrys.isEmpty()) {
            if (parentEntry != null) {
                parentEntry.childEntrys.remove(this);
            }
        }
    }

    private void reserveResourceToRemove(StoredResponse response) {
        synchronized (this) {
            responses.remove(response);
        }

        manager.responseRemover().reserve(response);
    }

    public CacheManagerImp manager() {
        return manager;
    }

    public String path() {
        return path;
    }

    public File directoryFile() {
        return directoryFile;
    }

    @Override
    public void HandleTimerEvent(Object data, long time) {
        StoredResponse response = (StoredResponse) data;
        reserveResourceToRemove(response);
    }

    public void debug(StringBuffer sb) {
        sb.append("{")
                .append(path)
                .append(":")
                .append(responses.size())
                .append("=");
        for (CacheEntry child : childEntrys) {
            child.debug(sb);
        }
        sb.append("}");
    }
}
