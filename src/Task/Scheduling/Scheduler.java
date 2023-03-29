package Task.Scheduling;

import Task.Base.IScheduler;

public final class Scheduler implements IScheduler {

    private final SchedulerInfo _info;

    public Scheduler(SchedulerInfo info) {
        _info = info;
    }

    @Override
    public String toString() {
        return "%s%nCores: %d%nQuantum: %d".formatted(_info.Policy.Name, _info.Cores, _info.Quantum);
    }
    public SchedulingPolicy getPolicy(){
        return this._info.Policy;
    }
    public int getCores(){
        return this._info.Cores;
    }
    public int getQuantum(){
        return this._info.Quantum;
    }
}
