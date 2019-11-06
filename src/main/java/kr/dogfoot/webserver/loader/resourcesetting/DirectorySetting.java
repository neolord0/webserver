package kr.dogfoot.webserver.loader.resourcesetting;

import kr.dogfoot.webserver.util.http.HttpString;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;

import java.util.ArrayList;

public class DirectorySetting extends SettingItem {
    private static final NotServicedFile[] Zero_Array = new NotServicedFile[0];
    private String defaultFile;
    private ArrayList<NotServicedFile> notServicedFiles;
    private ArrayList<SettingItem> childItems;

    public DirectorySetting() {
        notServicedFiles = new ArrayList<NotServicedFile>();
        childItems = new ArrayList<SettingItem>();
    }

    public boolean isChildURL(String url) {
        String urlPattern = urlFromRoot();
        if (urlPattern.endsWith(HttpString.Slash_String)) {
            urlPattern += "*";
        } else {
            urlPattern += "/*";
        }
        return FilenameUtils.wildcardMatch(url, urlPattern, IOCase.INSENSITIVE);
    }

    public String defaultFile() {
        return defaultFile;
    }

    public void defaultFile(String defaultFile) {
        this.defaultFile = defaultFile;
    }

    public NotServicedFile newNotServicedFile() {
        NotServicedFile nsf = new NotServicedFile();
        notServicedFiles.add(nsf);
        return nsf;
    }

    public NotServicedFile[] notServicedFiles() {
        return notServicedFiles.toArray(Zero_Array);
    }

    public FileSetting newChildFileSetting() {
        FileSetting fs = new FileSetting();
        fs.parentItem(this);
        childItems.add(fs);
        return fs;
    }

    public DirectorySetting newChildDirectorySetting() {
        DirectorySetting ds = new DirectorySetting();
        ds.parentItem(this);
        childItems.add(ds);
        return ds;
    }

    public VirtualDirectorySetting newChildVirtualDirectorySetting() {
        VirtualDirectorySetting vds = new VirtualDirectorySetting();
        vds.parentItem(this);
        childItems.add(vds);
        return vds;
    }

    @Override
    public SettingType type() {
        return SettingType.Directory;
    }

    public void findFilter(ResourceSetting.FindParameter fp) {
        for (SettingItem item : childItems) {
            if (item.type() == fp.type &&
                    item.urlFromRoot().equalsIgnoreCase(fp.url)) {
                fp.resultForFilter.addAll(item.filters);
            } else {
                if (item.type() == SettingType.Directory) {
                    ((DirectorySetting) item).findFilter(fp);
                }
            }
        }
    }

    public void findNotServiceFilePattern(ResourceSetting.FindParameter fp) {
        for (SettingItem item : childItems) {
            if (item.type() == SettingType.Directory && item.type() == SettingType.VirtualDirectory) {
                DirectorySetting ds = (DirectorySetting) item;
                String urlToRoot = item.urlFromRoot();
                if (urlToRoot.equalsIgnoreCase(fp.url)) {
                    fp.addNotServiceFilePatternALL(ds);
                } else if (ds.isChildURL(fp.url)) {
                    fp.addNotServiceFilePatternOnlyInherited(ds);
                    ds.findNotServiceFilePattern(fp);
                }
            }
        }
    }

    public void findVirtualDirectorySettings(ResourceSetting.FindParameter fp) {
        for (SettingItem item : childItems) {
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
    }

}
