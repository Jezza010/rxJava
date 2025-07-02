package org.example;

import org.example.flow.Disposable;
import org.example.flow.Stream;
import org.example.flow.schedulers.ComputationScheduler;
import org.example.flow.schedulers.IOThreadScheduler;
import org.example.flow.schedulers.SingleThreadScheduler;

import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        // Create schedulers
        IOThreadScheduler ioScheduler = new IOThreadScheduler();
        ComputationScheduler computationScheduler = new ComputationScheduler();
        SingleThreadScheduler singleThreadScheduler = new SingleThreadScheduler();

        // Example 1: Using flatMap to transform and flatten
        System.out.println("\nExample 1: Using flatMap");
        Stream<Integer> numbers = Stream.create(subscriber -> {
            try {
                for (int i = 1; i <= 3; i++) {
                    subscriber.onNext(i);
                }
                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        });

        Disposable disposable = numbers
            .flatMap(number -> Stream.create(subscriber -> {
                try {
                    // Simulate some work
                    Thread.sleep(100);
                    subscriber.onNext(number * 10);
                    subscriber.onNext(number * 20);
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }))
            .subscribe(
                item -> System.out.println("FlatMap result: " + item),
                error -> System.out.println("Error: " + error.getMessage()),
                () -> System.out.println("FlatMap completed!")
            );

        // Example 2: Error handling
        System.out.println("\nExample 2: Error handling");
        Stream.create(subscriber -> {
            try {
                subscriber.onNext(1);
                subscriber.onNext(2);
                throw new RuntimeException("Simulated error");
            } catch (Exception e) {
                subscriber.onError(e);
            }
        })
        .subscribe(
            item -> System.out.println("Received: " + item),
            error -> System.out.println("Error handled: " + error.getMessage()),
            () -> System.out.println("This won't be called due to error")
        );

        // Example 3: Disposable usage
        System.out.println("\nExample 3: Disposable usage");
        Stream<Integer> infinite = Stream.create(subscriber -> {
            int i = 0;
            while (true) {
                subscriber.onNext(i++);
                Thread.sleep(100);
            }
        });

        Disposable infiniteDisposable = infinite
            .subscribeOn(ioScheduler)
            .observeOn(computationScheduler)
            .subscribe(
                item -> System.out.println("Infinite: " + item),
                error -> System.out.println("Error: " + error.getMessage()),
                () -> System.out.println("This won't be called")
            );

        // Wait for a while and then dispose
        try {
            Thread.sleep(500);
            infiniteDisposable.dispose();
            System.out.println("Infinite stream disposed");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Wait for all operations to complete
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
} 