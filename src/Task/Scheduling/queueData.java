package Task.Scheduling;

import Task.threads.taskThread;

public class queueData {
    int maxBurst;
    int remainingBurst;
    int queuePlacement;
    boolean run=false;

    boolean running=false;
    queueData(int burst, int placement){
        this.maxBurst=burst;
        this.remainingBurst=burst;
        this.queuePlacement=placement;
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
