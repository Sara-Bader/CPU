package sample;

import java.util.Comparator;

public class ArrivalTime implements Comparator<Process> {

    @Override
    public int compare(Process o1, Process o2) {
        if(o1 != null && o2 != null){
            if(o1.getArrived() > o2.getArrived())
                return 1;
            else if(o1.getArrived() < o2.getArrived())
                return -1;
            return 0;
        }
        return 0;
    }
}