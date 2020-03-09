package kr.dogfoot.webserver.server.timer;

import java.util.Vector;

public class Timer {
    private Vector events;
    private Thread thread;
    private volatile boolean running;

    public Timer() {
        events = new Vector();
    }

    public Timer start() {
        running = true;

        thread = new Thread() {
            public void run() {
                while (running) {
                    TimerEvent e = getNextEvent();
                    if (e != null) {
                        e.handler.handleTimerEvent(e.data, e.time);
                    }
                }
            }
        };
        thread.start();
        thread.setPriority(Thread.MAX_PRIORITY - 1);

        return this;
    }

    public void terminate() {
        running = false;

        notify();
    }

    public Object addEvent(long timeout, TimerEventHandler handler, Object data) {
        long time = timeout * 1000 + System.currentTimeMillis();
        TimerEvent event = new TimerEvent(time, handler, data);

        return addEvent(event);
    }

    private synchronized Object addEvent(TimerEvent event) {
        int lo = 0;
        int hi = events.size();
        long newTime = event.time;
        TimerEvent e;
        long midTime;

        if (running == false) {
            return null;
        }

        if (hi == 0) {
            events.addElement(event);
        } else {
            while (hi - lo > 0) {
                int mid = (hi + lo) >> 1;
                e = (TimerEvent) events.elementAt(mid);
                midTime = e.time;

                if (midTime < newTime) {
                    lo = mid + 1;
                } else if (midTime > newTime) {
                    hi = mid;
                } else {
                    lo = mid;
                    break;
                }
            }
            if (lo < hi && ((TimerEvent) events.elementAt(lo)).time > newTime) {
                lo += 1;
            }
            events.insertElementAt(event, lo);
        }
        notify();
        return event;
    }

    public synchronized void removeEvent(Object event) {
        if (events.size() != 0) {
            events.remove(event);
            notify();
        }
    }

    private synchronized TimerEvent getNextEvent() {
        while (true) {
            while (events.size() == 0) {
                if (running == false) {
                    return null;
                }
                try {
                    wait(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            TimerEvent e = (TimerEvent) events.elementAt(0);
            long now = System.currentTimeMillis();
            long dt;
            dt = e.time - now;

            if (dt <= 0) {
                events.removeElementAt(0);
                return e;
            }

            try {
                wait(dt);
            } catch (InterruptedException exx) {
                exx.printStackTrace();
            }
        }
    }

    private class TimerEvent {
        long time;
        TimerEventHandler handler;
        Object data;

        TimerEvent(long time, TimerEventHandler handler, Object data) {
            this.time = time;
            this.handler = handler;
            this.data = data;
        }
    }
}
