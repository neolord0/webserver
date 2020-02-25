package kr.dogfoot.webserver.server.host.proxy_info.filter;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.server.Server;

public abstract class ProxyFilter {
    public abstract boolean inboundProcess(Context context, Server server);

    public abstract boolean outboundProcess(Context context, Server server);

    public abstract ProxyFilterSort sort();
}
