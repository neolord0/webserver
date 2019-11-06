package kr.dogfoot.webserver.server.host;

import kr.dogfoot.webserver.httpMessage.reply.ReplyMaker;
import kr.dogfoot.webserver.httpMessage.request.MethodType;
import kr.dogfoot.webserver.server.object.ServerObjects;
import kr.dogfoot.webserver.server.object.ServerProperties;

public class HostObjects {
    private ServerObjects serverObjects;

    private MediaTypeManager mediaTypeManager;
    private String defaultCharset;
    private MethodType[] defaultAllowedMethods;

    public HostObjects(ServerObjects serverObjects) {
        this.serverObjects = serverObjects;
        mediaTypeManager = new MediaTypeManager(serverObjects.defaultMediaTypeManager());

        defaultAllowedMethods = new MethodType[]{MethodType.GET, MethodType.HEAD};
    }

    public ReplyMaker replyMaker() {
        return serverObjects.replyMaker();
    }

    public ServerProperties serverProperties() {
        return serverObjects.properties();
    }

    public MediaTypeManager mediaTypeManager() {
        return mediaTypeManager;
    }

    public String defaultCharset() {
        return defaultCharset;
    }

    public void defaultCharset(String defaultCharset) {
        this.defaultCharset = defaultCharset;
    }

    public MethodType[] defaultAllowedMethods() {
        return defaultAllowedMethods;
    }

    public void defaultAllowedMethods(MethodType[] defaultAllowedMethods) {
        this.defaultAllowedMethods = defaultAllowedMethods;
    }
}
