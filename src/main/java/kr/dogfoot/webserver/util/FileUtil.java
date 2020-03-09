package kr.dogfoot.webserver.util;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {
    public static File openDirectory(Path path) {
        if (!Files.exists(path)) {
            try {
                return Files.createDirectories(path).toFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new File(path.toString());
    }

    public static File openDirectory(String path) {
        return openDirectory(Paths.get(path));
    }

    public static File openDirectory(File parentPath, String name) {
        return openDirectory(Paths.get(parentPath.toString(), name));
    }
}
