package io.nuvalence.kiqt.core.outputs;

import java.io.IOException;
import java.util.List;

/**
 * Abstraction for an output reader.
 *
 * @param <T> record type
 */
public interface Reader<T> {

    /**
     * Gets a list of records from the output since the last call to this method.
     *
     * @return list of new records in the output.
     * @throws IOException on error fetching records
     */
    List<T> getRecords() throws IOException;

    /**
     * Specifies configuration options for the reader.
     *
     * @param configuration configuration
     */
    void setConfiguration(ReaderConfiguration configuration);
}
