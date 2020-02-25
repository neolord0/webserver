package kr.dogfoot.webserver.server.resource.performer.util;

public class ContentRange {
    private boolean isAsterisk;
    private long firstPos;
    private long lastPos;

    public ContentRange() {
        this(0, 0);
    }

    public ContentRange(long firstPos, long lastPos) {
        isAsterisk = false;
        this.firstPos = firstPos;
        this.lastPos = lastPos;
    }

    public boolean isAsterisk() {
        return isAsterisk;
    }

    public void asterisk(boolean asterisk) {
        isAsterisk = asterisk;
    }

    public long firstPos() {
        return firstPos;
    }

    public void firstPos(long firstPos) {
        this.firstPos = firstPos;
    }

    public long lastPos() {
        return lastPos;
    }

    public void lastPos(long lastPos) {
        this.lastPos = lastPos;
    }

    public boolean isMerge(ContentRange other) {
        if (this.firstPos <= other.firstPos && this.lastPos >= other.lastPos) {
            return true;
        } else if (this.firstPos >= other.firstPos && this.lastPos <= other.lastPos) {
            this.firstPos = other.firstPos;
            this.lastPos = other.lastPos;
            return true;
        } else if (this.firstPos >= other.firstPos && this.firstPos - 1 <= other.lastPos) {
            this.firstPos = other.firstPos;
            return true;
        } else if (this.lastPos <= other.lastPos && this.lastPos + 1 >= other.firstPos) {
            this.lastPos = other.lastPos;
            return true;
        }
        return false;
    }

    public long length() {
        return lastPos - firstPos + 1;
    }

    public ContentRange clone() {
        ContentRange cloned = new ContentRange();
        cloned.isAsterisk = isAsterisk;
        cloned.firstPos = firstPos;
        cloned.lastPos = lastPos;
        return cloned;
    }
}


