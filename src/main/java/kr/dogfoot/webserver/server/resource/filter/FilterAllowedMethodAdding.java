package kr.dogfoot.webserver.server.resource.filter;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.httpMessage.request.MethodType;
import kr.dogfoot.webserver.httpMessage.response.StatusCode;
import kr.dogfoot.webserver.httpMessage.util.ResponseSetter;
import kr.dogfoot.webserver.server.Server;

import java.util.ArrayList;

public class FilterAllowedMethodAdding extends Filter {
    private static final MethodType[] Zero_Array = new MethodType[0];
    private ArrayList<MethodType> allowedMethodList;

    public FilterAllowedMethodAdding() {
        allowedMethodList = new ArrayList<MethodType>();
    }

    @Override
    public boolean inboundProcess(Context context, Server server) {
        if (isIncludedAllowedMethod(context.request().method()) == false) {
            context.response(server.objects().responseMaker().new_405MethodNotAllowed(context.request(), allowedMethods()));
            return false;
        }
        return true;
    }

    @Override
    public boolean outboundProcess(Context context, Server server) {
        if (context.request().method() == MethodType.HEAD && context.response().statusCode() == StatusCode.Code200) {
            ResponseSetter.setAllow(context.response(), allowedMethodList);
        }
        return true;
    }

    @Override
    public FilterSort sort() {
        return FilterSort.AllowedMethodAdding;
    }

    public void addAllowedMethod(MethodType mt) {
        if (isIncludedAllowedMethod(mt) == false) {
            allowedMethodList.add(mt);
        }
    }

    private boolean isIncludedAllowedMethod(MethodType mt) {
        for (MethodType mt2 : allowedMethodList) {
            if (mt2 == mt) {
                return true;
            }
        }
        return false;
    }

    public MethodType[] allowedMethods() {
        return allowedMethodList.toArray(Zero_Array);
    }
}
