package Task.threads;

import static Task.Scheduling.SchedulingTask.*;

public class taskThread implements Runnable{

    int remainingBurst;
    int placement;
    public taskThread(int placement, int burst){
        this.placement=placement;
        this.remainingBurst=burst;
    }
    @Override
    public void run() {

        while(remainingBurst>0){
            taskStartSem[this.placement].acquireUninterruptibly();
            interruptTime=0;
            for(int i=0; i<allocatedBurst[placement]; i++){
                if(remainingBurst>0){
                    this.remainingBurst--;
                    interruptTime++;
                    timeCount++;
                    for(int j=0; j<arrivalTime.length; j++){
                        if(timeCount==arrivalTime[j]&&this.remainingBurst!=0){
                            arrivalTime[j]=0;
                            interrupted=true;
                            i=allocatedBurst[placement];
                        }
                    }
                }
            }
            if(!interrupted) {
                System.out.println("Task " + placement + " Ran for " + allocatedBurst[placement] + " Cycles");
            }
            taskFinishSem[this.placement].release();
        }
        System.out.println("Task "+this.placement+" Complete");

    }


}
