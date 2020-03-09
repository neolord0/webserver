package kr.dogfoot.webserver.server.cache;

import kr.dogfoot.webserver.server.timer.Timer;
import kr.dogfoot.webserver.server.timer.TimerEventHandler;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ResponseRemover implements TimerEventHandler {
    private static int INTERVAL_FOR_REMOVE_RESPONSES = 1000; // 1 sec

    private volatile boolean running;
    private Timer timer;
    private CacheManagerImp cacheManager;

    private LinkedList<StoredResponse> reservedResponsesToRemove;
    private Object timerEventForRemoveResponses;

    public ResponseRemover(Timer timer, CacheManagerImp cacheManager) {
        running = false;
        this.timer = timer;
        this.cacheManager = cacheManager;

        reservedResponsesToRemove = new LinkedList<StoredResponse>();
        timerEventForRemoveResponses = null;
    }

    public void start() {
        running = true;

        setTimerForRemoveResponses();
    }

    private void setTimerForRemoveResponses() {
        if (running) {
            timerEventForRemoveResponses = timer.addEvent(INTERVAL_FOR_REMOVE_RESPONSES, this, null);
        }
    }

    public void terminate() {
        running = false;

        unsetTimerForRemoveResponses();
    }

    private void unsetTimerForRemoveResponses() {
        if (timerEventForRemoveResponses != null) {
            timer.removeEvent(timerEventForRemoveResponses);
            timerEventForRemoveResponses = null;
        }
    }

    public synchronized void reserve(StoredResponse response) {
        reservedResponsesToRemove.add(response);
    }

    @Override
    public void handleTimerEvent(Object data, long time) {
        deleteUnusedResource();

        setTimerForRemoveResponses();
    }

    private synchronized void deleteUnusedResource() {
        if (reservedResponsesToRemove.size() <= 0) {
            return;
        }

        Iterator<StoredResponse> it = reservedResponsesToRemove.iterator();
        while (it.hasNext()) {
            StoredResponse response = it.next();
            if (response.usingCount() <= 0) {
                deleteResponse(response);
                it.remove();
            }
        }
    }

    private void deleteResponse(StoredResponse response) {
        response.deleteMe();
        response.deleteFile(true);
        StoredResponse.release(response);
    }
}
