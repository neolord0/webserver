package kr.dogfoot.webserver.server.object;

import kr.dogfoot.webserver.context.connection.ajp.AjpProxyConnection;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BufferManager {
    private static final int Vary_Small_Buffer_Size = 100;
    private static final int Small_Buffer_Size = 1024 * 2;
    private static final int Normal_Buffer_Size = 1024 * 8;
    private static final int Large_Buffer_Size = 1024 * 16;

    private ConcurrentHashMap<Integer, ConcurrentLinkedQueue<ByteBuffer>> bufferPoolMap;

    public BufferManager() {
        bufferPoolMap = new ConcurrentHashMap<Integer, ConcurrentLinkedQueue<ByteBuffer>>();
    }

    public synchronized ByteBuffer pooledBuffer(int size) {
        ConcurrentLinkedQueue<ByteBuffer> bufferPool = getBufferPoolBySize(size);
        ByteBuffer buffer = bufferPool.poll();
        if (buffer == null) {
            buffer = ByteBuffer.allocate(size);
        }
        buffer.clear();
        return buffer;
    }

    private ConcurrentLinkedQueue<ByteBuffer> getBufferPoolBySize(int size) {
        ConcurrentLinkedQueue<ByteBuffer> bufferPool = bufferPoolMap.get(size);
        if (bufferPool == null) {
            bufferPool = new ConcurrentLinkedQueue<ByteBuffer>();
            bufferPoolMap.put(size, bufferPool);
        }
        return bufferPool;
    }

    public ByteBuffer pooledVarySmallBuffer() {
        return pooledBuffer(Vary_Small_Buffer_Size);
    }

    public ByteBuffer pooledSmallBuffer() {
        return pooledBuffer(Small_Buffer_Size);
    }

    public ByteBuffer pooledNormalBuffer() {
        return pooledBuffer(Normal_Buffer_Size);
    }

    public ByteBuffer pooledLargeBuffer() {
        return pooledBuffer(Large_Buffer_Size);
    }

    public ByteBuffer pooledBufferForAjpPacket() {
        return pooledBuffer(AjpProxyConnection.MaxPacketSize);
    }

    public void release(ByteBuffer buffer) {
        ConcurrentLinkedQueue<ByteBuffer> bufferPool = getBufferPoolBySize(buffer.capacity());
        bufferPool.add(buffer);
    }
}
