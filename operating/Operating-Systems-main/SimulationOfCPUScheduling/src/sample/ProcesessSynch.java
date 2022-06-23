package sample;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class ProcesessSynch {
    public static LinkedList<Process> finishedProcesses = new LinkedList<>();
    private final Semaphore mutex = new Semaphore(1);
    private static ProcesessSynch obj = null;

    private ProcesessSynch(){

    }

    public synchronized static ProcesessSynch getInstance(){
        if (obj == null)
            obj = new ProcesessSynch();
        return obj;
    }

    public  void waitFinished() throws InterruptedException {
        mutex.acquire();
    }

    public  void signalFinished(){
        mutex.release();
    }
}
