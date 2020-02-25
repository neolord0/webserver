package kr.dogfoot.webserver.server.cache;

import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.httpMessage.response.Response;
import kr.dogfoot.webserver.server.host.proxy_info.BackendServerInfo;

public interface CacheManager {
    void start();

    void terminate();

    boolean canStore(Request request, Response response);

    StoredResponse store(BackendServerInfo backendServerInfo, Request request, Response response);

    SelectedResourceInfo select(BackendServerInfo backendServerInfo, Request request);

    boolean canUpdate(Request request, Response response);

    StoredResponse getResponse(BackendServerInfo backendServerInfo, Request request, Response response);

    boolean canInvalidate(Request request, Response response);

    void invalidate(BackendServerInfo backendServerInfo, Request request, Response response);
}
