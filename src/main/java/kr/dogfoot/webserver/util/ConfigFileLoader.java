package kr.dogfoot.webserver.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;

public class ConfigFileLoader {
    private static File configDirectory;
    private static String configDirectoryPath;

    public static void setConfigDirectory(String path) throws Exception {
        File file = new File(path);
        if (file.isAbsolute() && file.isDirectory()) {
            configDirectory = file;
            configDirectoryPath = path;
        } else {
            throw new Exception("no the appropriate directory.");
        }
    }

    public static InputStream getFileInputStream(String filePath) throws FileNotFoundException {
        File file = new File(configDirectory, filePath);
        return new FileInputStream(file);
    }

    public static URI getURI(String filePath) {
        return new File(configDirectory, filePath).toURI();
    }
}
