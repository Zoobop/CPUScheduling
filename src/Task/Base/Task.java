package Task.Base;

public abstract class Task {

    private final String name;

    public Task(String name) {
        this.name = name;
    }

    public void Run() {
        System.out.println(name);
        System.out.println("--------------------------------------");

        // Gets the user input
        ConfigureTask();

        System.out.println("--------------------------------------");

        // Start simulation
        Simulate();

        System.out.println("--------------------------------------");
        System.out.println("Ending Program...");
    }

    protected abstract void ConfigureTask();
    protected abstract void Simulate();
}
