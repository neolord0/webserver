package kr.dogfoot.webserver.parser.util;

import java.nio.ByteBuffer;
import java.util.LinkedList;

public class CachedReader {
    public static final int NO_DATA = 0xffff;
    private static final int CR = 0xd;
    private static final int LF = 0xa;
    private static final int SP = 0x20;
    private static final int HT = 0x9;

    private ByteBuffer buffer;
    private LinkedList<Integer> cache;

    public CachedReader() {
        cache = new LinkedList<Integer>();
    }

    public CachedReader buffer(ByteBuffer buffer) {
        this.buffer = buffer;
        return this;
    }

    public ByteBuffer buffer() {
        return buffer;
    }

    public void reset() {
        cache.clear();
    }

    public void readAndCache() {
        int data = read();
        if (data != NO_DATA) {
            cache.offer(data);
        }
    }

    private int read() {
        if (buffer.hasRemaining() == false) {
            return NO_DATA;
        }
        return buffer.get();
    }

    public String debugCache() {
        StringBuffer sb = new StringBuffer();
        for (Integer i : cache) {
            sb.append(i.toString());
            sb.append(",");
        }
        return sb.toString();
    }

    public int readWithCache() {
        if (cache.isEmpty()) {
            return read();
        }
        return cache.poll();
    }

    public int peek() {
        if (cache.isEmpty()) {
            return NO_DATA;
        }
        return cache.peek();
    }

    public boolean peekIsCRLF() {
        int peek = peek();
        return peek == CR || peek == LF;
    }

    public boolean peekIs(int value) {
        int peek = peek();
        return peek == value;
    }

    public boolean peekIsCR() {
        return peekIs(CR);
    }

    public boolean peekIsLF() {
        return peekIs(LF);
    }

    public boolean peekIsSP() {
        return peekIs(SP);
    }

    public boolean peekIsSPHT() {
        int peek = peek();
        return peek == SP || peek == HT;
    }

    public boolean peekIsEnd() {
        return peekIs(NO_DATA);
    }

    public int poll() {
        if (cache.isEmpty()) {
            return NO_DATA;
        }
        return cache.poll();
    }

    public int pollAndReadAndCache() {
        int data = poll();
        readAndCache();
        return data;
    }

    public int cacheSize() {
        return cache.size();
    }

    public boolean hasData() {
        return buffer.hasRemaining() || cache.size() > 0;
    }

    public void rollbackByCache() {
        buffer.position(buffer.position() - cache.size());
        cache.clear();
    }

    public int remainingSize() {
        return cache.size() + buffer.remaining();
    }

    public void skip(int skip) {
        if (cache.size() >= skip) {
            for (int i = 0; i < skip; i++) {
                cache.poll();
            }
        } else {
            int bufferSkip = skip - cache.size();
            cache.clear();
            buffer.position(buffer.position() + bufferSkip);
        }
    }
}
