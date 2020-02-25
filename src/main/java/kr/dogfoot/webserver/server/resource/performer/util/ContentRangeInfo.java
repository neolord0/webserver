package kr.dogfoot.webserver.server.resource.performer.util;

import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

public class ContentRangeInfo {
    private static final int Default_ContentRange_Count = 5;
    private String unit;

    private ContentRange[] ranges;
    private int rangeCount = 0;

    private long instanceLength;

    public ContentRangeInfo() {
        ranges = new ContentRange[Default_ContentRange_Count];
        rangeCount = 0;
    }

    public String unit() {
        return unit;
    }

    public ContentRangeInfo unit(String unit) {
        this.unit = unit;
        return this;
    }

    public long instanceLength() {
        return instanceLength;
    }

    public ContentRangeInfo instanceLength(long instanceLength) {
        this.instanceLength = instanceLength;
        return this;
    }

    public ContentRangeInfo setRanges(ContentRange[] ranges) {
        for (ContentRange cr : ranges) {
            if (cr != null) {
                addRange(cr);
            }
        }
        return this;
    }

    private void addRange(ContentRange cr) {
        if (ranges.length <= rangeCount) {
            ContentRange[] newArray = new ContentRange[ranges.length * 2];
            System.arraycopy(ranges, 0, newArray, 0, ranges.length);
            ranges = newArray;
        }
        ranges[rangeCount++] = cr;
    }

    public int rangeCount() {
        return rangeCount;
    }

    public ContentRange[] ranges() {
        return ranges;
    }

    public ContentRange range(int index) {
        return ranges[index];
    }

    public byte[] toBytes(int index) {
        ContentRange range = range(index);

        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer.append(unit)
                .append(HttpString.Space)
                .appendLong(range.firstPos())
                .append(HttpString.Separator_Subtract)
                .appendLong(range.lastPos())
                .append(HttpString.Separator_Divide)
                .appendLong(instanceLength);
        return buffer.getBytesAndRelease();
    }

    public boolean isInvalid() {
        if (rangeCount == 0) {
            return true;
        }

        for (int index = 0; index < rangeCount; index++) {
            if (ranges[index] == null) {
                return true;
            }
        }
        return false;
    }
}
