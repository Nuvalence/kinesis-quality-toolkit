package io.nuvalence.kiqt.junit.setup;

import io.nuvalence.kiqt.core.inputs.Writer;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

/**
 * Scenario lifecycle management class for setting up a scenario via an input.
 *
 * @param <TRecord>   type of input record
 * @param <TResponse> write response type
 */
public class InputSetup<TRecord, TResponse> {
    private Writer<TRecord, TResponse> writer;
    private Consumer<TResponse> responseHandler = response -> { };

    /**
     * Wraps the specified writer in the input setup test phase.
     *
     * @param writer used to write data to streaming pipeline
     */
    public InputSetup(Writer<TRecord, TResponse> writer) {
        this.writer = writer;
    }

    /**
     * Wait for the specified number of milliseconds.
     *
     * @param millis number of milliseconds
     * @return this
     * @throws InterruptedException when interrupted while waiting specified time
     */
    public InputSetup<TRecord, TResponse> thenWait(Long millis) throws InterruptedException {
        Thread.sleep(millis);
        return this;
    }

    /**
     * Provides a response handler for processing the underlying writer's given result.
     *
     * @param responseHandler handler
     * @return this
     */
    public InputSetup<TRecord, TResponse> withResponseHandler(Consumer<TResponse> responseHandler) {
        this.responseHandler = responseHandler;
        return this;
    }

    /**
     * Puts input records to the input using the underlying writer.
     *
     * @param records records to write
     * @return this
     * @throws IOException if unable to write the specified records to the input
     */
    public InputSetup<TRecord, TResponse> given(List<TRecord> records) throws IOException {
        this.responseHandler.accept(writer.put(records));
        return this;
    }
}
