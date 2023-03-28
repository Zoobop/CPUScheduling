import Task.MultiCore.MultiCoreSchedulingTask;
import Task.Utility.SchedulerInfo;
import Task.Utility.SchedulingPolicy;
import Task.SingleCore.SingleCoreSchedulingTask;
import Task.Base.Task;

import java.util.Objects;

public final class Main {

    private static SchedulerInfo ParseArgs(String[] args) throws Exception {

        SchedulingPolicy schedulePolicy = null;
        Integer cores = null;
        Integer quantum = null;

        var i = 0;
        while (i < args.length) {

            switch (args[i++]) {
                case "-S" -> {
                    if (schedulePolicy != null) throw new Exception();

                    final var policyArg = args[i++];
                    schedulePolicy = SchedulingPolicy.values()[Integer.parseInt(policyArg)-1];

                    if (Objects.equals(policyArg, "2")) {
                        quantum = Integer.parseInt(args[i++]);
                    }
                }
                case "-C" -> {
                    if (cores != null) throw new Exception();

                    final var coreArg = args[i++];
                    cores = Integer.parseInt(coreArg);
                }
                default -> throw new IllegalStateException("Unexpected value: " + args[i]);
            }

        }

        final var coresValue = cores != null ? cores : 1;
        final var quantumValue = quantum != null ? quantum : 0;
        return new SchedulerInfo(schedulePolicy, coresValue, quantumValue);
    }

    public static void main(String[] args) {

        Task currentTask;

        try
        {
            // Get scheduler info from command line arguments
            final var schedulerInfo = ParseArgs(args);

            // Get task based on core count
            currentTask = schedulerInfo.Cores > 1 ?
                    new MultiCoreSchedulingTask(schedulerInfo) : // Multi core task
                    new SingleCoreSchedulingTask(schedulerInfo); // Single core task
        }
        catch (Exception ex)
        {
            // Incorrect command line arguments
            System.out.println("Invalid command line arguments! Unable to run scheduler.");
            System.out.println("First-Come First-Served:    -S 1");
            System.out.println("Round Robin:                -S 2 <quantum:2-10>");
            System.out.println("Non-Preemption:             -S 3");
            System.out.println("Preemption:                 -S 4");
            System.out.println("Number of Cores:            -C <cores:1-4> ");
            return;
        }

        // Run task
        currentTask.Run();
    }
}