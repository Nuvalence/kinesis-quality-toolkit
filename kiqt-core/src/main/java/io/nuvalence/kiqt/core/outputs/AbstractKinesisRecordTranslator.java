package io.nuvalence.kiqt.core.outputs;

import software.amazon.awssdk.services.kinesis.model.Record;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Translates a record to generic object.
 *
 * @param <T> record type
 */
public abstract class AbstractKinesisRecordTranslator<T> {

    /**
     * Translates a single record.
     *
     * @param record kinesis record
     * @return translated value
     * @throws IOException if the record cannot be translated
     */
    abstract T toValue(Record record) throws IOException;

    /**
     * Translates a list of records.
     *
     * @param records kinesis records
     * @return values
     * @throws IOException if any record cannot be translated
     */
    public List<T> toValues(List<Record> records) throws IOException {
        List<T> values = new LinkedList<>();
        if (records != null && !records.isEmpty()) {
            for (Record record : records) {
                values.add(this.toValue(record));
            }
        }
        return values;
    }
}
