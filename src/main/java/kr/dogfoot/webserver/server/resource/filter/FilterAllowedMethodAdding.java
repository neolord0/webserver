package kr.dogfoot.webserver.server.resource.filter;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueAllow;
import kr.dogfoot.webserver.httpMessage.reply.ReplyCode;
import kr.dogfoot.webserver.httpMessage.request.MethodType;
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
        if (includedAllowedMethod(context.request().method()) == false) {
            context.reply(server.objects().replyMaker().new_405MethodNotAllowed(context.request(), allowedMethods()));
            return false;
        }
        return true;
    }

    @Override
    public boolean outboundProcess(Context context, Server server) {
        if (context.request().method() == MethodType.HEAD && context.reply().code() == ReplyCode.Code200) {
            context.reply().addHeader(HeaderSort.Allow, allowHeaderValue());
        }
        return true;
    }

    private byte[] allowHeaderValue() {
        HeaderValueAllow allow = new HeaderValueAllow();
        for (MethodType mt : allowedMethodList) {
            allow.addMethodType(mt);
        }
        return allow.combineValue();
    }

    @Override
    public FilterSort sort() {
        return FilterSort.AllowedMethodAdding;
    }

    public void addAllowedMethod(MethodType mt) {
        if (includedAllowedMethod(mt) == false) {
            allowedMethodList.add(mt);
        }
    }

    private boolean includedAllowedMethod(MethodType mt) {
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
