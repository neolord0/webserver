package kr.dogfoot.webserver.server.cache;

import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.httpMessage.response.Response;
import kr.dogfoot.webserver.server.host.proxy_info.BackendServerInfo;
import kr.dogfoot.webserver.server.host.proxy_info.CacheOption;

public class CacheManagerNull implements CacheManager {
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
    public void replace(StoredResponse storedResponse, Request originalRequest, Response response, CacheOption cacheOption) {
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
    public void update(StoredResponse storedResponse, Request originalRequest, Response response, CacheOption cacheOption) {
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
