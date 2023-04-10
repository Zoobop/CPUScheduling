package Task.Threads;

import Task.Scheduling.SchedulingPolicy;

import static Task.Scheduling.SchedulingTask.*;

public final class DispatcherThread implements Runnable{

    private final int coreNum;//number of cores
    private static int count=0;//keep track of what is in the ready queue
    private final int quantum;//time period for round-robin
    private final SchedulingPolicy policy;//which algorithm to use

    public DispatcherThread(int coreNum, SchedulingPolicy policy, int quantum) {
        this.policy=policy;
        this.quantum=quantum;
        this.coreNum=coreNum;
    }

    public void dispatchToCPU(int burst, int placement) {
        allocatedBurst[placement] = burst;
    }

    public boolean hasNext() {
        for (final var queueData : ReadyQueue) {
            if (!queueData.isRunning()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasNextRR() {
        for (final var queueData : ReadyQueue) {
            if (!queueData.isRunning() && !queueData.hasRun()) {
                return true;
            }
        }
        return false;
    }

    public int getNextRR() {
        for(var i = 0; i < ReadyQueue.size(); i++) {
            final var data = ReadyQueue.get(i);
            if(!data.isRunning() && !data.hasRun()) {
                data.switchRun();
                data.switchRunning();
                return i;
            }
        }

        // Default
        return 0;
    }

    public boolean hasNextPreempt() {
        for (var queueData : ReadyQueue) {
            if (arrivalTime[queueData.getQueuePlacement()] <= timeCount) {
                return true;
            }
        }
        return false;
    }

    public int getNextPreempt() {
        var shortest = 5000;
        var queuePlacement = 0;
        for(var i = 0; i < ReadyQueue.size(); i++) {
            final var data = ReadyQueue.get(i);
            if(arrivalTime[data.getQueuePlacement()] <= timeCount) {
                if(data.getRemainingBurst() < shortest){
                    shortest = data.getRemainingBurst();
                    queuePlacement=i;
                }
            }
        }
        return queuePlacement;
    }

    public int getNextAvailable() {
        for(var i = 0; i < ReadyQueue.size(); i++) {
            final var data = ReadyQueue.get(i);
            if(!data.isRunning()) {
                data.switchRunning();
                return i;
            }
        }
        return 0;
    }

    public int getShortest() {
        var shortestBurst = 3000;
        var queuePlacement = 0;
        for(var i = 0; i < ReadyQueue.size(); i++) {
            final var data = ReadyQueue.get(i);
            if(data.getRemainingBurst() < shortestBurst && !data.isRunning()){
                shortestBurst = data.getRemainingBurst();
                queuePlacement = i;
            }
        }

        ReadyQueue.get(queuePlacement).switchRunning();
        return queuePlacement;
    }

    public int getPlacement(int placement) {
        for(var i = 0; i < ReadyQueue.size(); i++){
            final var data = ReadyQueue.get(i);
            if(placement == data.getQueuePlacement()){
                return i;
            }
        }
        return 0;
    }

    @Override
    public void run() {
        //First Come, First Served: first to arrive in the ready queue is first on the "cpu"
        //Round Robin: running time on the cpu is determined by a time slice. if a thread does
        //not finish in time, it goes back in the ready queue to run for its remaining time.
        //Non-Preemptive Shortest Job First: Shortest job in the ready queue goes first cannot be switched until it completes
        //Preemptive Shortest Job First: Shortest job in the ready queue goes first and can be kicked out if another,
        //shorter job comes along with a burst time that is less than the thread's remaining time.
        switch (policy) {
            case FCFS -> FirstComeFirstServed();
            case RoundRobin -> RoundRobin();
            case NonPreemptive -> NonPreemptive();
            case Preemptive -> Preemptive();
        }
    }

    private void FirstComeFirstServed() {
        while(taskCounter > 0) {
            queueSem.acquireUninterruptibly();
            if(hasNext()) {

                var readyPlacement = getNextAvailable();
                final var data = ReadyQueue.get(readyPlacement);
                final var burst = data.getMaxBurst();
                final var placement = data.getQueuePlacement();
                data.setRemainingBurst(burst);

                dispatchToCPU(burst, placement);
                System.out.printf("Dispatcher %d dispatches Task %d for %d Bursts of Time.%n", coreNum, placement, burst);

                taskStartSem[placement].release();
                queueSem.release();

                taskFinishSem[placement].acquireUninterruptibly();
                queueSem.acquireUninterruptibly();

                System.out.printf("Task %d removed from ready queue.%n", placement);
                readyPlacement = getPlacement(placement);
                ReadyQueue.remove(readyPlacement);

                queueSem.release();

                counterSem.acquireUninterruptibly();
                taskCounter--;
                counterSem.release();
            } else {
                queueSem.release();
                taskCounter = 0;
            }
        }
    }

    private void RoundRobin() {
        while(taskCounter > 0) {
            queueSem.acquireUninterruptibly();
            if(hasNextRR()) {

                var readyPlacement = getNextRR();
                final var data = ReadyQueue.get(readyPlacement);
                final var burst = quantum;
                final var placement = data.getQueuePlacement();
                data.setRemainingBurst(burst);

                dispatchToCPU(burst, placement);
                System.out.printf("Dispatcher %d dispatches Task %d for %d Bursts of Time.%n", coreNum, placement, burst);

                taskStartSem[placement].release();
                queueSem.release();

                taskFinishSem[placement].acquireUninterruptibly();
                queueSem.acquireUninterruptibly();

                readyPlacement = getPlacement(placement);
                final var newData = ReadyQueue.get(readyPlacement);
                if(newData.getRemainingBurst() <= 0) {
                    System.out.printf("Task %d removed from ready queue.%n", placement);
                    ReadyQueue.remove(readyPlacement);
                    taskCounter--;
                }else{
                    newData.switchRunning();
                }

                queueSem.release();
            }else{

                for (final var queueData : ReadyQueue) {
                    if (queueData.hasRun()) {
                        counterSem.acquireUninterruptibly();
                        count++;
                        counterSem.release();
                    }
                }

                if(count == ReadyQueue.size()) {
                    for (final var queueData : ReadyQueue) {
                        queueData.switchRun();
                    }
                }

                counterSem.acquireUninterruptibly();
                count = 0;
                counterSem.release();

                queueSem.release();
            }

        }
    }

    private void NonPreemptive() {
        while(taskCounter > 0) {
            queueSem.acquireUninterruptibly();
            if(hasNext()) {

                var readyPlacement = getShortest();
                final var data = ReadyQueue.get(readyPlacement);
                final var burst = data.getMaxBurst();
                final var placement = data.getQueuePlacement();
                data.setRemainingBurst(burst);

                dispatchToCPU(burst, placement);
                System.out.printf("Dispatcher %d dispatches Task %d for %d Bursts of Time.%n", coreNum, placement, burst);

                taskStartSem[placement].release();
                queueSem.release();

                taskFinishSem[placement].acquireUninterruptibly();
                queueSem.acquireUninterruptibly();

                System.out.printf("Task %d removed from ready queue.%n", placement);
                readyPlacement = getPlacement(placement);
                ReadyQueue.remove(readyPlacement);

                queueSem.release();

                counterSem.acquireUninterruptibly();
                taskCounter--;
                counterSem.release();
            }else{
                queueSem.release();
                taskCounter = 0;
            }
        }
    }

    private void Preemptive() {
        while(taskCounter > 0){
            queueSem.acquireUninterruptibly();

            System.out.printf("Time Count: %d%n", timeCount);
            // Display the current state of the ready queue
            System.out.println("------------- Ready Queue ------------");
            for (final var data : ReadyQueue) {
                System.out.printf("[ID: %d, Max Burst: %d, Current Burst: %d, Arrival Time: %d]%n", data.getQueuePlacement(), data.getMaxBurst(), data.getRemainingBurst(), data.getArrivalTime());
            }
            System.out.println("--------------------------------------");

            if(hasNextPreempt()) {
                final var readyPlacement = getNextPreempt();
                final var data = ReadyQueue.get(readyPlacement);
                final var burst = data.getRemainingBurst();
                final var placement = data.getQueuePlacement();

                dispatchToCPU(burst, placement);
                System.out.printf("Dispatcher %d dispatches Task %d for %d Bursts of Time.%n", coreNum, placement, burst);

                taskStartSem[placement].release();
                queueSem.release();

                taskFinishSem[placement].acquireUninterruptibly();
                queueSem.acquireUninterruptibly();

                if(interrupted) {
                    System.out.printf("Task %d interrupted after %d Bursts.%n", placement, interruptTime);
                    System.out.printf("Dispatcher %d checking Ready Queue.%n", coreNum);
                    data.setRemainingBurst(interruptTime);
                    interrupted=false;
                }
                else{
                    System.out.printf("Task %d finished and removed from Ready Queue.%n", placement);
                    ReadyQueue.remove(readyPlacement);
                    taskCounter--;
                }

            }else{
                var smallest = 5000;
                for (final var queueData : ReadyQueue) {
                    if (queueData.getArrivalTime() < smallest) {
                        smallest = queueData.getArrivalTime();
                    }
                }

                final var difference = smallest - timeCount;
                System.out.printf("Dispatcher waits for %d Bursts.%n", difference);
                timeCount = smallest;
            }

            queueSem.release();

        }
    }
}
