package kr.dogfoot.webserver.server.resource;

import kr.dogfoot.webserver.httpMessage.response.Response;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.server.host.HostObjects;
import kr.dogfoot.webserver.server.resource.look.LookResult;
import kr.dogfoot.webserver.server.resource.look.LookState;
import kr.dogfoot.webserver.server.resource.performer.FilePerformer;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

import java.io.File;

public class ResourceFile extends Resource {
    private ResourceDirectory parentDirectory;

    private String fileName;

    private File file;
    private byte[] mediaType;
    private long length;
    private long lastModified;
    private byte[] etag;

    public ResourceFile(ResourceDirectory parentDirectory) {
        this.parentDirectory = parentDirectory;

        this.file = null;

        this.fileName = null;
        this.mediaType = null;
        this.length = -1;
        this.lastModified = 0;
        this.etag = null;
    }

    @Override
    public String name() {
        return file.getName();
    }

    @Override
    public ResourceType type() {
        return ResourceType.File;
    }

    @Override
    public String path() {
        return fileName;
    }

    @Override
    public String pathFromRoot() {
        if (parentDirectory.path() == null) {
            return HttpString.PathSeparator + fileName;
        } else {
            return parentDirectory.pathFromRoot() + HttpString.PathSeparator + fileName;
        }
    }

    @Override
    public boolean look(LookState ls, LookResult lr) {
        lr.appendFilter(filters());
        lr.resource(this);
        return true;
    }

    @Override
    protected boolean isMatched(String str) {
        return str != null && str.equalsIgnoreCase(fileName);
    }

    @Override
    public File file() {
        return file;
    }

    @Override
    public Response perform(Request request, HostObjects hostObjects) {
        return FilePerformer.perform(request, this, hostObjects);
    }

    public ResourceDirectory parentDirectory() {
        return parentDirectory;
    }

    public String fileName() {
        return fileName;
    }

    public void fileName(String fileName) {
        this.fileName = fileName;
        updateFile();
    }

    private void updateFile() {
        file = new File(parentDirectory.file(), fileName);

        length = file.length();
        lastModified = file.lastModified();
        etag = makeEtag();
    }

    private byte[] makeEtag() {
        if (lastModified >= 0L) {
            byte[] part1 = Long.toString(length, 32).getBytes();
            byte[] part2 = Long.toString(lastModified, 32).getBytes();

            OutputBuffer buffer =  OutputBuffer.pooledObject();
            buffer.append(HttpString.DQuote)
                    .append(part1)
                    .append(HttpString.Colon)
                    .append(part2)
                    .append(HttpString.DQuote);
            byte[] etag = buffer.getBytes();
            OutputBuffer.release(buffer);
            return etag;
        }
        return null;
    }

    public byte[] mediaType() {
        return mediaType;
    }

    public void mediaType(byte[] mediaType) {
        this.mediaType = mediaType;
    }

    public long length() {
        return length;
    }

    public long lastModified() {
        return lastModified;
    }

    public byte[] etag() {
        return etag;
    }

    public byte[] getETag() {
        return etag;
    }

}
