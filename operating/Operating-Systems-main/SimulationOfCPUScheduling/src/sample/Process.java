//the start of the process class
package sample;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;

import java.util.LinkedList;

public class Process implements Comparable<Process> {
    public static LinkedList<Process> processes = new LinkedList<>();
    
    private final SimpleIntegerProperty pid;
    private final LinkedList<Long> CPUBurst;
    private final LinkedList<Long> IOBurst;
    private int currentCPUBurst;
    private int currentIOBurst;
    private final SimpleLongProperty arrived;
    private final SimpleLongProperty finished;
    private final SimpleLongProperty responseTime;
    private final SimpleLongProperty waitTime;
    private String state;
    private boolean CPUTurn;
    private final SimpleLongProperty addedToIODevice;
    private final SimpleLongProperty assignedProcessorAt;
    private final SimpleLongProperty burstsSum;
    private final SimpleLongProperty turnAroundTime;
    private final SimpleIntegerProperty delay;

    public Process() {
        super();

        this.CPUBurst = new LinkedList<>();
        this.IOBurst = new LinkedList<>();
        this.state = "Ready";
        this.CPUTurn = true;
        this.addedToIODevice = new SimpleLongProperty(0);
        this.assignedProcessorAt = new SimpleLongProperty(0);
        this.arrived = new SimpleLongProperty(0);
        this.finished = new SimpleLongProperty(0);
        this.responseTime = new SimpleLongProperty(-1);
        this.waitTime = new SimpleLongProperty(0);
        this.burstsSum = new SimpleLongProperty(0);
        this.delay = new SimpleIntegerProperty(0);
        this.turnAroundTime = new SimpleLongProperty(0);
        this.pid = new SimpleIntegerProperty(0);
    }

    public int getPid() {
        return this.pid.getValue();
    }

    public void setPid(int pid) {
        this.pid.setValue(pid);
    }

    public long getArrived() {
        return this.arrived.getValue();
    }

    public void setArrived(long arrived) {
        this.arrived.setValue(arrived);
    }

    public long getFinished() {
        return this.finished.getValue();
    }

    public void setFinished(long finished) {
        this.finished.setValue(finished);
    }

    public long getResponseTime() {
        return this.responseTime.getValue();
    }

    public void setResponseTime(long responseTime) {
        this.responseTime.setValue(responseTime);
    }

    public long getWaitTime() {
        return this.waitTime.getValue();
    }

    public void setWaitTime(long waitTime) {
        this.waitTime.setValue(waitTime);
    }

    public void insertCPUBurstTime(long value){
        this.CPUBurst.add(value);
    }

    public long getCPUBurstTime(){
        return this.CPUBurst.get(this.currentCPUBurst);
    }

    public long getBurstsSum() {
        return burstsSum.getValue();
    }

    public long getTurnAroundTime() {
        return turnAroundTime.getValue();
    }

    public void setTurnAroundTime(long turnAroundTime) {
        this.turnAroundTime.setValue(turnAroundTime);
    }

    public int getDelay() {
        return delay.getValue();
    }

    public void setDelay(int delay) {
        this.delay.setValue(delay);
    }

    public void setBurstsSum(long burstsSum) {
        this.burstsSum.setValue(burstsSum);
    }

    public void updateCPUBurst(long value){
        this.CPUBurst.set(this.currentCPUBurst, value);
        if(value == 0) {
            this.currentCPUBurst++;
            this.CPUTurn = false;
        }
    }

    public void insertIOBurstTime(long value){
        this.IOBurst.add(value);
    }

    public long getIOBurstTime(){
        return this.IOBurst.get(this.currentIOBurst);
    }

    public void updateIOBurst(long value){
        this.IOBurst.set(this.currentIOBurst, value);
        if(value == 0) {
            this.currentIOBurst++;
            this.CPUTurn = true;
        }
    }

    public long getLastIOBurst(){
        return this.IOBurst.getLast();
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isCPUTurn() {
        return CPUTurn;
    }

    public void setCPUTurn(boolean CPUTurn) {
        this.CPUTurn = CPUTurn;
    }

    public long getAddedToIODevice() {
        return addedToIODevice.getValue();
    }

    public void setAddedToIODevice(long addedToIOQueue) {
        this.addedToIODevice.setValue(addedToIOQueue);
    }

    public long getAssignedProcessorAt() {
        return assignedProcessorAt.getValue();
    }

    public void setAssignedProcessorAt(long assignedProcessorAt) {
        this.assignedProcessorAt.setValue(assignedProcessorAt);
    }

    public void setCurrentCPUBurst(int currentCPUBurst) {
        this.currentCPUBurst = currentCPUBurst;
    }

    public void setCurrentIOBurst(int currentIOBurst) {
        this.currentIOBurst = currentIOBurst;
    }

    public int getNumberOfBursts(){
        return this.CPUBurst.size();
    }

    public void setCPUBurstAt(int index, long value){
        this.CPUBurst.set(index, value);
    }

    public void setIOBurstAt(int index, long value){
        this.IOBurst.set(index, value);
    }

    public long getCPUBurstAt(int index){
        return this.CPUBurst.get(index);
    }

    public long getIOBurstAt(int index){
        return this.IOBurst.get(index);
    }

    @Override
    public int compareTo(Process o) {
        if(o != null){
            if(this.arrived.getValue() > o.arrived.getValue())
                return 1;
            else if(this.arrived.getValue() < o.arrived.getValue())
                return -1;
            return 0;
        }
        return 0;
    }
}