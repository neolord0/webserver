package kr.dogfoot.webserver.processor.util;

import kr.dogfoot.webserver.httpMessage.request.Request;

import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RequestSaver {
    private ConcurrentHashMap<SocketChannel, ConcurrentLinkedQueue<Request>> storage;

    public RequestSaver() {
        storage = new ConcurrentHashMap<SocketChannel, ConcurrentLinkedQueue<Request>>();
    }

    public synchronized void save(SocketChannel channel, Request request) {
        ConcurrentLinkedQueue<Request> queue = storage.get(storage);
        if (queue == null) {
            queue = new ConcurrentLinkedQueue<Request>();
            storage.put(channel, queue);
        }
        queue.add(request);
    }

    public synchronized Request get(SocketChannel channel) {
        Request ret = null;
        ConcurrentLinkedQueue<Request> queue = storage.get(channel);
        if (queue != null) {
            ret = queue.poll();

            if (queue.isEmpty()) {
                storage.remove(queue);
            }
        }
        return ret;
    }
}
