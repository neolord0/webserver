package kr.dogfoot.webserver.httpMessage.header;

import com.sun.corba.se.impl.orbutil.concurrent.Sync;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SyncTest {
    public static void main(String[] args) throws Exception {
        SyncTest syncTest = new SyncTest();
        syncTest.start();

        while (true) {
            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + " : " + syncTest.count);
        }
    }

    private Integer count;
    private Object lock;
    private ExecutorService executor;

    public SyncTest() {
        count = 0;
        lock = new Object();
        executor = Executors.newFixedThreadPool(201);
    }

    private void start() {
        for (int i = 0; i < 10; i++) {
            createThreadForIncrease();
        }
        createThreadForWait();
    }

    private void createThreadForIncrease() {
        executor.execute(()->{
            try {
                Thread.currentThread().sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (true) {
                increase();
            }
        });
    }

    private void increase() {
        synchronized (lock) {
            count++;
        }
    }

    private void createThreadForWait() {
        executor.execute(()->{
            while (true) {
                try {
                    Thread.currentThread().sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sleep();
            }
        });
    }

    private void sleep() {
        System.out.println("sleep start");
        synchronized (lock) {
            try {
                Thread.currentThread().sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("sleep ends");
    }
}
