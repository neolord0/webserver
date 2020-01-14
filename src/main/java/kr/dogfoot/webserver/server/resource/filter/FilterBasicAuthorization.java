package kr.dogfoot.webserver.server.resource.filter;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueAuthorization;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.util.http.HttpString;

import java.util.Base64;

public class FilterBasicAuthorization extends Filter {
    private String realmDescription;
    private String userName;
    private String password;

    public FilterBasicAuthorization() {
    }

    @Override
    public boolean inboundProcess(Context context, Server server) {
        if (isAuthenticated(context.request()) == false) {
            context.response(server.objects().responseMaker().new_401Unauthorized(HttpString.Basic_Auth, realmDescription));
            return false;
        }
        return true;
    }

    private boolean isAuthenticated(Request request) {
        HeaderValueAuthorization authorization = (HeaderValueAuthorization) request.getHeaderValueObj(HeaderSort.Authorization);
        return authorization != null && check(authorization);
    }

    private boolean check(HeaderValueAuthorization authorization) {
        if (authorization.type().equalsIgnoreCase(HttpString.Basic_Auth)) {
            if (authorization.credentials() != null) {
                String[] credentials = new String(Base64.getDecoder().decode(authorization.credentials())).split(":");
                if (credentials.length == 2) {
                    return userName.equals(credentials[0]) && password.equals(credentials[1]);
                }
            }
        }
        return false;
    }


    @Override
    public boolean outboundProcess(Context context, Server server) {
        return true;
    }

    @Override
    public FilterSort sort() {
        return FilterSort.BasicAuthorization;
    }

    public String realmDescription() {
        return realmDescription;
    }

    public void realmDescription(String realmDescription) {
        this.realmDescription = realmDescription;
    }

    public String userName() {
        return userName;
    }

    public void userName(String userName) {
        this.userName = userName;
    }

    public String password() {
        return password;
    }

    public void password(String password) {
        this.password = password;
    }
}
