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
            for(int i=0; i<allocatedBurst[placement]; i++){
                if(remainingBurst>0){
                    this.remainingBurst--;
                }
            }
            System.out.println("Task "+placement+" Ran for "+allocatedBurst[placement]+" Cycles");
            taskFinishSem[this.placement].release();
        }
        System.out.println("Task "+this.placement+" Complete");

    }


}
