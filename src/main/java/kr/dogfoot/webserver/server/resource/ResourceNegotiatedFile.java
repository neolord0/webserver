package kr.dogfoot.webserver.server.resource;

import kr.dogfoot.webserver.httpMessage.reply.Reply;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.server.host.HostObjects;
import kr.dogfoot.webserver.server.resource.look.LookResult;
import kr.dogfoot.webserver.server.resource.look.LookState;
import kr.dogfoot.webserver.server.resource.negotiation.NegotiationInfo;
import kr.dogfoot.webserver.server.resource.performer.NegoFilePerformer;
import kr.dogfoot.webserver.util.http.HttpString;

public class ResourceNegotiatedFile extends Resource {
    private ResourceDirectory parentDirectory;

    private NegotiationInfo negoInfo;

    public ResourceNegotiatedFile(ResourceDirectory parentDirectory) {
        this.parentDirectory = parentDirectory;
        this.negoInfo = new NegotiationInfo();
    }

    @Override
    public ResourceType type() {
        return ResourceType.NegotiationFile;
    }

    @Override
    public String path() {
        return negoInfo.filename();
    }

    @Override
    public String pathFromRoot() {
        if (parentDirectory.path() == null) {
            return HttpString.PathSeparator + path();
        } else {
            return parentDirectory.pathFromRoot() + HttpString.PathSeparator + path();
        }
    }

    @Override
    public boolean look(LookState ls, LookResult lr) {
        lr.appendFilter(filters());
        lr.resource(this);
        return true;
    }

    @Override
    protected boolean isMatched(String str) {
        return str != null && str.equalsIgnoreCase(path());
    }

    @Override
    public Reply perform(Request request, HostObjects hostObjects) {
        return NegoFilePerformer.perform(request, this, hostObjects);
    }

    public ResourceDirectory parentDirectory() {
        return parentDirectory;
    }

    public NegotiationInfo negoInfo() {
        return negoInfo;
    }
}
