package Task.Threads;

import static Task.Scheduling.SchedulingTask.*;

public final class TaskThread implements Runnable{

    private final int placement;
    private int remainingBurst;

    public TaskThread(int placement, int burst){
        this.placement = placement;
        this.remainingBurst = burst;
    }

    @Override
    public void run() {

        while(remainingBurst > 0) {
            taskStartSem[placement].acquireUninterruptibly();
            interruptTime=0;
            for(var i = 0; i < allocatedBurst[placement]; i++) {
                if(remainingBurst > 0){
                    this.remainingBurst--;
                    interruptTime++;
                    timeCount++;
                    System.out.println("Task "+placement+" Runs for Cycle "+interruptTime);

                    for(var j = 0; j < arrivalTime.length; j++) {
                        if(timeCount==arrivalTime[j] && remainingBurst != 0){
                            arrivalTime[j] = 0;
                            interrupted = true;
                            i = allocatedBurst[placement];
                        }
                    }
                }
            }
            if(!interrupted) {
                System.out.printf("Task %d ran for %d Cycles.%n", placement, allocatedBurst[placement]);
            }
            taskFinishSem[placement].release();
        }
        System.out.printf("Task %d Complete.%n", placement);

    }


}
