package kr.dogfoot.webserver.loader.resourcesetting;

import kr.dogfoot.webserver.server.resource.filter.Filter;
import kr.dogfoot.webserver.util.http.HttpString;

import java.util.ArrayList;

public abstract class SettingItem {
    private static final Filter[] Zero_Array = new Filter[0];
    protected SettingItem parentItem;
    protected String url;
    protected ArrayList<Filter> filters;

    protected SettingItem() {
        this.url = "/";
        parentItem = null;
        filters = new ArrayList<Filter>();
    }

    public SettingItem parentItem() {
        return parentItem;
    }

    public void parentItem(SettingItem parentItem) {
        this.parentItem = parentItem;
    }

    public String url() {
        return url;
    }

    public void url(String url) {
        if (parentItem == null) {
            if (url == null || url.length() == 0) {
                this.url = "/";
            } else {
                if (url.charAt(0) == HttpString.Slash) {
                    this.url = url;
                } else {
                    this.url = "/" + url;
                }
            }
        } else {
            if (url.charAt(0) == HttpString.Slash) {
                this.url = url.substring(1);
            } else {
                this.url = url;
            }
        }
    }

    public void addFilter(Filter f) {
        filters.add(f);
    }

    public Filter[] filters() {
        return filters.toArray(Zero_Array);
    }

    public abstract SettingType type();

    public String urlFromRoot() {
        if (parentItem == null) {
            return url;
        } else {
            if (parentItem.url.equals(HttpString.PathSeparator)) {
                return HttpString.PathSeparator + url;
            } else {
                return parentItem.urlFromRoot() + HttpString.PathSeparator + url;
            }
        }
    }
}
