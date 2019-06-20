package io.nuvalence.kiqt.core.inputs;

import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;
import software.amazon.awssdk.services.kinesis.model.PutRecordsResponse;

import java.io.IOException;
import java.util.List;

/**
 * Asynchronous Kinesis stream writer.
 *
 * @param <T> record type
 */
public class StreamWriter<T> implements Writer<T, PutRecordsResponse> {
    private KinesisClient client;
    private String streamName;
    private AbstractPutRecordsRequestTranslator<T> translator;

    /**
     * Creates a writer with the specified stream as a destination.
     *
     * @param streamName      name of destination stream
     * @param entryTranslator translates objects to an entries in a put records request
     */
    public StreamWriter(String streamName, AbstractPutRecordsRequestTranslator<T> entryTranslator) {
        this(KinesisClient.create(), streamName, entryTranslator);
    }

    /**
     * Creates a writer with the specified client and destination stream.
     *
     * @param client     client used to write to destination
     * @param streamName name of destination stream
     * @param translator maps a record to a {@link PutRecordsRequestEntry}
     */
    public StreamWriter(KinesisClient client, String streamName,
                        AbstractPutRecordsRequestTranslator<T> translator) {
        this.client = client;
        this.streamName = streamName;
        this.translator = translator;
    }

    @Override
    public PutRecordsResponse put(List<T> records) throws IOException {
        return client.putRecords(PutRecordsRequest.builder()
            .streamName(streamName)
            .records(translator.toEntries(records))
            .build());
    }
}
