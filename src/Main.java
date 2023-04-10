import Task.Scheduling.SchedulerInfo;
import Task.Scheduling.SchedulingPolicy;
import Task.Scheduling.SchedulingTask;
import Task.Base.Task;

import java.util.Objects;

public final class Main {



    private static SchedulerInfo ParseArgs(String[] args) throws Exception {

        SchedulingPolicy schedulePolicy = null;
        Integer cores = null;
        Integer quantum = null;

        // Parse through command line arguments
        var i = 0;
        while (i < args.length) {

            switch (args[i++]) {
                case "-S" -> {
                    // Throw if argument already accounted for
                    if (schedulePolicy != null) throw new Exception();

                    // Get policy from int
                    final var policyArg = args[i++];
                    schedulePolicy = SchedulingPolicy.values()[Integer.parseInt(policyArg)-1];

                    // Get time quantum argument, if applicable
                    if (Objects.equals(policyArg, "2")) {
                        quantum = Integer.parseInt(args[i++]);

                        // Throw if not within bounds [2-10]
                        if (quantum < 2 || quantum > 10)
                            throw new Exception();
                    }
                }
                case "-C" -> {
                    // Throw if argument accounted for
                    if (cores != null) throw new Exception();

                    // Get core count as int
                    final var coreArg = args[i++];
                    cores = Integer.parseInt(coreArg);

                    // Throw if not within bounds [1-4]
                    if (cores < 1 || cores > 4)
                        throw new Exception();
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

            // Create scheduling task from info
            currentTask = new SchedulingTask(schedulerInfo);
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