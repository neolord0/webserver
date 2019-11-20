package kr.dogfoot.webserver.server.resource.look;

import kr.dogfoot.webserver.httpMessage.request.MethodType;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.server.host.Host;
import kr.dogfoot.webserver.server.resource.Resource;
import kr.dogfoot.webserver.server.resource.filter.*;

import java.util.ArrayList;

public class LookResult {
    private static final Filter[] Zero_Array = new Filter[0];
    private Request request;
    private Host host;
    private Resource resource;
    private FilterBasicAuthorization basicAuthorization;
    private FilterExpectChecking expectChecking;
    private FilterAllowedMethodAdding allowedMethod;
    private FilterURLRedirecting urlRedirecting;
    private ArrayList<FilterHeaderAdding> headerAddingList;
    private FilterCharsetEncoding charsetEncoding;
    private FilterContentEncoding contentEncoding;

    public LookResult(Request request, Host host) {
        this.request = request;
        this.host = host;

        setDefaultFilter(host);
    }

    private void setDefaultFilter(Host host) {
        allowedMethod = new FilterAllowedMethodAdding();
        for (MethodType mt : host.hostObjects().defaultAllowedMethods()) {
            allowedMethod.addAllowedMethod(mt);
        }
    }

    public Host host() {
        return host;
    }

    public Resource resource() {
        return resource;
    }

    public void resource(Resource resource) {
        this.resource = resource;
    }

    public Filter[] filters() {
        ArrayList<Filter> list = new ArrayList<Filter>();
        if (basicAuthorization != null) {
            list.add(basicAuthorization);
        }
        if (expectChecking != null) {
            list.add(expectChecking);
        }
        if (allowedMethod != null) {
            list.add(allowedMethod);
        }
        if (urlRedirecting != null) {
            list.add(urlRedirecting);
        }
        if (headerAddingList != null && headerAddingList.size() > 0) {
            list.addAll(headerAddingList);
        }
        if (charsetEncoding != null) {
            list.add(charsetEncoding);
        }
        if (contentEncoding != null) {
            list.add(contentEncoding);
        }
        return list.toArray(Zero_Array);
    }

    public void appendFilter(Filter[] filters) {
        for (Filter f : filters) {
            if (f != null) {
                apepndFilter(f);
            }
        }
    }

    private void apepndFilter(Filter filter) {
        switch (filter.sort()) {
            case BasicAuthorization:
                setBasicAuthorization((FilterBasicAuthorization) filter);
                break;
            case ExpectChecking:
                setExpectChecking((FilterExpectChecking) filter);
                break;
            case AllowedMethodAdding:
                addAllowedMethod((FilterAllowedMethodAdding) filter);
                break;
            case URLRedirecting:
                setURLRedirecting((FilterURLRedirecting) filter);
                break;
            case HeaderAdding:
                setHeaderAdding((FilterHeaderAdding) filter);
                break;
            case CharsetEncoding:
                setCharsetEncoding((FilterCharsetEncoding) filter);
                break;
            case ContentEncoding:
                setContentEncoding((FilterContentEncoding) filter);
                break;
        }
    }

    private void setBasicAuthorization(FilterBasicAuthorization filter) {
        basicAuthorization = filter;
    }

    private void setExpectChecking(FilterExpectChecking filter) {
        expectChecking = filter;
    }

    private void addAllowedMethod(FilterAllowedMethodAdding filter) {
        for (MethodType mt : filter.allowedMethods()) {
            if (mt != null) {
                allowedMethod.addAllowedMethod(mt);
            }
        }
    }

    private void setURLRedirecting(FilterURLRedirecting filter) {
        urlRedirecting = filter;
    }

    private void setHeaderAdding(FilterHeaderAdding filter) {
        if (headerAddingList == null) {
            headerAddingList = new ArrayList<FilterHeaderAdding>();
        }
        headerAddingList.add(filter);
    }

    private void setCharsetEncoding(FilterCharsetEncoding filter) {
        if (charsetEncoding == null || filter.isMoreAppropriate(charsetEncoding, request)) {
            charsetEncoding = filter;
        }
    }

    private void setContentEncoding(FilterContentEncoding filter) {
        if (contentEncoding == null || filter.isMoreAppropriate(contentEncoding, request)) {
            contentEncoding = filter;
        }
    }
}
