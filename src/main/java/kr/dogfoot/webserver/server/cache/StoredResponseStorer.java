package kr.dogfoot.webserver.server.cache;

import kr.dogfoot.webserver.httpMessage.header.HeaderItem;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValue;
import kr.dogfoot.webserver.httpMessage.response.Response;
import kr.dogfoot.webserver.server.resource.performer.util.ContentRange;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StoredResponseStorer {
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

    public static void deleteTempFile(StoredResponse sr, boolean includeBodyFile) {
        if (sr.infoFile() != null) {
            sr.infoFile().delete();
        }
        if (sr.responseFile() != null) {
            sr.responseFile().delete();
        }
        if (includeBodyFile == true && sr.bodyFile() != null) {
            sr.bodyFile().delete();
        }
    }

    public static void store(StoredResponse sr) {
        if (sr.infoFile() == null) {
            createTempFiles(sr);
        }
        storeInfo(sr);
        storeResponseInfo(sr);
    }

    private static void createTempFiles(StoredResponse sr) {
        try {
            sr.infoFile(File.createTempFile("csr", ".inf", sr.parentPathFile()));
        } catch (IOException e) {
            sr.infoFile(null);
        }
        if (sr.infoFile() != null) {
            String infoFileName = sr.infoFile().getName();

            sr.responseFile(new File(sr.parentPathFile(), infoFileName.substring(0, infoFileName.length() - 4) + ".rsp"));
            sr.bodyFile(new File(sr.parentPathFile(), infoFileName.substring(0, infoFileName.length() - 4) + ".bdy"));
        }
    }

    private static void storeInfo(StoredResponse sr) {
        if (sr.infoFile() == null) {
            return;
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(sr.requestMethod());
                oos.writeObject(sr.selectionFields().size());
                for (HeaderValue field : sr.selectionFields()) {
                    oos.writeObject(field.sort());
                    oos.writeObject(field.combineValue());
                }
                oos.writeObject(sr.etag());
                oos.writeObject(sr.date());
                oos.writeObject(sr.age());
                oos.writeObject(sr.requestTime());
                oos.writeObject(sr.responseTime());
                oos.writeObject(sr.freshnessLifeTime());

                FileOutputStream fos = new FileOutputStream(sr.infoFile());
                fos.write(baos.toByteArray());
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void storeResponseInfo(StoredResponse sr) {
        if (sr.responseFile() == null) {
            return;
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                Response response = sr.response();

                oos.writeObject(response.majorVersion());
                oos.writeObject(response.minorVersion());
                oos.writeObject(response.statusCode());
                int headerCount = response.headerCount();
                oos.writeObject(headerCount);
                for (int index = 0; index < headerCount; index++) {
                    HeaderItem item = response.getHeaderItem(index);
                    oos.writeObject(item.sort());
                    oos.writeObject(item.valueBytes());
                }

                FileOutputStream fos = new FileOutputStream(sr.responseFile());
                fos.write(baos.toByteArray());
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void storeBody(StoredResponse sr, ByteBuffer buffer) {
        if (sr.bodyFile() == null) {
            return;
        }

        FileChannel wChannel = null;
        try {
            wChannel = new FileOutputStream(sr.bodyFile(), true).getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            wChannel = null;
        }

        if (wChannel != null) {
            try {
                wChannel.write(buffer);
                wChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        updateResponseRange(sr);
    }

    private static void updateResponseRange(StoredResponse sr) {
        if (sr.response().range() == null) {
            sr.response().range(new ContentRange());
        }
        sr.response().range().lastPos(sr.bodyFile().length() - 1);
    }
}
