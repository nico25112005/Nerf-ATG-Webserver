/*
	Author: Elias (Gotzi) Gottsbacher
	Copyright (c) 2023 Elias Gottsbacher
*/

package net.nerfatg.task;

public class TaskRunFactory {

    public Runnable delayedTask(Runnable task, long millis) {
        return () -> {
            try {
                Thread.sleep(millis);
                if (!Thread.currentThread().isInterrupted())
                    task.run();
            } catch (InterruptedException ignored) { }
        };
    }

    public Runnable repeatingDelayedTask(Runnable task, long millis) {
        return () -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException ignored) { }
                task.run();
            }
        };
    }

    public Runnable repeatingTask(Runnable task) {
        return () -> {
            while(!Thread.currentThread().isInterrupted()) task.run();
        };
    }

    public Runnable delayedRepeatingTask(Runnable task, long millis) {
        return () -> {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException ignored) { }
            while(!Thread.currentThread().isInterrupted()) task.run();
        };
    }
}