package Task.threads;

import Task.Scheduling.SchedulingPolicy;

import javax.sound.midi.SysexMessage;

import static Task.Scheduling.SchedulingTask.*;

public class dispatcherThread implements Runnable{


    int coreNum;//number of cores
    static int count=0;//keep track of what is in the ready queue
    int quantum;//time period for round-robin
    SchedulingPolicy policy;//which algorithm to use

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
        int queuePlacement=0;
        for(int i=0; i<ReadyQueue.size(); i++){
            if(!ReadyQueue.get(i).isRunning()&&!ReadyQueue.get(i).hasRun()){
                ReadyQueue.get(i).switchRun();
                ReadyQueue.get(i).switchRunning();
                return i;
            }
        }
        return queuePlacement;
    }
    public boolean hasNextPreempt(){
        for(int i=0; i<ReadyQueue.size(); i++){
            if(arrivalTime[ReadyQueue.get(i).getQueuePlacement()]<=timeCount){
                return true;
            }
        }
        return false;
    }
    public int getNextPreempt(){
        int shortest=5000;
        int queuePlacement=0;
        for(int i=0; i<ReadyQueue.size(); i++){
            if(arrivalTime[ReadyQueue.get(i).getQueuePlacement()]<=timeCount){
                if(ReadyQueue.get(i).getRemainingBurst()<shortest){
                    shortest=ReadyQueue.get(i).getRemainingBurst();
                    queuePlacement=i;
                }
            }
        }
        return queuePlacement;
    }
    public int getNextAvailable(){
        int queuePlacement=0;
        for(int i=0; i<ReadyQueue.size(); i++){
            if(!ReadyQueue.get(i).isRunning()){
                ReadyQueue.get(i).switchRunning();
                return i;
            }
        }
        return queuePlacement;
    }
    public int getShortest(){
        int shortestBurst=3000;
        int queuePlacement=0;
        for(int i=0; i<ReadyQueue.size(); i++){
            if(ReadyQueue.get(i).getRemainingBurst()<shortestBurst&&!ReadyQueue.get(i).isRunning()){
                shortestBurst=ReadyQueue.get(i).getRemainingBurst();
                queuePlacement=i;
            }
        }
        ReadyQueue.get(queuePlacement).switchRunning();
        return queuePlacement;
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
            //First Come, First Served: first to arrive in the ready queue is first on the "cpu"
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
            //Non-Preemptive Shortest Job First: Shortest job in the ready queue goes first cannot be switched until it completes
            case NonPreemptive -> {
                while(taskCounter>0) {
                    queueSem.acquireUninterruptibly();
                    if(hasNext()) {
                        int readyPlacement = getShortest();
                        int burst = ReadyQueue.get(readyPlacement).getMaxBurst();
                        int placement = ReadyQueue.get(readyPlacement).getQueuePlacement();
                        ReadyQueue.get(readyPlacement).setRemainingBurst(burst);
                        dispatchToCPU(burst, placement);
                        System.out.println("Dispatcher " + this.coreNum + " Dispatches Task " + placement + " For " + burst + " Bursts of time");
                        taskStartSem[placement].release();
                        queueSem.release();
                        taskFinishSem[placement].acquireUninterruptibly();
                        queueSem.acquireUninterruptibly();
                        readyPlacement=getPlacement(placement);
                        System.out.println("Task " + placement + " Removed from ready queue");
                        ReadyQueue.remove(readyPlacement);
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
            //Round Robin: running time on the cpu is determined by a time slice. if a thread does
            //not finish in time, it goes back in the ready queue to run for its remaining time.
            case RoundRobin -> {
                while(taskCounter>0) {
                    queueSem.acquireUninterruptibly();
                    if(hasNextRR()) {
                        int readyPlacement = getNextRR();
                        int burst = quantum;
                        int placement = ReadyQueue.get(readyPlacement).getQueuePlacement();
                        ReadyQueue.get(readyPlacement).setRemainingBurst(burst);
                        dispatchToCPU(burst, placement);
                        System.out.println("Dispatcher " + this.coreNum + " Dispatches Task " + placement + " For " + burst + " Bursts of time");
                        taskStartSem[placement].release();
                        queueSem.release();
                        taskFinishSem[placement].acquireUninterruptibly();
                        queueSem.acquireUninterruptibly();
                        readyPlacement=getPlacement(placement);
                        if(ReadyQueue.get(readyPlacement).getRemainingBurst()<=0){
                            System.out.println("Task " + placement + " Removed from ready queue");
                            ReadyQueue.remove(readyPlacement);
                            taskCounter--;
                        }else{
                            ReadyQueue.get(readyPlacement).switchRunning();
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
            //Preemptive Shortest Job First: Shortest job in the ready queue goes first and can be kicked out if another,
            //shorter job comes along with a burst time that is less than the thread's remaining time.
            case Preemptive -> {
                while(taskCounter>0){
                    queueSem.acquireUninterruptibly();
                    System.out.println("Time Count: "+timeCount);
                    System.out.println("-----------Ready Queue----------------");
                    for(int i=0; i<ReadyQueue.size(); i++){
                        System.out.println("ID: "+ReadyQueue.get(i).getQueuePlacement()+", Max Burst: "+ReadyQueue.get(i).getMaxBurst()+", Current Burst: "+ReadyQueue.get(i).getRemainingBurst()+", Arrival Time: "+ReadyQueue.get(i).getArrivalTime());
                    }
                    System.out.println("--------------------------------------");
                    if(hasNextPreempt()){
                        int readyPlacement=getNextPreempt();
                        int burst=ReadyQueue.get(readyPlacement).getRemainingBurst();
                        int placement=ReadyQueue.get(readyPlacement).getQueuePlacement();
                        dispatchToCPU(burst,placement);
                        System.out.println("Dispatcher " + this.coreNum + " Dispatches Task " + placement + " For " + burst + " Bursts of time");
                        taskStartSem[placement].release();
                        queueSem.release();
                        taskFinishSem[placement].acquireUninterruptibly();
                        queueSem.acquireUninterruptibly();
                        if(interrupted){
                            System.out.println("Task "+placement+" interrupted after "+interruptTime+" bursts");
                            System.out.println("Dispatcher "+this.coreNum+" Checking Ready Queue");
                            ReadyQueue.get(readyPlacement).setRemainingBurst(interruptTime);
                            interrupted=false;
                        }
                        else{
                            System.out.println("Task "+placement+" Finished and Removed from Ready Queue");
                            ReadyQueue.remove(readyPlacement);
                            taskCounter--;
                        }
                    }else{
                        int smallest=5000;
                        for(int i=0; i<ReadyQueue.size(); i++){
                            if(ReadyQueue.get(i).getArrivalTime()<smallest){
                                smallest=ReadyQueue.get(i).getArrivalTime();
                            }
                        }
                        int difference=smallest-timeCount;
                        System.out.println("Dispatcher waits for "+difference+" bursts");
                        timeCount=smallest;
                    }
                    queueSem.release();

                }


            }
        }
    }
}
