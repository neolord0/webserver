package kr.dogfoot.webserver.server.cache;

import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.httpMessage.response.Response;
import kr.dogfoot.webserver.server.host.proxy_info.BackendServerInfo;

public class NullCacheManagerImp implements CacheManager {
    @Override
    public void start() {
    }

    @Override
    public void terminate() {
    }

    @Override
    public boolean canStore(Request request, Response response) {
        return false;
    }

    @Override
    public StoredResponse store(BackendServerInfo backendServerInfo, Request request, Response response) {
        return null;
    }

    @Override
    public SelectedResourceInfo select(BackendServerInfo backendServerInfo, Request request) {
        return null;
    }

    @Override
    public boolean canUpdate(Request request, Response response) {
        return false;
    }

    @Override
    public StoredResponse getResponse(BackendServerInfo backendServerInfo, Request request, Response response) {
        return null;
    }

    @Override
    public boolean canInvalidate(Request request, Response response) {
        return false;
    }

    @Override
    public void invalidate(BackendServerInfo backendServerInfo, Request request, Response response) {
    }
}
