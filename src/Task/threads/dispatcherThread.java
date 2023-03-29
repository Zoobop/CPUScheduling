package Task.threads;

import Task.Scheduling.SchedulingPolicy;

import static Task.Scheduling.SchedulingTask.*;

public class dispatcherThread implements Runnable{


    int coreNum;
    static int count=0;
    int quantum;
    SchedulingPolicy policy;

    //initializer for non RR
    public dispatcherThread(int coreNum, SchedulingPolicy policy){
        this.policy=policy;
        this.quantum=0;
        this.coreNum=coreNum;
    }
    //initializer for RR
    public dispatcherThread(int coreNum,SchedulingPolicy policy, int quantum){
        this.policy=policy;
        this.quantum=quantum;
        this.coreNum=coreNum;
    }

    public void dispatchToCPU(int burst,int placement){
        allocatedBurst[placement]=burst;

    }
    public boolean hasNext(){
        for(int i=0; i<ReadyQueue.size(); i++){
            if(!ReadyQueue.get(i).isRunning()){
                return true;
            }
        }
        return false;
    }

    public boolean hasNextRR(){
        for(int i=0; i<ReadyQueue.size(); i++){
            if(!ReadyQueue.get(i).isRunning()&&!ReadyQueue.get(i).hasRun()){
                return true;
            }
        }
        return false;
    }
    public int getNextRR(){
        int queueplacement=0;
        for(int i=0; i<ReadyQueue.size(); i++){
            if(!ReadyQueue.get(i).isRunning()&&!ReadyQueue.get(i).hasRun()){
                ReadyQueue.get(i).switchRun();
                ReadyQueue.get(i).switchRunning();
                return i;
            }
        }
        return queueplacement;
    }
    public int getNextAvailable(){
        int queueplacement=0;
        for(int i=0; i<ReadyQueue.size(); i++){
            if(!ReadyQueue.get(i).isRunning()){
                ReadyQueue.get(i).switchRunning();
                return i;
            }
        }
        return queueplacement;
    }
    public int getShortest(){
        int shortestBurst=3000;
        int queueplacement=0;
        for(int i=0; i<ReadyQueue.size(); i++){
            if(ReadyQueue.get(i).getRemainingBurst()<shortestBurst&&!ReadyQueue.get(i).isRunning()){
                shortestBurst=ReadyQueue.get(i).getRemainingBurst();
                queueplacement=i;
            }
        }
        ReadyQueue.get(queueplacement).switchRunning();
        return queueplacement;
    }
    public int getPlacement(int placement){
        for(int i=0; i<ReadyQueue.size(); i++){
            if(placement==ReadyQueue.get(i).getQueuePlacement()){
                return i;
            }
        }
        return 0;
    }
    @Override
    public void run() {
        switch(this.policy){
            case FCFS -> {
                while(taskCounter>0) {
                    queueSem.acquireUninterruptibly();
                    if(hasNext()) {
                        int readyplacement = getNextAvailable();
                        int burst = ReadyQueue.get(readyplacement).getMaxBurst();
                        int placement = ReadyQueue.get(readyplacement).getQueuePlacement();
                        ReadyQueue.get(readyplacement).setRemainingBurst(burst);
                        dispatchToCPU(burst, placement);
                        System.out.println("Dispatcher " + this.coreNum + " Dispatches Task " + placement + " For " + burst + " Bursts of time");
                        taskStartSem[placement].release();
                        queueSem.release();
                        taskFinishSem[placement].acquireUninterruptibly();
                        queueSem.acquireUninterruptibly();
                        System.out.println("Task " + placement + " Removed from ready queue");
                        readyplacement=getPlacement(placement);
                        ReadyQueue.remove(readyplacement);
                        queueSem.release();
                        counterSem.acquireUninterruptibly();
                        taskCounter--;
                        counterSem.release();
                    }else{
                        queueSem.release();
                        taskCounter=0;
                    }

                }
            }
            case NonPreemptive -> {
                while(taskCounter>0) {
                    queueSem.acquireUninterruptibly();
                    if(hasNext()) {
                        int readyplacement = getShortest();
                        int burst = ReadyQueue.get(readyplacement).getMaxBurst();
                        int placement = ReadyQueue.get(readyplacement).getQueuePlacement();
                        ReadyQueue.get(readyplacement).setRemainingBurst(burst);
                        dispatchToCPU(burst, placement);
                        System.out.println("Dispatcher " + this.coreNum + " Dispatches Task " + placement + " For " + burst + " Bursts of time");
                        taskStartSem[placement].release();
                        queueSem.release();
                        taskFinishSem[placement].acquireUninterruptibly();
                        queueSem.acquireUninterruptibly();
                        readyplacement=getPlacement(placement);
                        System.out.println("Task " + placement + " Removed from ready queue");
                        ReadyQueue.remove(readyplacement);
                        queueSem.release();
                        counterSem.acquireUninterruptibly();
                        taskCounter--;
                        counterSem.release();
                    }else{
                        queueSem.release();
                        taskCounter=0;
                    }

                }

            }
            case RoundRobin -> {
                while(taskCounter>0) {
                    queueSem.acquireUninterruptibly();
                    if(hasNextRR()) {
                        int readyplacement = getNextRR();
                        int burst = quantum;
                        int placement = ReadyQueue.get(readyplacement).getQueuePlacement();
                        ReadyQueue.get(readyplacement).setRemainingBurst(burst);
                        dispatchToCPU(burst, placement);
                        System.out.println("Dispatcher " + this.coreNum + " Dispatches Task " + placement + " For " + burst + " Bursts of time");
                        taskStartSem[placement].release();
                        queueSem.release();
                        taskFinishSem[placement].acquireUninterruptibly();
                        queueSem.acquireUninterruptibly();
                        readyplacement=getPlacement(placement);
                        if(ReadyQueue.get(readyplacement).getRemainingBurst()<=0){
                            System.out.println("Task " + placement + " Removed from ready queue");
                            ReadyQueue.remove(readyplacement);
                            taskCounter--;
                        }else{
                            ReadyQueue.get(readyplacement).switchRunning();
                        }
                        queueSem.release();
                        counterSem.acquireUninterruptibly();
                        counterSem.release();
                    }else{

                        for(int i=0; i<ReadyQueue.size(); i++){
                            if(ReadyQueue.get(i).hasRun()){
                                counterSem.acquireUninterruptibly();
                                count++;
                                counterSem.release();
                            }
                        }
                        if(count==ReadyQueue.size()){
                            for(int i=0; i<ReadyQueue.size(); i++){
                                ReadyQueue.get(i).switchRun();
                            }
                        }
                        counterSem.acquireUninterruptibly();
                        count=0;
                        counterSem.release();
                        queueSem.release();
                    }

                }

            }
        }
    }
}
