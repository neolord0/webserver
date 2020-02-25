package kr.dogfoot.webserver;

import kr.dogfoot.webserver.loader.WebServiceLoader;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.util.ConfigFileLoader;
import kr.dogfoot.webserver.util.Message;

public class Main {
    private static final String Default_Config_File = "webserver.xml";

    public static void main(String[] args) throws Exception {
        Message.enableDebug(false);
        ConfigFileLoader.setConfigDirectory("/Users/neolord/WebServerHome/config");
        Server server = new Server();
        if (args.length > 1) {
            WebServiceLoader.fromConfigFile(args[0], server);
        } else {
            WebServiceLoader.fromConfigFile(Default_Config_File, server);
        }

        server.start();
    }
}
