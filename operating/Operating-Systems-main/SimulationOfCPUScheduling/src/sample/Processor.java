package sample;

import java.util.LinkedList;

public class Processor implements Runnable{
    private final int processorID;
    private static int nextID = 2;
    private final LinkedList<Process> processorQueue;
    private static final IOScheduler IOLock = IOScheduler.getInstance();
    private long notEfficientTime;

    public Processor() {
        this.processorID = Processor.nextID;
        this.processorQueue = new LinkedList<>();
        this.notEfficientTime = 0;
        Processor.nextID++;
    }

    public int getProcessorID() {
        return processorID;
    }

    public int getNumberOfProcessesInCPUsQueue(){
        return this.processorQueue.size();
    }

    public void setNotEfficientTime(long notEfficientTime){
        this.notEfficientTime = notEfficientTime;
    }

    public long getNotEfficientTime(){
        return this.notEfficientTime;
    }

    private void RR(){ // round robin algorithem
        boolean busy = false;
        Process runningProcess = new Process();
        long time;
        while(true){
            int maximumNumberOfProcessesInSingleProcessor = 0;
            try {
                ProcesessSynch.getInstance().waitFinished();
                if(ProcesessSynch.finishedProcesses.size() == SimulateController.numberOfProcesses){
                    ProcesessSynch.getInstance().signalFinished();
                    break;
                }
                ProcesessSynch.getInstance().signalFinished();
                TimeListSynchronization.getInstance().waitTimeList();
                if(TimeListSynchronization.ThreadReadTimeVariable.get(this.processorID)){
                    time = TimeListSynchronization.currentTime;
                    TimeListSynchronization.getInstance().signalTimeList();
                    GlobalQueue.getInstance().readWriteLock();
                    if(GlobalQueue.global.size() != 0){
                        ProcessorsQueuesLock.getInstance().lock();
                        int numberOfCPUsHaveMaximumNumberOfProcesses = 0;
                        for(int i = 0;i < SimulateController.numberOfProcessors;i++)
                            maximumNumberOfProcessesInSingleProcessor = Math.max(maximumNumberOfProcessesInSingleProcessor, SimulateController.processors.get(i).getNumberOfProcessesInCPUsQueue());
                        for(int i = 0;i < SimulateController.numberOfProcessors;i++)
                            if (maximumNumberOfProcessesInSingleProcessor == SimulateController.processors.get(i).getNumberOfProcessesInCPUsQueue())
                                numberOfCPUsHaveMaximumNumberOfProcesses++;
                        if (this.processorQueue.size() < maximumNumberOfProcessesInSingleProcessor || numberOfCPUsHaveMaximumNumberOfProcesses == SimulateController.numberOfProcessors) {
                            TimeDetailsLock.getInstance().lock();
                            TimeDetailsLock.specificTimeDetails.computeIfAbsent(TimeListSynchronization.currentTime, k -> new LinkedList<>());
                            TimeDetailsLock.specificTimeDetails.get(TimeListSynchronization.currentTime).add("Process " + GlobalQueue.global.peek().getPid()+" is added to processor "+(this.getProcessorID()-2)+" queue");
                            TimeDetailsLock.getInstance().unlock();
                            this.processorQueue.add(GlobalQueue.global.poll());
                        }

                        ProcessorsQueuesLock.getInstance().unlock();
                    }
                    GlobalQueue.getInstance().readWriteUnlock();
                    ProcessorsQueuesLock.getInstance().lock();
                    if(!busy && this.processorQueue.size() > 0){
                        runningProcess = this.processorQueue.poll();
                        runningProcess.setState("Running");
                        runningProcess.setAssignedProcessorAt(time);
                        if(runningProcess.getResponseTime() == -1)
                            runningProcess.setResponseTime(time - runningProcess.getArrived());
                        TimeDetailsLock.getInstance().lock();
                        TimeDetailsLock.specificTimeDetails.computeIfAbsent(TimeListSynchronization.currentTime, k -> new LinkedList<>());
                        TimeDetailsLock.specificTimeDetails.get(TimeListSynchronization.currentTime).add("Process " + runningProcess.getPid()+" is assigned to processor "+(this.getProcessorID()-2)+" (Context Switch)");
                        TimeDetailsLock.getInstance().unlock();
                        this.notEfficientTime++;
                        busy = true;
                    }
                    ProcessorsQueuesLock.getInstance().unlock();
                    if(busy){
                        long remainingTime = time - runningProcess.getAssignedProcessorAt();
                        if(remainingTime == runningProcess.getCPUBurstTime()){
                            IOScheduler.getInstance().waitIOQueue();
                            runningProcess.updateCPUBurst(runningProcess.getCPUBurstTime() - remainingTime);
                            runningProcess.setState("Waiting");
                            IOLock.addToIOQueue(runningProcess);
                            IOScheduler.getInstance().signalIOQueue();
                            TimeDetailsLock.getInstance().lock();
                            TimeDetailsLock.specificTimeDetails.computeIfAbsent(TimeListSynchronization.currentTime, k -> new LinkedList<>());
                            TimeDetailsLock.specificTimeDetails.get(TimeListSynchronization.currentTime).add("Process " + runningProcess.getPid()+" is added to IO queue");
                            TimeDetailsLock.getInstance().unlock();
                            busy = false;
                        }
                        else if(remainingTime == SimulateController.timeQuantum){
                            runningProcess.updateCPUBurst(runningProcess.getCPUBurstTime() - SimulateController.timeQuantum);
                            runningProcess.setState("Waiting");
                            ProcessorsQueuesLock.getInstance().lock();
                            this.processorQueue.add(runningProcess);
                            ProcessorsQueuesLock.getInstance().unlock();
                            this.notEfficientTime++;
                            busy = false;
                        }
                    }
                    TimeListSynchronization.getInstance().waitTimeList();
                    TimeListSynchronization.ThreadReadTimeVariable.set(this.processorID, false);
                    TimeListSynchronization.getInstance().signalTimeList();
                    continue;
                }
                TimeListSynchronization.getInstance().signalTimeList();
            } catch (NullPointerException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void SRTF(){
        boolean busy = false;
        Process runningProcess = null;
        long time;
        while(true){
            int maximumNumberOfProcessesInSingleProcessor = 0;
            try {
                ProcesessSynch.getInstance().waitFinished();
                if(ProcesessSynch.finishedProcesses.size() == SimulateController.numberOfProcesses){
                    ProcesessSynch.getInstance().signalFinished();
                    break;
                }
                ProcesessSynch.getInstance().signalFinished();
                TimeListSynchronization.getInstance().waitTimeList();
                if(TimeListSynchronization.ThreadReadTimeVariable.get(this.processorID)){
                    time = TimeListSynchronization.currentTime;
                    TimeListSynchronization.getInstance().signalTimeList();

                    long elapsedTime = 0;
                    boolean addContextSwitch = false;
                    if(busy)
                        elapsedTime = time - runningProcess.getAssignedProcessorAt();

                    GlobalQueue.getInstance().readWriteLock();
                    if(GlobalQueue.global.size() != 0){
                        ProcessorsQueuesLock.getInstance().lock();
                        int numberOfCPUsHaveMaximumNumberOfProcesses = 0;
                        for(int i = 0;i < SimulateController.numberOfProcessors;i++)
                            maximumNumberOfProcessesInSingleProcessor = Math.max(maximumNumberOfProcessesInSingleProcessor, SimulateController.processors.get(i).getNumberOfProcessesInCPUsQueue());
                        for(int i = 0;i < SimulateController.numberOfProcessors;i++)
                            if (maximumNumberOfProcessesInSingleProcessor == SimulateController.processors.get(i).getNumberOfProcessesInCPUsQueue())
                                numberOfCPUsHaveMaximumNumberOfProcesses++;
                        if (this.processorQueue.size() < maximumNumberOfProcessesInSingleProcessor || numberOfCPUsHaveMaximumNumberOfProcesses == SimulateController.numberOfProcessors){
                            TimeDetailsLock.getInstance().lock();
                            TimeDetailsLock.specificTimeDetails.computeIfAbsent(TimeListSynchronization.currentTime, k -> new LinkedList<>());
                            TimeDetailsLock.specificTimeDetails.get(TimeListSynchronization.currentTime).add("Process " + GlobalQueue.global.peek().getPid()+" is added to processor "+(this.getProcessorID()-2)+" queue");
                            TimeDetailsLock.getInstance().unlock();
                            this.processorQueue.add(GlobalQueue.global.poll());
                            this.processorQueue.sort(new SRTFComparator());
                            if(runningProcess != null) {
                                Process nextProcess = this.processorQueue.peek();
                                long remainingTime = runningProcess.getCPUBurstTime() - elapsedTime;
                                if (nextProcess.getCPUBurstTime() < remainingTime) {
                                    runningProcess.updateCPUBurst(runningProcess.getCPUBurstTime() - elapsedTime);
                                    runningProcess.setState("Waiting");
                                    this.processorQueue.add(runningProcess);
                                    TimeDetailsLock.getInstance().lock();
                                    TimeDetailsLock.specificTimeDetails.computeIfAbsent(TimeListSynchronization.currentTime, k -> new LinkedList<>());
                                    TimeDetailsLock.specificTimeDetails.get(TimeListSynchronization.currentTime).add("Process " + runningProcess.getPid()+" is preempted from processor"+(this.getProcessorID()-2));
                                    TimeDetailsLock.getInstance().unlock();
                                    busy = false;
                                    addContextSwitch = true;
                                }
                            }
                        }
                        ProcessorsQueuesLock.getInstance().unlock();
                    }
                    GlobalQueue.getInstance().readWriteUnlock();
                    ProcessorsQueuesLock.getInstance().lock();
                    if(!busy && this.processorQueue.size() > 0){
                        runningProcess = this.processorQueue.poll();
                        runningProcess.setState("Running");
                        if(addContextSwitch)
                            runningProcess.setAssignedProcessorAt(time + 1);
                        else
                            runningProcess.setAssignedProcessorAt(time);
                        if(runningProcess.getResponseTime() == -1)
                            runningProcess.setResponseTime(runningProcess.getAssignedProcessorAt() - runningProcess.getArrived());
                        TimeDetailsLock.getInstance().lock();
                        TimeDetailsLock.specificTimeDetails.computeIfAbsent(TimeListSynchronization.currentTime, k -> new LinkedList<>());
                        TimeDetailsLock.specificTimeDetails.get(TimeListSynchronization.currentTime).add("Process " + runningProcess.getPid()+" is added to processor "+(this.getProcessorID()-2));
                        TimeDetailsLock.getInstance().unlock();
                        this.notEfficientTime++;
                        busy = true;
                    }
                    ProcessorsQueuesLock.getInstance().unlock();
                    if(runningProcess != null && busy){
                        if(elapsedTime == runningProcess.getCPUBurstTime()) {
                            System.out.println(runningProcess.getPid()+" "+runningProcess.getCPUBurstTime()+" "+runningProcess.getAssignedProcessorAt()+" "+time);
                            IOScheduler.getInstance().waitIOQueue();
                            runningProcess.updateCPUBurst(runningProcess.getCPUBurstTime() - elapsedTime);
                            runningProcess.setState("Waiting");
                            IOLock.addToIOQueue(runningProcess);
                            TimeDetailsLock.getInstance().lock();
                            TimeDetailsLock.specificTimeDetails.computeIfAbsent(TimeListSynchronization.currentTime, k -> new LinkedList<>());
                            TimeDetailsLock.specificTimeDetails.get(TimeListSynchronization.currentTime).add("Process " + runningProcess.getPid()+" is added to IO queue");
                            TimeDetailsLock.getInstance().unlock();
                            IOScheduler.getInstance().signalIOQueue();
                            runningProcess = null;
                            busy = false;
                        }
                    }
                    TimeListSynchronization.getInstance().waitTimeList();
                    TimeListSynchronization.ThreadReadTimeVariable.set(this.processorID, false);
                    TimeListSynchronization.getInstance().signalTimeList();
                    continue;
                }
                TimeListSynchronization.getInstance().signalTimeList();
            } catch (NullPointerException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        if(SimulateController.algo.equals("RR"))
            RR();
        else if(SimulateController.algo.equals("SRTF"))
            SRTF();
    }

}