package sample;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Semaphore;
// get the details at a spicefic time
public class TimeDetailsLock {
    private final Semaphore mutex = new Semaphore(1);
    private static TimeDetailsLock queueLock = null;
    public static Map<Long, LinkedList<String>> specificTimeDetails = new HashMap<>();

    private TimeDetailsLock(){

    }

    public synchronized static TimeDetailsLock getInstance(){
        if(queueLock == null)
            queueLock = new TimeDetailsLock();
        return queueLock;
    }

    public void lock() throws InterruptedException {
        mutex.acquire();
    }

    public void unlock(){
        mutex.release();
    }
}
