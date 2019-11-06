package kr.dogfoot.webserver.context;

public class DebugInfo {
    private long startTime;
    private long readBytes;
    private long wroteBytes;

    public DebugInfo() {
        reset();
    }


    public void reset() {
        startTime = 0;
        readBytes = 0;
        wroteBytes = 0;
    }

    public void setStartTime() {
        startTime = System.currentTimeMillis();
    }

    public long getInterval() {
        return System.currentTimeMillis() - startTime;
    }

    public long readBytes() {
        return readBytes;
    }

    public void addReadBytes(long readBytes) {
        this.readBytes += readBytes;
    }

    public long wroteBytes() {
        return wroteBytes;
    }

    public void addWroteBytes(long wroteBytes) {
        this.wroteBytes += wroteBytes;
    }
}
