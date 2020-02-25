package kr.dogfoot.webserver.server.cache;

import kr.dogfoot.webserver.server.timer.Timer;
import kr.dogfoot.webserver.server.timer.TimerEventHandler;

import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;

public class InactiveResponseRemover implements TimerEventHandler {
    private static int TIMEOUT_FOR_REMOVE_RESPONSES = 1000;

    private boolean running;
    private Timer timer;
    private CacheManagerImp cacheManager;

    private ConcurrentLinkedQueue<StoredResponse> reservedResponsesToRemove;
    private Object timerEventForRemoveResponses;

    public InactiveResponseRemover(Timer timer, CacheManagerImp cacheManager) {
        running = false;
        this.timer = timer;
        this.cacheManager = cacheManager;

        reservedResponsesToRemove = new ConcurrentLinkedQueue<StoredResponse>();
        timerEventForRemoveResponses = null;
    }

    public void start() {
        running = true;
        resetTimerForRemoveResponses();
    }

    private void resetTimerForRemoveResponses() {
        if (running) {
            if (timerEventForRemoveResponses != null) {
                timer.removeEvent(timerEventForRemoveResponses);
                timerEventForRemoveResponses = null;
            }
            timerEventForRemoveResponses = timer.addEvent(TIMEOUT_FOR_REMOVE_RESPONSES, this, null);
        }
    }

    public void terminate() {
        running = false;

        if (timerEventForRemoveResponses != null) {
            timer.removeEvent(timerEventForRemoveResponses);
            timerEventForRemoveResponses = null;
        }
    }

    public synchronized void reserve(StoredResponse response) {
        reservedResponsesToRemove.add(response);
    }

    @Override
    public void HandleTimerEvent(Object data, long time) {
        if (reservedResponsesToRemove.size() > 0) {
            HashSet<StoredResponse> retryList = new HashSet<StoredResponse>();
            StoredResponse response;
            while ((response = reservedResponsesToRemove.poll()) != null) {
                if (response.usingCount() <= 0) {
                    response.invalidate();
                    StoredResponse.release(response);
                } else {
                    retryList.add(response);
                }
            }
            addForRetry(retryList);
        }
    }

    private synchronized void addForRetry(HashSet<StoredResponse> retryList) {
        reservedResponsesToRemove.addAll(retryList);
    }
}
