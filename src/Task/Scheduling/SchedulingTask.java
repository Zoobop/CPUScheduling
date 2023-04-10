package Task.Scheduling;

import Task.Base.Task;
import Task.Threads.TaskThread;
import Task.Threads.DispatcherThread;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    public static ArrayList<QueueData> ReadyQueue=new ArrayList<>();
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
    private List<Thread> threads;

    public SchedulingTask(SchedulerInfo schedulerInfo) {
        super(schedulerInfo.toString());

        scheduler = new Scheduler(schedulerInfo);
    }

    @Override
    protected void ConfigureTask() {

        // Display scheduler info
        System.out.println(scheduler);

        // Set up [1-25] amount of threads
        final var threadCount = ThreadLocalRandom.current().nextInt(1,26);
        arrivalTime = new int[threadCount];
        taskStartSem = new Semaphore[threadCount];
        taskFinishSem = new Semaphore[threadCount];
        taskCounter = threadCount;
        allocatedBurst = new int[threadCount];
        threads = new ArrayList<>(threadCount);
    }

    @Override
    protected void Simulate() {

        // Display new threads
        System.out.printf("Creating %d New Threads.%n", taskCounter);

        // Initialize threads based on policy
        final var threadCount = taskCounter;
        final var isPreemptive = scheduler.getPolicy() == SchedulingPolicy.Preemptive;

        for (var i = 0; i < threadCount; i++) {
            // Sets the task variables and initializes its semaphores
            arrivalTime[i] = isPreemptive ? ThreadLocalRandom.current().nextInt(0, 200) : 0;
            allocatedBurst[i] = 0;
            taskStartSem[i] = new Semaphore(0);
            taskFinishSem[i] = new Semaphore(0);

            // Creates and runs the task on a thread
            final var burst = ThreadLocalRandom.current().nextInt(1, 51);
            final var thread = new Thread(new TaskThread(i, burst));
            thread.start();
            threads.add(thread);

            // Store queue data and add to ready queue
            final var data = new QueueData(burst, i, arrivalTime[i]);
            ReadyQueue.add(data);
        }

        // Display the current state of the ready queue
        System.out.println("------------- Ready Queue ------------");
        for (final var data : ReadyQueue) {
            System.out.printf("[ID: %d, Max Burst: %d, Current Burst: %d, Arrival Time: %d]%n", data.getQueuePlacement(), data.getMaxBurst(), data.getRemainingBurst(), data.getArrivalTime());
        }
        System.out.println("--------------------------------------");

        // Create and run dispatcher threads
        final var isRoundRobin = scheduler.getPolicy() == SchedulingPolicy.RoundRobin;

        for (var i = 0; i < scheduler.getCores(); i++) {
            System.out.printf("Dispatching Core %d%n", i + 1);
            final var quantum = isRoundRobin ? scheduler.getQuantum() : 0;
            final var dispatcherThread = new Thread(new DispatcherThread(i, scheduler.getPolicy(), quantum));
            dispatcherThread.start();
            threads.add(dispatcherThread);
        }

        System.out.println();

        // Await the end of simulation
        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
