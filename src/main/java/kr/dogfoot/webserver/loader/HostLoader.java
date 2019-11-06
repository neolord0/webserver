package kr.dogfoot.webserver.loader;

import kr.dogfoot.webserver.loader.resourcesetting.ResourceSetting;
import kr.dogfoot.webserver.loader.resourcesetting.SettingType;
import kr.dogfoot.webserver.loader.resourcesetting.VirtualDirectorySetting;
import kr.dogfoot.webserver.server.host.Host;
import kr.dogfoot.webserver.server.host.MediaTypeManager;
import kr.dogfoot.webserver.server.resource.Resource;
import kr.dogfoot.webserver.server.resource.ResourceDirectory;
import kr.dogfoot.webserver.server.resource.ResourceFile;
import kr.dogfoot.webserver.server.resource.ResourceNegotiatedFile;
import kr.dogfoot.webserver.server.resource.filter.Filter;
import kr.dogfoot.webserver.server.resource.negotiation.NegotiationVariant;
import kr.dogfoot.webserver.util.http.HttpString;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;

import java.io.File;

public class HostLoader {
    public static void load(Host host, String directoryPath, ResourceSetting resourceSetting) {
        File directory = new File(directoryPath);
        if (directory.isDirectory()) {
            host.root().physicalPath(directoryPath);

            addFilter(host.root(), resourceSetting);
            addResourceFromDirectory(host.root(), directory, host, resourceSetting);
            addResourceFromVirtualDirectory(host.root(), host, resourceSetting);
        }
    }

    private static void addFilter(Resource resource, ResourceSetting resourceSetting) {
        Filter[] filters = resourceSetting.findFilters(resource.pathFromRoot(), SettingType.Directory);

        for (Filter f : filters) {
            resource.addFilter(f);
        }
    }

    private static void addResourceFromDirectory(ResourceDirectory dirRes, File directory, Host host, ResourceSetting resourceSetting) {
        String[] notServiceFilePatterns = resourceSetting.findNotServiceFilePattern(dirRes.pathFromRoot());

        String[] filenames = directory.list();
        for (String filename : filenames) {
            if (isNotServiceFile(filename, notServiceFilePatterns) == false) {
                File child = new File(directory, filename);
                if (child.isDirectory()) {
                    ResourceDirectory childDirRes = new ResourceDirectory(dirRes);
                    childDirRes.path(filename);
                    childDirRes.physicalPath(child.getAbsolutePath());
                    dirRes.addResource(childDirRes);

                    addFilter(childDirRes, resourceSetting);
                    addResourceFromDirectory(childDirRes, child, host, resourceSetting);
                    addResourceFromVirtualDirectory(childDirRes, host, resourceSetting);
                } else if (child.isFile()) {
                    if (filename.toLowerCase().endsWith("nego")) {
                        ResourceNegotiatedFile negoFile = new ResourceNegotiatedFile(dirRes);
                        negoFile.negoInfo().parse(dirRes, child);
                        setMediaType(negoFile, host.hostObjects().mediaTypeManager());
                        dirRes.addResource(negoFile);

                        addFilter(negoFile, resourceSetting);
                    } else {
                        ResourceFile fileRes = new ResourceFile(dirRes);
                        fileRes.fileName(filename);
                        setMediaType(fileRes, host.hostObjects().mediaTypeManager());
                        dirRes.addResource(fileRes);

                        addFilter(fileRes, resourceSetting);
                    }
                }
            }
        }
    }

    private static boolean isNotServiceFile(String filename, String[] notServiceFilePatterns) {
        for (String pattern : notServiceFilePatterns) {
            if (FilenameUtils.wildcardMatch(filename, pattern, IOCase.INSENSITIVE)) {
                return true;
            }
        }
        return false;
    }

    private static void setMediaType(ResourceFile fileRes, MediaTypeManager mediaTypeManager) {
        String filename = fileRes.fileName();
        int i = filename.lastIndexOf('.');
        if (i > 0) {
            String ext = filename.substring(i + 1);
            fileRes.mediaType(mediaTypeManager.getMediaType(ext.toLowerCase()));
        }
    }

    private static void addResourceFromVirtualDirectory(ResourceDirectory parentRes, Host host, ResourceSetting resourceSetting) {
        VirtualDirectorySetting[] virtualDirectorySettings = resourceSetting.findVirtualDirectorySettings(parentRes.pathFromRoot());
        for (VirtualDirectorySetting item : virtualDirectorySettings) {
            String[] paths = item.url().split(HttpString.PathSeparator);
            ResourceDirectory childDirRes = parentRes;
            for (String path : paths) {
                if (path != null) {
                    childDirRes = parentRes.getDirectory(path);
                    if (childDirRes == null) {
                        childDirRes = new ResourceDirectory(parentRes);
                        childDirRes.path(path);
                        parentRes.addResource(childDirRes);
                    }
                    parentRes = childDirRes;
                }
            }

            File directory = new File(item.sourcePath());
            if (directory.isDirectory()) {
                childDirRes.physicalPath(item.sourcePath());
                addFilter(childDirRes, resourceSetting);
                addResourceFromDirectory(childDirRes, directory, host, resourceSetting);
                addResourceFromVirtualDirectory(childDirRes, host, resourceSetting);
            }
        }
    }

    private static void setMediaType(ResourceNegotiatedFile negoFile, MediaTypeManager mediaTypeManager) {
        NegotiationVariant[] variants = negoFile.negoInfo().variants();
        for (NegotiationVariant variant : variants) {
            setMediaType(variant, mediaTypeManager);
        }
    }
}
