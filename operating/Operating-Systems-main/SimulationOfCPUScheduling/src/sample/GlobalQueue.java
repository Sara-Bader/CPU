package sample;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;
// the global queue has a thread that has all of the processes that are
// global queue usually processes with arrrival time 0has an arrival time
public class GlobalQueue {
    public static final LinkedList<Process> global = new LinkedList<>();
    private final Semaphore writeSemaphore = new Semaphore(1);
    private static GlobalQueue obj = null;

    private GlobalQueue(){

    }

    public synchronized static GlobalQueue getInstance(){
        if(obj == null)
            obj = new GlobalQueue();
        return obj;
    }

    // Get read write lock
    public  void readWriteLock() throws InterruptedException {
        writeSemaphore.acquire();
    }

    //Release read write lock
    public  void readWriteUnlock(){
        writeSemaphore.release();
    }

}