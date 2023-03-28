package Task.Utility;

public final class SchedulerInfo {

    public final SchedulingPolicy Policy;
    public final int Cores;
    public final int Quantum;

    public SchedulerInfo(SchedulingPolicy policy, int cores, int quantum) {
        Policy = policy;
        Cores = cores;
        Quantum = quantum;
    }
}
