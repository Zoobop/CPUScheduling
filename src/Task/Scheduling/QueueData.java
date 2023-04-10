package Task.Scheduling;

public final class QueueData {
    private final int maxBurst;//highest burst time
    private int remainingBurst;//how much burst time remains
    private final int queuePlacement;//where in the ready queue the thread is
    private boolean run;//whether a thread is running
    private int arrivalTime;//time arrived in the ready queue
    private boolean running;

    QueueData(int burst, int placement, int arrivalTime){
        this.maxBurst=burst;
        this.remainingBurst=burst;
        this.queuePlacement=placement;
        this.arrivalTime=arrivalTime;

        run = false;
        running = false;
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
        this.remainingBurst -= subtract;
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
        this.run = !this.run;
    }
    public boolean isRunning(){
        return this.running;
    }
    public void switchRunning(){
        this.running = !this.running;
    }

}
