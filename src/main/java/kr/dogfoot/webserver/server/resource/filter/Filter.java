package kr.dogfoot.webserver.server.resource.filter;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.server.resource.Resource;

public abstract class Filter {
    private Resource resource;

    public abstract boolean inboundProcess(Context context, Server server);

    public abstract boolean outboundProcess(Context context, Server server);

    public abstract FilterSort sort();

    public Resource resource() {
        return resource;
    }

    public void resource(Resource resource) {
        this.resource = resource;
    }
}

