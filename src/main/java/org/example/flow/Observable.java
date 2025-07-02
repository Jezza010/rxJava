package org.example.flow;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class representing a Stream in the Observer pattern.
 * @param <T> The type of items being emitted
 */
public class Stream<T> {
    private final Consumer<Subscriber<T>> source;

    private Stream(Consumer<Subscriber<T>> source) {
        this.source = source;
    }

    /**
     * Creates a new Stream from a source function.
     * @param source The function that defines how the Stream emits items
     * @param <T> The type of items being emitted
     * @return A new Stream instance
     */
    public static <T> Stream<T> create(Consumer<Subscriber<T>> source) {
        return new Stream<>(source);
    }

    /**
     * Subscribes a Subscriber to this Stream and returns a Disposable.
     * @param subscriber The Subscriber to subscribe
     * @return A Disposable that can be used to cancel the subscription
     */
    public Disposable subscribe(Subscriber<T> subscriber) {
        AtomicBoolean disposed = new AtomicBoolean(false);
        try {
            source.accept(new Subscriber<T>() {
                @Override
                public void onNext(T item) {
                    if (!disposed.get()) {
                        subscriber.onNext(item);
                    }
                }

                @Override
                public void onError(Throwable t) {
                    if (!disposed.get()) {
                        subscriber.onError(t);
                    }
                }

                @Override
                public void onComplete() {
                    if (!disposed.get()) {
                        subscriber.onComplete();
                    }
                }
            });
        } catch (Exception e) {
            if (!disposed.get()) {
                subscriber.onError(e);
            }
        }
        return new Disposable() {
            @Override
            public void dispose() {
                disposed.set(true);
            }

            @Override
            public boolean isDisposed() {
                return disposed.get();
            }
        };
    }

    /**
     * Subscribes to this Stream with callbacks for onNext, onError, and onComplete.
     * @param onNext The callback for handling emitted items
     * @param onError The callback for handling errors
     * @param onComplete The callback for handling completion
     * @return A Disposable that can be used to cancel the subscription
     */
    public Disposable subscribe(Consumer<T> onNext, Consumer<Throwable> onError, Runnable onComplete) {
        return subscribe(new Subscriber<T>() {
            @Override
            public void onNext(T item) {
                onNext.accept(item);
            }

            @Override
            public void onError(Throwable t) {
                onError.accept(t);
            }

            @Override
            public void onComplete() {
                onComplete.run();
            }
        });
    }

    /**
     * Transforms the items emitted by this Stream by applying a function to each item.
     * @param mapper The function to apply to each item
     * @param <R> The type of items emitted by the resulting Stream
     * @return A new Stream that emits the transformed items
     */
    public <R> Stream<R> map(Function<T, R> mapper) {
        return new Stream<>(subscriber -> subscribe(
            item -> {
                try {
                    subscriber.onNext(mapper.apply(item));
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            },
            subscriber::onError,
            subscriber::onComplete
        ));
    }

    /**
     * Filters items emitted by this Stream by only emitting those that satisfy a predicate.
     * @param predicate The predicate to apply to each item
     * @return A new Stream that emits only those items that satisfy the predicate
     */
    public Stream<T> filter(Predicate<T> predicate) {
        return new Stream<>(subscriber -> subscribe(
            item -> {
                try {
                    if (predicate.test(item)) {
                        subscriber.onNext(item);
                    }
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            },
            subscriber::onError,
            subscriber::onComplete
        ));
    }

    /**
     * Transforms the items emitted by this Stream into Streams, then flattens the emissions from those into a single Stream.
     * @param mapper A function that returns a Stream for each item emitted by the source Stream
     * @param <R> The type of items emitted by the resulting Stream
     * @return A new Stream that emits the items emitted by the Streams returned by the mapper function
     */
    public <R> Stream<R> flatMap(Function<T, Stream<R>> mapper) {
        return new Stream<>(subscriber -> {
            AtomicBoolean disposed = new AtomicBoolean(false);
            subscribe(
                item -> {
                    if (!disposed.get()) {
                        try {
                            Stream<R> innerStream = mapper.apply(item);
                            innerStream.subscribe(
                                subscriber::onNext,
                                subscriber::onError,
                                () -> {} // Don't complete when inner completes
                            );
                        } catch (Exception e) {
                            subscriber.onError(e);
                        }
                    }
                },
                subscriber::onError,
                subscriber::onComplete
            );
        });
    }

    /**
     * Specifies the Executor on which a Stream will operate.
     * @param executor The Executor to use
     * @return A new Stream that operates on the specified Executor
     */
    public Stream<T> subscribeOn(Scheduler executor) {
        return new Stream<>(subscriber -> executor.execute(() -> subscribe(subscriber)));
    }

    /**
     * Specifies the Executor on which a Subscriber will observe this Stream.
     * @param executor The Executor to use
     * @return A new Stream that is observed on the specified Executor
     */
    public Stream<T> observeOn(Scheduler executor) {
        return new Stream<>(subscriber -> subscribe(
            item -> executor.execute(() -> subscriber.onNext(item)),
            error -> executor.execute(() -> subscriber.onError(error)),
            () -> executor.execute(subscriber::onComplete)
        ));
    }
} 