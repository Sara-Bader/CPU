package sample;
// IO scheduler with a thread a

import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class IOScheduler implements Runnable {
    public static LinkedList<Process> IOQueue = new LinkedList<>();
    private static IOScheduler IO = null;
    private final Semaphore mutex = new Semaphore(1); // adding the semaphore ao the time stays consistant and the queue can not be accessed by more than one threadd at a time
    private static boolean IOBusy = false;

    private IOScheduler(){

    }

    public synchronized static IOScheduler getInstance(){
        if(IOScheduler.IO == null){
            IOScheduler.IO = new IOScheduler();
        }
        return IOScheduler.IO;
    }

    public  void waitIOQueue() throws InterruptedException {
        mutex.acquire();
    }

    public  void signalIOQueue(){
        mutex.release();
    }

    public  void addToIOQueue(Process process){
        IOScheduler.IOQueue.add(process);
    }//add proccesses to the queue

    @Override
    public void run() {
        Process runningProcess = new Process();
        long time;
        while(true){
            try {
                ProcesessSynch.getInstance().waitFinished();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(ProcesessSynch.finishedProcesses.size() == SimulateController.numberOfProcesses) {
                ProcesessSynch.getInstance().signalFinished();
                break;
            }
            ProcesessSynch.getInstance().signalFinished();
            try {
                TimeListSynchronization.getInstance().waitTimeList();
                if (TimeListSynchronization.ThreadReadTimeVariable.get(1)) { // check if the thread can read the time variable
                    time = TimeListSynchronization.currentTime;
                    TimeListSynchronization.getInstance().signalTimeList();
                    this.waitIOQueue();
                    if (!IOBusy && IOQueue.size() > 0) {
                        runningProcess = IOQueue.poll();
                        runningProcess.setAddedToIODevice(time);
                        IOBusy = true;
                    }
                    this.signalIOQueue();
                    if (IOBusy) {
                        long remainingTime = time - runningProcess.getAddedToIODevice();
                        if (remainingTime == runningProcess.getIOBurstTime()) {// check the io burst if it is zero terminate the process
                            runningProcess.updateIOBurst(0L);
                            if (runningProcess.getLastIOBurst() == 0) {
                                runningProcess.setFinished(time);
                                runningProcess.setState("Finished");
                                ProcesessSynch.getInstance().waitFinished();
                                ProcesessSynch.finishedProcesses.add(runningProcess);
                                TimeDetailsLock.getInstance().lock();
                                TimeDetailsLock.specificTimeDetails.computeIfAbsent(TimeListSynchronization.currentTime, k -> new LinkedList<>());
                                TimeDetailsLock.specificTimeDetails.get(TimeListSynchronization.currentTime).add("Process " + runningProcess.getPid()+" terminated");
                                TimeDetailsLock.getInstance().unlock();
                                System.out.println("Finished: " + runningProcess.getPid()+" "+runningProcess.getFinished());
                                ProcesessSynch.getInstance().signalFinished();
                            } else { // if the state of the process is running move it from the IO queue to the global queue
                                runningProcess.setState("Ready");
                                GlobalQueue.getInstance().readWriteLock();
                                GlobalQueue.global.add(runningProcess);
                                TimeDetailsLock.getInstance().lock();
                                TimeDetailsLock.specificTimeDetails.computeIfAbsent(TimeListSynchronization.currentTime, k -> new LinkedList<>());
                                TimeDetailsLock.specificTimeDetails.get(TimeListSynchronization.currentTime).add("Process " + runningProcess.getPid()+" is moved from IO queue to global queue");
                                TimeDetailsLock.getInstance().unlock();
                                GlobalQueue.getInstance().readWriteUnlock();
                            }
                            this.waitIOQueue();
                            if (IOQueue.size() == 0)
                                IOBusy = false;
                            else {
                                runningProcess = IOQueue.poll();
                                runningProcess.setAddedToIODevice(time);
                            }
                            this.signalIOQueue();

                        } else {
                            runningProcess.updateIOBurst(runningProcess.getIOBurstTime() - remainingTime);
                            runningProcess.setAddedToIODevice(time);
                        }
                    }
                    TimeListSynchronization.getInstance().waitTimeList();
                    TimeListSynchronization.ThreadReadTimeVariable.set(1, false);
                    TimeListSynchronization.getInstance().signalTimeList();
                    continue;
                }
                TimeListSynchronization.getInstance().signalTimeList();
            }
            catch(InterruptedException e){
                    e.printStackTrace();
                }
        }
    }
}