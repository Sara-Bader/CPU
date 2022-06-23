package sample;

import java.util.concurrent.Semaphore;

public class ProcessorsQueuesLock {
    private final Semaphore mutex = new Semaphore(1);
    private static ProcessorsQueuesLock queueLock = null;

    private ProcessorsQueuesLock(){

    }

    public synchronized static ProcessorsQueuesLock getInstance(){
        if(queueLock == null)
            queueLock = new ProcessorsQueuesLock();
        return queueLock;
    }

    public void lock() throws InterruptedException {
        mutex.acquire();
    }

    public void unlock(){
        mutex.release();
    }
}
