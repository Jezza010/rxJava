package org.example.flow;

/**
 * Interface representing a Subscriber in the Observer pattern.
 * @param <T> The type of items being observed
 */
public interface Subscriber<T> {
    /**
     * Called when the Stream emits an item.
     * @param item The item emitted by the Stream
     */
    void onNext(T item);

    /**
     * Called when the Stream encounters an error.
     * @param t The error that occurred
     */
    void onError(Throwable t);

    /**
     * Called when the Stream has completed emitting items.
     */
    void onComplete();
} 