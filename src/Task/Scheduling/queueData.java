package Task.Scheduling;

import Task.threads.taskThread;

public class queueData {
    int maxBurst;//highest burst time
    int remainingBurst;//how much burst time remains
    int queuePlacement;//where in the ready queue the thread is
    boolean run=false;//whether a thread is running
    int arrivalTime;//time arrived in the ready queue

    boolean running=false;
    queueData(int burst, int placement,int arrivalTime){
        this.maxBurst=burst;
        this.remainingBurst=burst;
        this.queuePlacement=placement;
        this.arrivalTime=arrivalTime;
    }

    public int getRemainingBurst(){
        return this.remainingBurst;
    }
    public int getQueuePlacement(){
        return this.queuePlacement;
    }
    public int getMaxBurst(){
        return this.maxBurst;
    }
    public void setRemainingBurst(int subtract){
        this.remainingBurst=remainingBurst-subtract;
    }
    public int getArrivalTime(){
        return this.arrivalTime;
    }

    public void setArrivalTime(int arrivalTime){
        this.arrivalTime=arrivalTime;
    }
    public boolean hasRun(){
        return this.run;
    }
    public void switchRun(){
        this.run=!this.run;
    }
    public boolean isRunning(){
        return this.running;
    }
    public void switchRunning(){
        this.running=!this.running;
    }

}
