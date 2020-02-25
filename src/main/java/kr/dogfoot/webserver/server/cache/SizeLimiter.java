package kr.dogfoot.webserver.server.cache;

public class SizeLimiter {
    private static long DEFAULT_MAX_SIZE = 1 * 1024 * 1024;
    private long maxSize;
    private long size;

    public SizeLimiter() {
        maxSize = DEFAULT_MAX_SIZE;
        size = 0;
    }

    public void maxSize(long maxSize) {
        this.maxSize = maxSize;
    }

    public synchronized boolean isOverflow(int currentSize) {
        return size + currentSize > maxSize;
    }

    public synchronized void addSize(long size) {
        this.size += size;
    }

    public synchronized void subtractSize(long size) {
        this.size -= size;
    }
}
