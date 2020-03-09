package kr.dogfoot.webserver.server.cache;

import java.io.File;

public class CacheLoader {
    public static void load(File storagePath, CacheManagerImp cacheManager) {
        if (storagePath.isDirectory()) {
            String[] filenames = storagePath.list();
            for (String filename : filenames) {
                CacheHost host = newHostToFilename(filename, cacheManager);
                if (host != null) {
                    loadHost(host, cacheManager);
                    cacheManager.addHost(host);
                }
            }
        }
    }

    private static final String IP_PORT_SPLITTER = "_";
    private static CacheHost newHostToFilename(String filename, CacheManagerImp cacheManager) {
        String[] item = filename.split(IP_PORT_SPLITTER);
        if (item.length == 2) {
            int port = 0;
            try {
                port = Integer.parseInt(item[1]);
            } catch (NumberFormatException e) {
                return null;
            }
            return new CacheHost(cacheManager, item[0], port);
        }
        return null;
    }


    private static void loadHost(CacheHost host, CacheManagerImp cacheManager) {
        loadEntry(host.rootEntry(), cacheManager);
    }

    private static void loadEntry(CacheEntry entry, CacheManagerImp manager) {
        File[] filenames = entry.directoryFile().listFiles();
        for (File childFile : filenames) {
            if (childFile.exists() == false) {
                continue;
            }
            String childFilename = childFile.getName();
            if (childFile.isDirectory()) {
                CacheEntry childEntry = new CacheEntry(manager)
                        .openPath(entry.directoryFile(), childFilename);;
                entry.addEntry(childEntry);

                loadEntry(childEntry, manager);
            } else if (childFile.isFile() && childFilename.endsWith(".inf")) {
                StoredResponse response = StoredResponse.pooledObject(entry);
                response.loadFromFile(childFile);
                entry.addResponse(response);
            }
        }
    }
}
