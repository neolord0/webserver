package kr.dogfoot.webserver.server.host.proxy_info;

public class CacheOption {
    private boolean use;
    private long defaultExpires;

    public CacheOption() {
        use = false;
        defaultExpires = -1;
    }

    public boolean use() {
        return use;
    }

    public void use(boolean use) {
        this.use = use;
    }

    public long defaultExpires() {
        return defaultExpires;
    }

    public void defaultExpires(long defaultExpires) {
        this.defaultExpires = defaultExpires;
    }
}
