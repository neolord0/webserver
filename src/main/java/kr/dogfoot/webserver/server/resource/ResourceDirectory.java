package kr.dogfoot.webserver.server.resource;

import kr.dogfoot.webserver.util.http.HttpString;

import java.io.File;


public class ResourceDirectory extends ResourceContainer {
    private static final String Default_Default_Filename = "index.html";

    private ResourceDirectory parentDirectory;
    private String path;
    private String pathFromRoot;
    private String physicalPath;
    private File file;

    private String defaultFilename;

    public ResourceDirectory(ResourceDirectory parentDirectory) {
        this.parentDirectory = parentDirectory;

        path = null;
        pathFromRoot = null;
        physicalPath = null;
        file = null;

        defaultFilename = Default_Default_Filename;
    }

    @Override
    public ResourceType type() {
        return ResourceType.Directory;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public String pathFromRoot() {
        if (pathFromRoot == null) {
            if (parentDirectory == null) {
                if (path == null) {
                    pathFromRoot = "/";
                } else {
                    pathFromRoot = HttpString.PathSeparator + path;
                }
            } else {
                if (parentDirectory.path() == null) {
                    pathFromRoot = HttpString.PathSeparator + path;
                } else {
                    pathFromRoot = parentDirectory.pathFromRoot() + HttpString.PathSeparator + path;
                }
            }
        }
        return pathFromRoot;
    }

    @Override
    protected Resource look(String path) {
        if (path.equals("")) {
            return super.look(defaultFilename);
        }
        return super.look(path);
    }

    @Override
    protected boolean isMatched(String str) {
        return str != null && str.equalsIgnoreCase(path);
    }

    @Override
    public File file() {
        return file;
    }

    public ResourceDirectory parentDirectory() {
        return parentDirectory;
    }

    public void path(String path) {
        this.path = path;
    }

    public String physicalPath() {
        return physicalPath;
    }

    public void physicalPath(String physicalPath) {
        this.physicalPath = physicalPath;
        updateFile();
    }

    private void updateFile() {
        file = new File(physicalPath);
    }

    public String defaultFilename() {
        return defaultFilename;
    }

    public void defaultFilename(String defaultFilename) {
        this.defaultFilename = defaultFilename;
    }

}

