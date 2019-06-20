package io.nuvalence.kiqt.core.inputs;

import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstraction for translating objects entries for use in a
 * {@link software.amazon.awssdk.services.kinesis.model.PutRecordsRequest}.
 *
 * @param <T> input type
 */
public abstract class AbstractPutRecordsRequestTranslator<T> {

    /**
     * Translates item to a put records request entry.
     *
     * @param item input item
     * @return put records request entry
     * @throws IOException if the item cannot be translated
     */
    abstract PutRecordsRequestEntry toEntry(T item) throws IOException;

    /**
     * Converts a list of objects to put records request entries.
     *
     * @param items input items
     * @return list of entries, mapped 1:1 from input
     * @throws IOException if an item cannot be translated
     */
    public List<PutRecordsRequestEntry> toEntries(List<T> items) throws IOException {
        LinkedList<PutRecordsRequestEntry> result = new LinkedList<>();
        if (items != null && !items.isEmpty()) {
            for (T item : items) {
                result.add(toEntry(item));
            }
        }
        return result;
    }
}
