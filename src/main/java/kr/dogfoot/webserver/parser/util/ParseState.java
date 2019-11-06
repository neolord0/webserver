package kr.dogfoot.webserver.parser.util;


import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ParseState {
    private static ConcurrentLinkedQueue<ParseState> psPool = new ConcurrentLinkedQueue<ParseState>();
    public int ioff;       // input offset
    public int ooff;       // output offset (where parsing should continue)
    public int start;      // Start of parsed item (if needed)
    public int end;        // End of parsed item (if needed)
    public int bufend;     // End of the buffer to parseValue
    public boolean isSkipable;   // Always skip space when this make sense
    public boolean isQuotable;   // Support quted string while parsing next item
    public boolean spaceIsSep;
    public boolean permitNoSeparator;
    public byte separator; // Separator for parsing list

    private ParseState() {
    }

    public static ParseState pooledObject() {
        ParseState ps = psPool.poll();
        if (ps == null) {
            ps = new ParseState();
        }
        ps.reset();
        return ps;
    }

    public static ParseState pooledObject(int ioff) {
        ParseState ps = psPool.poll();
        if (ps == null) {
            ps = new ParseState();
        }
        ps.reset();
        ps.ioff = ioff;
        return ps;
    }

    public static ParseState pooledObject(int ioff, int bufend) {
        ParseState ps = psPool.poll();
        if (ps == null) {
            ps = new ParseState();
        }
        ps.reset();
        ps.ioff = ioff;
        ps.bufend = bufend;
        return ps;
    }

    public static void release(ParseState ps) {
        psPool.add(ps);
    }

    private void reset() {
        ioff = -1;
        ooff = -1;
        start = -1;
        end = -1;
        bufend = -1;

        isSkipable = true;
        isQuotable = true;
        spaceIsSep = true;
        permitNoSeparator = true;

        separator = (byte) ',';
    }

    public final void prepare() {
        ioff = ooff;
        start = -1;
        end = -1;
    }

    public final void prepare(ParseState ps) {
        this.ioff = ps.start;
        this.bufend = ps.end;
    }

    public void rest() {
        this.start = this.end + 1;
        this.end = this.bufend;
    }

    public final String toString(byte[] raw) {
        String ret;
        ret = new String(raw, start, end - start, StandardCharsets.ISO_8859_1);
        return ret;
    }

    public final String toString(byte[] raw, boolean lower) {
        if (lower) {
            // To lower case:
            for (int i = start; i < end; i++)
                raw[i] = (((raw[i] >= 'A') && (raw[i] <= 'Z'))
                        ? (byte) (raw[i] - 'A' + 'a')
                        : raw[i]);
        } else {
            // To upper case:
            for (int i = start; i < end; i++)
                raw[i] = (((raw[i] >= 'a') && (raw[i] <= 'z'))
                        ? (byte) (raw[i] - 'a' + 'A')
                        : raw[i]);
        }
        String ret;
        ret = new String(raw, start, end - start, StandardCharsets.ISO_8859_1);
        return ret;
    }

    public byte[] toNewBytes(byte[] value) {
        byte[] ret = new byte[end - start];
        for (int index = 0; index < end - start; index++) {
            ret[index] = value[start + index];
        }
        return ret;
    }

    public boolean isEnd() {
        return end == bufend;
    }
}


