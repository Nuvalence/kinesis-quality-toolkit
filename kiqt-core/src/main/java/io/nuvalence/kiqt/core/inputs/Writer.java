package io.nuvalence.kiqt.core.inputs;

import java.io.IOException;
import java.util.List;

/**
 * Interface for writing to a destination.
 *
 * @param <TInput>    record type to be written
 * @param <TResponse> write response type
 */
public interface Writer<TInput, TResponse> {
    /**
     * Put a list of given.
     *
     * @param records list of input given
     * @return write response
     * @throws IOException error writing records
     */
    TResponse put(List<TInput> records) throws IOException;
}
