package kr.dogfoot.webserver.server.cache;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueETag;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.httpMessage.response.Response;
import kr.dogfoot.webserver.server.resource.look.LookState;
import kr.dogfoot.webserver.server.timer.TimerEventHandler;
import kr.dogfoot.webserver.util.FileUtil;
import kr.dogfoot.webserver.util.bytes.BytesUtil;

import java.io.File;
import java.util.LinkedList;

public class CacheEntry implements TimerEventHandler {
    private CacheManagerImp manager;
    private CacheEntry parentEntry;

    private String path;
    private File directoryFile;

    private LinkedList<CacheEntry> childEntrys;
    private LinkedList<StoredResponse> responses;

    public CacheEntry(CacheManagerImp manager) {
        this.manager = manager;

        childEntrys = new LinkedList<CacheEntry>();
        responses = new LinkedList<StoredResponse>();
    }

    public CacheEntry openPath(File parentPathFile, String  path) {
        this.path = path;
        directoryFile = FileUtil.openDirectory(parentPathFile, path);
        return this;
    }

    public CacheEntry find(LookState ls, boolean createChildItem) {
        String path = ls.getNextPathItem();

        CacheEntry childEntry = find(path);
        if (childEntry != null) {
            if (ls.isLastPathItem() == false) {
                return childEntry.find(ls, createChildItem);
            } else {
                return childEntry;
            }
        }

        if (createChildItem == false) {
            return null;
        }

        if (ls.isLastPathItem() == false) {
            return addNewEntry(path).find(ls, createChildItem);
        } else {
            return addNewEntry(path);
        }
    }

    public CacheEntry find(String path) {
        synchronized (childEntrys) {
            for (CacheEntry childEntry : childEntrys) {
                if (childEntry.path.equals(path)) {
                    return childEntry;
                }
            }
        }
        return null;
    }

    private CacheEntry addNewEntry(String path) {
        CacheEntry newEntry = new CacheEntry(manager)
                .openPath(this.directoryFile, path);
        addEntry(newEntry);
        return newEntry;
    }

    public void addEntry(CacheEntry entry) {
        synchronized (childEntrys) {
            entry.parentEntry = this;
            childEntrys.add(entry);
        }
    }

    public StoredResponse addNewResponse() {
        StoredResponse newResponse = StoredResponse.pooledObject(this);
        addResponse(newResponse);
        return newResponse;
    }

    public void addResponse(StoredResponse response) {
        synchronized (responses) {
            responses.add(response);
        }
        response.setTimerForInactiveTimeout();
    }

    public StoredResponseSet getResponseByMethodAndSelectedFields(Request request) {
        StoredResponseSet foundResponses = new StoredResponseSet();

        synchronized (responses) {
            for (StoredResponse response : responses) {
                if (response.isMatchMethodAndSelectedFields(request)) {
                    foundResponses.add(response);
                }
            }
        }

        return foundResponses;
    }

    public StoredResponse getResponseByValidator(Response response) {
        if (response.hasValidator() == false) {
            StoredResponse item = getResourceWhenOne();
            if (item != null && item.response().hasValidator() == false) {
                return item;
            }
        } else {
            HeaderValueETag eTag = (HeaderValueETag) response.getHeaderValueObj(HeaderSort.ETag);
            StoredResponseSet foundResponses = getResponseByETag(eTag.etag());
            if (foundResponses.size() > 0) {
                return foundResponses.first();
            }
        }
        return null;
    }

    private StoredResponse getResourceWhenOne() {
        synchronized (responses) {
            if (responses.size() == 1) {
                return responses.getFirst();
            }
        }
        return null;
    }

    private StoredResponseSet getResponseByETag(byte[] etag) {
        StoredResponseSet foundResponses = new StoredResponseSet();
        synchronized (responses) {
            for (StoredResponse storedResponse : responses) {
                if (BytesUtil.compareWithNull(storedResponse.etag(), etag) == 0) {
                    foundResponses.add(storedResponse);
                }
            }
        }
        return foundResponses;
    }

    public void invalidate() {
        removeAllResponse();

        removeThisEntry();
    }

    private void removeAllResponse() {
        synchronized (responses) {
            for (StoredResponse response : responses) {
                response.unsetTimerForInactiveTimeout();
                manager.responseRemover().reserve(response);
            }
            responses.clear();
        }
    }

    private void removeThisEntry() {
        synchronized (childEntrys) {
            if (childEntrys.isEmpty() && parentEntry != null) {
                parentEntry.removeEntry(this);
            }
        }
    }

    private void removeEntry(CacheEntry entry) {
        synchronized (childEntrys) {
            childEntrys.remove(entry);
        }
    }

    public CacheManagerImp manager() {
        return manager;
    }

    public File directoryFile() {
        return directoryFile;
    }

    @Override
    public void handleTimerEvent(Object data, long time) {
        StoredResponse response = (StoredResponse) data;
        removeResponse(response);

        manager.responseRemover().reserve(response);
    }

    private void removeResponse(StoredResponse response) {
        synchronized (responses) {
            responses.remove(response);
        }
    }
}
