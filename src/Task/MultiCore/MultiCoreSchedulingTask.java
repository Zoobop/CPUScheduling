package Task.MultiCore;

import Task.Base.IScheduler;
import Task.Base.Scheduler;
import Task.Base.Task;
import Task.Utility.SchedulerInfo;

public final class MultiCoreSchedulingTask extends Task {

    private final IScheduler scheduler;

    public MultiCoreSchedulingTask(SchedulerInfo schedulerInfo) {
        super("Multi-Core Scheduling (%d Cores)".formatted(schedulerInfo.Cores));

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
