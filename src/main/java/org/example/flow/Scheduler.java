package org.example.flow;

/**
 * Interface for scheduling tasks to be executed on different threads (Executor abstraction).
 */
public interface Scheduler {
    /**
     * Schedules a task for execution.
     * @param task The task to be executed
     */
    void execute(Runnable task);
} 