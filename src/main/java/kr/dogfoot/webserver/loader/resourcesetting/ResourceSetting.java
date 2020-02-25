package kr.dogfoot.webserver.loader.resourcesetting;

import kr.dogfoot.webserver.server.resource.filter.Filter;

import java.util.ArrayList;

public class ResourceSetting {
    private static final SettingItem[] Zero_Array = new SettingItem[0];

    private ArrayList<SettingItem> settings;

    public ResourceSetting() {
        settings = new ArrayList<SettingItem>();
    }

    public FileSetting newFileSetting() {
        FileSetting fs = new FileSetting();
        settings.add(fs);
        return fs;
    }

    public DirectorySetting newDirectorySetting() {
        DirectorySetting ds = new DirectorySetting();
        settings.add(ds);
        return ds;
    }

    public VirtualDirectorySetting newVirtualDirectorySetting() {
        VirtualDirectorySetting vds = new VirtualDirectorySetting();
        settings.add(vds);
        return vds;
    }

    public SettingItem[] settingItems() {
        return settings.toArray(Zero_Array);
    }

    public Filter[] findFilters(String url, SettingType type) {
        FindParameter fp = new FindParameter().readyForFindingFilter(url, type);

        for (SettingItem item : settings) {
            if (item.type() == type &&
                    item.urlFromRoot().equalsIgnoreCase(url)) {
                fp.resultForFilter.addAll(item.filters);
            } else {
                if (item.type() == SettingType.Directory) {
                    ((DirectorySetting) item).findFilter(fp);
                }
            }
        }
        return fp.resultForFilter.toArray(new Filter[0]);
    }

    public String[] findNotServiceFilePattern(String url) {
        FindParameter fp = new FindParameter().readyForNotServiceFilenamePattern(url);
        for (SettingItem item : settings) {
            if (item.type() == SettingType.Directory || item.type() == SettingType.VirtualDirectory) {
                DirectorySetting ds = (DirectorySetting) item;
                String urlToRoot = ds.urlFromRoot();
                if (urlToRoot.equalsIgnoreCase(fp.url)) {
                    fp.addNotServiceFilePatternALL(ds);
                } else if (ds.isChildURL(fp.url)) {
                    fp.addNotServiceFilePatternOnlyInherited(ds);
                    ds.findNotServiceFilePattern(fp);
                }
            }
        }
        return fp.resultForNotServiceFilenamePattern.toArray(new String[0]);
    }

    public VirtualDirectorySetting[] findVirtualDirectorySettings(String url) {
        FindParameter fp = new FindParameter().readyForVirtualDirectorySetting(url);
        for (SettingItem item : settings) {
            if (item.type() == SettingType.VirtualDirectory) {
                VirtualDirectorySetting vds = (VirtualDirectorySetting) item;
                String parentURLFromRoot;
                if (item.parentItem() == null) {
                    parentURLFromRoot = "/";
                } else {
                    parentURLFromRoot = item.parentItem().urlFromRoot();
                }
                if (fp.url.equalsIgnoreCase(parentURLFromRoot)) {
                    fp.resultForVirtualDirectorySetting.add((VirtualDirectorySetting) item);
                } else {
                    vds.findVirtualDirectorySettings(fp);
                }
            } else if (item.type() == SettingType.Directory) {
                DirectorySetting ds = (DirectorySetting) item;
                ds.findVirtualDirectorySettings(fp);
            }
        }
        return fp.resultForVirtualDirectorySetting.toArray(new VirtualDirectorySetting[0]);
    }


    protected class FindParameter {
        String url;
        SettingType type;
        ArrayList<Filter> resultForFilter;
        ArrayList<VirtualDirectorySetting> resultForVirtualDirectorySetting;
        ArrayList<String> resultForNotServiceFilenamePattern;

        public FindParameter() {
        }

        public FindParameter readyForFindingFilter(String url, SettingType type) {
            this.url = url;
            this.type = type;
            resultForFilter = new ArrayList<Filter>();
            return this;
        }

        public FindParameter readyForNotServiceFilenamePattern(String url) {
            this.url = url;
            resultForNotServiceFilenamePattern = new ArrayList<String>();
            return this;
        }

        public FindParameter readyForVirtualDirectorySetting(String url) {
            this.url = url;
            resultForVirtualDirectorySetting = new ArrayList<VirtualDirectorySetting>();
            return this;
        }


        public void addNotServiceFilePatternALL(DirectorySetting ds) {
            for (NotServicedFile nsr : ds.notServicedFiles()) {
                resultForNotServiceFilenamePattern.add(nsr.namePatten());
            }
        }

        public void addNotServiceFilePatternOnlyInherited(DirectorySetting ds) {
            for (NotServicedFile nsr : ds.notServicedFiles()) {
                if (nsr.isInheritable()) {
                    resultForNotServiceFilenamePattern.add(nsr.namePatten());
                }
            }
        }
    }
}
