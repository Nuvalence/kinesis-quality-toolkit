package io.nuvalence.kiqt.core.outputs;

import io.nuvalence.kiqt.core.resources.AwsResource;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Default reader provider. Currently only supports Kinesis Streams,
 * a custom {@link ReaderProvider} may be implemented to support other types of
 * destinations.
 */
public class DefaultReaderProvider implements ReaderProvider {

    private ReaderConfiguration configuration;

    /**
     * Creates a reader provider with default configuration.
     */
    public DefaultReaderProvider() {
        this(new ReaderConfiguration());
    }

    /**
     * Creates a reader provider with custom configuration.
     *
     * @param configuration configuration options for readers
     */
    public DefaultReaderProvider(ReaderConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public <TOutput> Reader<TOutput> get(AwsResource resource, ObjectMapper mapper, Class<TOutput> recordType) {
        KinesisStreamReader<TOutput> reader = new KinesisStreamReader<>(
            resource.getResource(),
            new JsonRecordTranslator<>(mapper, recordType)
        );
        reader.setConfiguration(configuration);
        return new OutputCache<>(reader, 2500L);
    }
}
