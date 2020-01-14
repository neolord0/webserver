package kr.dogfoot.webserver.server.host.proxy_info.filter;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueProxyAuthorization;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.util.http.HttpString;

import java.util.Base64;

public class ProxyFilterBasicAuthorization extends ProxyFilter {
    private String realmDescription;
    private String userName;
    private String password;

    @Override
    public boolean inboundProcess(Context context, Server server) {
        if (isAuthenticated(context.request()) == false) {
            context.response(server.objects().responseMaker().new_407Unauthorized(HttpString.Basic_Auth, realmDescription));
            return false;
        }
        return true;
    }

    private boolean isAuthenticated(Request request) {
        HeaderValueProxyAuthorization proxyAuthorization = (HeaderValueProxyAuthorization) request.getHeaderValueObj(HeaderSort.Proxy_Authorization);
        return proxyAuthorization != null && check(proxyAuthorization);
    }

    private boolean check(HeaderValueProxyAuthorization proxyAuthorization) {
        if (proxyAuthorization.type().equalsIgnoreCase(HttpString.Basic_Auth)) {
            if (proxyAuthorization.credentials() != null) {
                String[] credentials = new String(Base64.getDecoder().decode(proxyAuthorization.credentials())).split(":");
                if (credentials.length == 2) {
                    return userName.equals(credentials[0]) && password.equals(credentials[1]);
                }
            }
        }
        return false;
    }

    @Override
    public boolean outboundProcess(Context context, Server server) {
        return false;
    }

    @Override
    public ProxyFilterSort sort() {
        return null;
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
