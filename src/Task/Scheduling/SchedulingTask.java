package Task.Scheduling;

import Task.Base.IScheduler;
import Task.Base.Task;

public final class SchedulingTask extends Task {

    private final IScheduler scheduler;

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

        // Simulation
        System.out.println("Simulation...");

    }
}
