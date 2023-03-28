package Task.SingleCore;

import Task.Base.IScheduler;
import Task.Base.Scheduler;
import Task.Base.Task;
import Task.Utility.SchedulerInfo;

public final class SingleCoreSchedulingTask extends Task {

    private final IScheduler scheduler;

    public SingleCoreSchedulingTask(SchedulerInfo schedulerInfo) {
        super("Single-Core Scheduling (1 Core)");

        scheduler = new Scheduler(schedulerInfo);
    }

    @Override
    protected void ConfigureTask() {

        // Display scheduler info
        System.out.println(scheduler);

    }

    @Override
    protected void Simulate() {

        // Simulation
        System.out.println("Simulation...");

    }
}
