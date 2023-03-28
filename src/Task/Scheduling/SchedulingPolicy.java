package Task.Scheduling;

public enum SchedulingPolicy {
    FCFS("First Come First Served (FCFS)"),
    RoundRobin("Round Robin"),
    NonPreemptive("Non-Preemptive"),
    Preemptive("Preemptive");

    public final String Name;

    SchedulingPolicy(String name) {
        this.Name = name;
    }
}
