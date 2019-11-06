package kr.dogfoot.webserver.server.host.proxy_info;

import kr.dogfoot.webserver.util.http.HttpString;

public enum Protocol {
    None,
    Ajp13,
    Http,
    Https;

    public static Protocol fromString(String protocol) {
        if (HttpString.Ajp_1_3.equalsIgnoreCase(protocol)) {
            return Ajp13;
        } else if (HttpString.Http.equalsIgnoreCase(protocol)) {
            return Http;
        } else if (HttpString.Https.equalsIgnoreCase(protocol)) {
            return Https;
        } else {
            return None;
        }
    }


    public String toStringWithPostFix() {
        if (this == Http) {
            return HttpString.HttpWithPostFix;
        } else if (this == Https) {
            return HttpString.HttpsWithPostFix;
        }
        return null;
    }
}
