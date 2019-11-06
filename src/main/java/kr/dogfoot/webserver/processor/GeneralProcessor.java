package kr.dogfoot.webserver.processor;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.server.Server;

public abstract class GeneralProcessor extends Processor {
    protected volatile boolean running;
    protected Thread thread;

    public GeneralProcessor(Server server) {
        super(server);
    }

    @Override
    public void start() throws Exception {
        running = true;
        thread = new Thread(() -> {
            while (running) {
                synchronized (thread) {
                    while (waitingContextQueue.peek() == null) {
                        try {
                            thread.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                Context context = waitingContextQueue.poll();

                onNewContext(context);
            }
        });
        thread.start();
    }

    protected abstract void onNewContext(Context context);


    @Override
    protected void wakeup() {
        synchronized (thread) {
            thread.notify();
        }
    }

    @Override
    public void terminate() throws Exception {
        running = false;
        wakeup();
    }

}
