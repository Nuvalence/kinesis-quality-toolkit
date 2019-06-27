package io.nuvalence.kiqt.core.outputs;

import software.amazon.awssdk.services.kinesis.model.Record;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Translates kinesis record to generic object.
 *
 * @param <T> output type
 */
public class JsonRecordTranslator<T> extends AbstractKinesisRecordTranslator<T> {
    private ObjectMapper mapper;
    private Class<T> recordType;

    /**
     * Creates a translator using a custom ObjectMapper.
     *
     * @param objectMapper mapper used to deserialize record data
     * @param recordType   output record class
     */
    public JsonRecordTranslator(ObjectMapper objectMapper, Class<T> recordType) {
        this.mapper = objectMapper;
        this.recordType = recordType;
    }

    @Override
    public T toValue(Record record) throws IOException {
        return mapper.readValue(record.data().asByteArray(), recordType);
    }
}
