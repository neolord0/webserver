package kr.dogfoot.webserver.server.resource.look;

import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.util.http.HttpString;

public class LookState {
    private String[] pathItems;
    private int pathItemIndex;

    public LookState(Request request) {
        splitPathItems(request.requestURI().path());
    }

    public LookState(String url) {
        splitPathItems(url);
    }


    private void splitPathItems(String url) {
        if (url.charAt(0) == HttpString.Slash) {
            url = url.substring(1);
        }
        pathItems = url.split(HttpString.PathSeparator);
        pathItemIndex = 0;
    }

    public String getNextPathItem() {
        if (pathItems != null && pathItems.length > pathItemIndex) {
            return pathItems[pathItemIndex++];
        }
        return null;
    }

    public boolean isLastPathItem() {
        return pathItems.length == pathItemIndex;
    }
}
