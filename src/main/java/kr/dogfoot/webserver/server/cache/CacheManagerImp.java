package kr.dogfoot.webserver.server.cache;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueContentLocation;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueLocation;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.CacheDirectiveSort;
import kr.dogfoot.webserver.httpMessage.request.MethodType;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.httpMessage.request.URI;
import kr.dogfoot.webserver.httpMessage.response.EachRangePart;
import kr.dogfoot.webserver.httpMessage.response.Response;
import kr.dogfoot.webserver.httpMessage.response.StatusCode;
import kr.dogfoot.webserver.server.host.proxy_info.BackendServerInfo;
import kr.dogfoot.webserver.server.host.proxy_info.CacheOption;
import kr.dogfoot.webserver.server.resource.look.LookState;
import kr.dogfoot.webserver.server.timer.Timer;
import kr.dogfoot.webserver.util.FileUtil;
import kr.dogfoot.webserver.util.Message;

import java.io.File;
import java.util.LinkedList;

public class CacheManagerImp implements CacheManager {
    private File storagePathFile;
    private long inactiveTimeout;

    private Timer timer;
    private SizeLimiter sizeLimiter;
    private ResponseRemover responseRemoverForInactive;

    private LinkedList<CacheHost> hosts;

    public CacheManagerImp(Timer timer) {
        storagePathFile = null;
        inactiveTimeout = 30;

        this.timer = timer;
        sizeLimiter = new SizeLimiter();
        responseRemoverForInactive = new ResponseRemover(timer, this);

        hosts = new LinkedList<CacheHost>();
    }

    @Override
    public void start() {
        CacheLoader.load(storagePathFile, this);
        responseRemoverForInactive.start();
    }

    @Override
    public void terminate() {
        responseRemoverForInactive.terminate();
    }

    @Override
    public boolean canStore(Request request, Response response) {
        if (sizeLimiter.isOverflow(response.contentLength())) {
            Message.debug("Cache is full, so response can't store");
            return false;
        }

        if (request.method() == MethodType.GET
                && (response.statusCode() == StatusCode.Code200 || response.statusCode() == StatusCode.Code206)
                && !request.hasCacheDirective(CacheDirectiveSort.NoStore)
                && !response.hasCacheDirective(CacheDirectiveSort.NoStore)
                && !response.hasCacheDirective(CacheDirectiveSort.Private)
                && canStoreForAuthorization(request, response)) {
            return true;
            /*
            if (response.hasHeader(HeaderSort.Expires)
                    || response.hasCacheDirective(CacheDirectiveSort.MaxAge)
                    || response.hasCacheDirective(CacheDirectiveSort.SMaxAge)
                    || response.hasHeader(HeaderSort.Last_Modified)
                    || response.hasCacheDirective(CacheDirectiveSort.Public)) {
                return true;
            }
             */
        }
        return false;
    }


    private boolean canStoreForAuthorization(Request request, Response response) {
        if (!request.hasHeader(HeaderSort.Authorization)) {
            return true;
        } else {
            if (response.hasCacheDirective(CacheDirectiveSort.MustRevalidate)
                    || response.hasCacheDirective(CacheDirectiveSort.Public)
                    || response.hasCacheDirective(CacheDirectiveSort.SMaxAge)) {
                return true;
            }
            return false;
        }
    }

    @Override
    public StoredResponse store(BackendServerInfo backendServerInfo, Request request, Response response) {
        CacheEntry entry = getEntry(backendServerInfo, request.requestURI(), true);

        StoredResponse newResponse = entry.addNewResponse();
        newResponse.set(request, response, backendServerInfo.cacheOption());
        newResponse.storeToFile();
        return newResponse;
    }

    @Override
    public void replace(StoredResponse storedResponse, Request request, Response response, CacheOption cacheOption) {
        storedResponse.deleteFile(true);
        storedResponse.set(request, response, cacheOption);
        storedResponse.storeToFile();
    }

    @Override
    public SelectedResourceInfo select(BackendServerInfo backendServerInfo, Request request) {
        if (request.method() != MethodType.GET) {
            return SelectedResourceInfo.nullObject();
        }
        CacheEntry entry = getEntry(backendServerInfo, request.requestURI(), false);
        if (entry != null) {
            return SelectedResourceInfo.select(entry, request);
        } else {
            return SelectedResourceInfo.nullObject();
        }
    }

    private CacheEntry getEntry(BackendServerInfo backendServerInfo, URI uri, boolean createChildItem) {
        CacheHost host = getHost(backendServerInfo, createChildItem);
        if (host == null) {
            return null;
        }

        LookState ls = new LookState(uri);
        return host.getEntry(ls, createChildItem);
    }

    private CacheHost getHost(BackendServerInfo backendServerInfo, boolean createChildItem) {
        CacheHost host = findHost(backendServerInfo);
        if (host == null && createChildItem == true) {
            host= new CacheHost(this, backendServerInfo);
            addHost(host);
        }
        return host;
    }

    private synchronized CacheHost findHost(BackendServerInfo backendServerInfo) {
        for (CacheHost host : hosts) {
            if (host.isMatch(backendServerInfo)) {
                return host;
            }
        }
        return null;
    }

    public synchronized void addHost(CacheHost host) {
        hosts.add(host);
    }

    @Override
    public boolean canUpdate(Request request, Response response) {
        if (response.statusCode() == StatusCode.Code304
                || (request.method() == MethodType.HEAD
                && response.statusCode() == StatusCode.Code200
                && response.hasContentLength())) {
            return true;
        }
        return false;
    }

    @Override
    public void update(StoredResponse storedResponse, Request request, Response response, CacheOption cacheOption) {
        storedResponse.deleteFile(false);
        storedResponse.update(request, response, cacheOption);
        storedResponse.storeToFile();
    }

    @Override
    public StoredResponse getResponse(BackendServerInfo backendServerInfo, Request request, Response response) {
        CacheEntry entry = getEntry(backendServerInfo, request.requestURI(), false);
        if (entry == null) {
            return null;
        }
        return entry.getResponseByValidator(response);
    }

    @Override
    public boolean canInvalidate(Request request, Response response) {
        if (request.isSafe() == false && response.isError() == false) {
            return true;
        }
        return false;
    }

    @Override
    public void invalidate(BackendServerInfo backendServerInfo, Request request, Response response) {
        invalidateByRequestURI(backendServerInfo, request);
        invalidateByLocation(backendServerInfo, request, response);
        invalidateByContentLocation(backendServerInfo, request, response);
    }

    private void invalidateByRequestURI(BackendServerInfo backendServerInfo, Request request) {
        invalidateByURI(backendServerInfo, request.requestURI());
    }

    private void invalidateByURI(BackendServerInfo backendServerInfo, URI uri) {
        CacheEntry entry = getEntry(backendServerInfo, uri, false);
        if (entry != null) {
            entry.invalidate();
        }
    }

    private void invalidateByLocation(BackendServerInfo backendServerInfo, Request request, Response response) {
        HeaderValueLocation location = (HeaderValueLocation) response.getHeaderValueObj(HeaderSort.Location);
        if (location != null && location.uriObject().isSameHost(request.requestURI())) {
            invalidateByURI(backendServerInfo, location.uriObject());
        }
    }

    private void invalidateByContentLocation(BackendServerInfo backendServerInfo, Request request, Response response) {
        invalidateByContentLocationForEachRangePart(backendServerInfo, request, response);
        int count = response.rangePartCount();
        for (int index = 0; index < count; index++) {
            EachRangePart part = response.rangePart(index);
            if (part != null) {
                invalidateByContentLocationForEachRangePart(backendServerInfo, request, part);
            }
        }
    }

    private void invalidateByContentLocationForEachRangePart(BackendServerInfo backendServerInfo, Request request, EachRangePart rangePart) {
        HeaderValueContentLocation contentLocation = (HeaderValueContentLocation) rangePart.getHeaderValueObj(HeaderSort.Location);
        if (contentLocation != null && contentLocation.uriObject().isSameHost(request.requestURI())) {
            invalidateByURI(backendServerInfo, contentLocation.uriObject());
        }
    }

    public File storagePathFile() {
        return storagePathFile;
    }

    public void storagePath(String storagePath) {
        storagePathFile = FileUtil.openDirectory(storagePath);
    }

    public long inactiveTimeout() {
        return inactiveTimeout;
    }

    public void inactiveTimeout(long inactiveTimeout) {
        this.inactiveTimeout = inactiveTimeout;
    }

    public Timer timer() {
        return timer;
    }

    public SizeLimiter sizeLimiter() {
        return sizeLimiter;
    }

    public ResponseRemover responseRemover() {
        return responseRemoverForInactive;
    }
}
