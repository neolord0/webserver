package kr.dogfoot.webserver.server.resource;

import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.httpMessage.response.Response;
import kr.dogfoot.webserver.server.host.HostObjects;
import kr.dogfoot.webserver.server.resource.filter.Filter;
import kr.dogfoot.webserver.server.resource.look.LookResult;
import kr.dogfoot.webserver.server.resource.look.LookState;

import java.io.File;

public abstract class Resource {
    private static final int Default_Filter_Array_Length = 5;

    private Filter[] filters;
    private int filterCount;

    protected Resource() {
        filters = new Filter[Default_Filter_Array_Length];
        filterCount = 0;
    }

    public abstract String name();

    public Filter[] filters() {
        return filters;
    }

    public void addFilter(Filter filter) {
        if (filters.length <= filterCount) {
            Filter[] newArray = new Filter[filters.length * 2];
            System.arraycopy(filters, 0, newArray, 0, filters.length);
            filters = newArray;
        }

        filters[filterCount++] = filter;
        filter.resource(this);
    }

    public abstract ResourceType type();

    public abstract String path();

    public String pathFromRoot() {
        return null;
    }

    public boolean look(LookState ls, LookResult lr) {
        return false;
    }

    protected boolean isMatched(String str) {
        return false;
    }

    public File file() {
        return null;
    }

    public Response perform(Request request, HostObjects hostObjects) {
        return null;
    }
}