package Task.Scheduling;

import Task.Base.Task;
import Task.threads.taskThread;
import Task.threads.dispatcherThread;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

public final class SchedulingTask extends Task {
    //dispatch thread while loop condition
    public static int taskCounter;//keep track of how many tasks
    public static int[] arrivalTime;//times threads arrive in the ready queue
    public static int timeCount;//keep track of time the program has been running(to compare with arrival times)
    public static boolean interrupted;//whether a run has been interrupted
    public static int interruptTime=0;
    //separated the info from the thread itself, this simulates the queue
    public static ArrayList<queueData> ReadyQueue=new ArrayList<>();
    //semaphore for adjusting any counter
    public static Semaphore counterSem=new Semaphore(1);
    //semaphore for signaling from task to dispatcher
    public static Semaphore[] taskFinishSem;
    //semaphore for accessing ready queue
    public static Semaphore queueSem=new Semaphore(1);
    //semaphore for signaling from dispatcher to task
    public static Semaphore[] taskStartSem;
    public static int[] allocatedBurst;
    private final Scheduler scheduler;

    public SchedulingTask(SchedulerInfo schedulerInfo) {
        super(schedulerInfo.toString());

        scheduler = new Scheduler(schedulerInfo);
    }

    @Override
    protected void ConfigureTask() {

        // Display scheduler info
        System.out.println(scheduler);

    }

    @Override
    protected void Simulate() {

        int threadCount = ThreadLocalRandom.current().nextInt(1,26);
        arrivalTime=new int[threadCount];
        taskStartSem=new Semaphore[threadCount];
        taskFinishSem=new Semaphore[threadCount];
        taskCounter=threadCount;
        allocatedBurst=new int[threadCount];
        System.out.println("Creating "+threadCount+" New Threads");

        if (Objects.requireNonNull(scheduler.getPolicy()) == SchedulingPolicy.Preemptive) {
            for (int i = 0; i < threadCount; i++) {
                int arrival = ThreadLocalRandom.current().nextInt(0, 200);
                arrivalTime[i] = arrival;
                allocatedBurst[i] = 0;
                taskStartSem[i] = new Semaphore(0);
                taskFinishSem[i] = new Semaphore(0);
                int burst = ThreadLocalRandom.current().nextInt(1, 51);
                taskThread task = new taskThread(i, burst);
                Thread thread = new Thread(task);
                thread.start();
                queueData data = new queueData(burst, i, arrival);
                ReadyQueue.add(data);
            }
        } else {
            for (int i = 0; i < threadCount; i++) {
                arrivalTime[i] = 0;
                allocatedBurst[i] = 0;
                taskStartSem[i] = new Semaphore(0);
                taskFinishSem[i] = new Semaphore(0);
                int burst = ThreadLocalRandom.current().nextInt(1, 51);
                taskThread task = new taskThread(i, burst);
                Thread thread = new Thread(task);
                thread.start();
                queueData data = new queueData(burst, i, 0);
                ReadyQueue.add(data);
            }
        }
        System.out.println("-----------Ready Queue----------------");
        for(int i=0; i<ReadyQueue.size(); i++){
            System.out.println("ID: "+ReadyQueue.get(i).getQueuePlacement()+", Max Burst: "+ReadyQueue.get(i).getMaxBurst()+", Current Burst: "+ReadyQueue.get(i).getRemainingBurst()+", Arrival Time: "+ReadyQueue.get(i).getArrivalTime());
        }
        System.out.println("--------------------------------------");

        if (Objects.requireNonNull(scheduler.getPolicy()) == SchedulingPolicy.RoundRobin) {
            for (int i = 0; i < scheduler.getCores(); i++) {
                System.out.println("Dispatching Core " + (i + 1));
                dispatcherThread dispatch = new dispatcherThread(i, scheduler.getPolicy(), scheduler.getQuantum());
                Thread dthread = new Thread(dispatch);
                dthread.start();
            }
        } else {
            for (int i = 0; i < scheduler.getCores(); i++) {
                System.out.println("Dispatching Core " + (i + 1));
                dispatcherThread dispatch = new dispatcherThread(i, scheduler.getPolicy());
                Thread dthread = new Thread(dispatch);
                dthread.start();
            }
        }

    }
}
