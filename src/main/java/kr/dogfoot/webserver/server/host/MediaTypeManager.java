package kr.dogfoot.webserver.server.host;

import kr.dogfoot.webserver.server.object.DefinedMediaTypeManager;
import kr.dogfoot.webserver.util.http.HttpString;

import java.util.HashMap;
import java.util.Map;

public class MediaTypeManager {
    private byte[] defaultMediaType;
    private DefinedMediaTypeManager definedMediaTypeManager;

    private Map<String, byte[]> mediaTypeList;

    public MediaTypeManager(DefinedMediaTypeManager dmtm) {
        this.mediaTypeList = new HashMap<String, byte[]>();

        this.definedMediaTypeManager = dmtm;

        defaultMediaType = HttpString.Text_Plain;
    }

    public byte[] getMediaType(String ext) {
        byte[] mediaType = mediaTypeList.get(ext);
        if (mediaType == null) {
            mediaType = definedMediaTypeManager.getMediaType(ext);
        }
        if (mediaType == null) {
            return defaultMediaType;
        }
        return mediaType;
    }

    public void addCustomMediaType(String ext, String mediaType) {
        mediaTypeList.put(ext, mediaType.getBytes());
    }
}
