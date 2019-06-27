package io.nuvalence.kiqt.core.inputs;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;

import java.io.IOException;
import java.util.function.Function;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Translates objects to PutRecordsRequestEntry by json serializing the object to use as data.
 *
 * @param <T> input type
 */
public class JsonPutRecordsRequestEntryTranslator<T> extends AbstractPutRecordsRequestTranslator<T> {
    private ObjectMapper mapper;
    private Function<T, String> partitionKeyProvider;

    /**
     * Creates a translator using the specified mapper and partition key provider.
     *
     * @param objectMapper         object mapper used to serialize objects
     * @param partitionKeyProvider given an object, provides its partition key
     */
    public JsonPutRecordsRequestEntryTranslator(ObjectMapper objectMapper, Function<T, String> partitionKeyProvider) {
        this.mapper = objectMapper;
        this.partitionKeyProvider = partitionKeyProvider;
    }

    @Override
    public PutRecordsRequestEntry toEntry(T item) throws IOException {
        return PutRecordsRequestEntry.builder()
            .partitionKey(partitionKeyProvider.apply(item))
            .data(SdkBytes.fromByteArray(mapper.writeValueAsBytes(item)))
            .build();
    }
}
