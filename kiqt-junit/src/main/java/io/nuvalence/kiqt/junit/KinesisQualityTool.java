package io.nuvalence.kiqt.junit;

import io.nuvalence.kiqt.core.errors.AbstractErrorModel;
import io.nuvalence.kiqt.core.inputs.JsonPutRecordsRequestEntryTranslator;
import io.nuvalence.kiqt.core.inputs.StreamWriter;
import io.nuvalence.kiqt.core.inputs.WriterProvider;
import io.nuvalence.kiqt.core.kda.ApplicationIOProvider;
import io.nuvalence.kiqt.core.outputs.AbstractKinesisRecordTranslator;
import io.nuvalence.kiqt.core.outputs.DefaultReaderProvider;
import io.nuvalence.kiqt.core.outputs.JsonRecordTranslator;
import io.nuvalence.kiqt.core.outputs.KinesisStreamReader;
import io.nuvalence.kiqt.core.outputs.ReaderConfiguration;
import io.nuvalence.kiqt.core.outputs.ReaderProvider;
import io.nuvalence.kiqt.core.resources.AwsResource;
import io.nuvalence.kiqt.junit.setup.InputSetup;
import io.nuvalence.kiqt.junit.verification.OutputVerification;

import software.amazon.awssdk.services.kinesis.model.PutRecordsResponse;

import java.time.Instant;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Sets up a scenario for testing a streaming application.
 */
public class KinesisQualityTool {
    protected ApplicationIOProvider application;
    private ObjectMapper objectMapper = new ObjectMapper();
    private ReaderProvider readerProvider;

    /**
     * Creates a scenario for the given application.
     *
     * @param application analytics application wrapper
     */
    public KinesisQualityTool(ApplicationIOProvider application) {
        this.application = application;
        ReaderConfiguration readerConfiguration = new ReaderConfiguration();
        readerConfiguration.setStartTime(Instant.now());
        readerProvider = new DefaultReaderProvider(readerConfiguration);
    }

    /**
     * Gets the default object mapper for this scenario.
     *
     * @return object mapper
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * Sets the default object mapper for the test scenario.
     *
     * @param objectMapper object mapper to be used in this scenario.
     */
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Configure {@link ReaderProvider} used to create a reader from an {@link AwsResource}.
     * Defaults to a {@link KinesisStreamReader#KinesisStreamReader(String, AbstractKinesisRecordTranslator)}
     * using the default client and {@link JsonRecordTranslator}
     *
     * @param provider custom provider
     */
    public void setReaderProvider(ReaderProvider provider) {
        this.readerProvider = provider;
    }

    /**
     * Creates a writer for the input and initializes a {@link InputSetup} with
     * the desired input and output types.
     *
     * @param <TRecord>      input record type
     * @param <TResponse>    write response type
     * @param writerProvider provides a writer for the input
     * @return set up test scenario
     */
    public <TRecord, TResponse> InputSetup<TRecord, TResponse> theInput(
        WriterProvider<TRecord, TResponse> writerProvider
    ) {
        return new InputSetup<>(writerProvider.get(application.getInput(), objectMapper));
    }

    /**
     * Creates a writer the input kinesis stream using {@link StreamWriter} with
     * a {@link JsonPutRecordsRequestEntryTranslator}
     * using a constant partition key and the configured object mapper.
     *
     * @param <TRecord> input record type
     * @return set up test scenario
     */
    public <TRecord> InputSetup<TRecord, PutRecordsResponse> theInputStream() {
        return theInput((resource, mapper) -> {
            JsonPutRecordsRequestEntryTranslator<TRecord> translator =
                new JsonPutRecordsRequestEntryTranslator<>(objectMapper, o -> "0");
            return new StreamWriter<>(resource.getResource(), translator);
        });
    }

    /**
     * Creates a reader for the output and initializes an {@link OutputVerification}
     * with the desired output type.
     *
     * @param name              name of output
     * @param outputRecordClass output record class
     * @param <TOutputRecord>   output record type
     * @return test scenario verification phase
     */
    public <TOutputRecord> OutputVerification<TOutputRecord> theOutput(String name,
                                                                       Class<TOutputRecord> outputRecordClass) {
        AwsResource output = application.getOutput(name);
        return new OutputVerification<>(readerProvider.get(output, objectMapper, outputRecordClass));
    }

    /**
     * Creates a reader for the aws-provided error stream output and initializes an {@link OutputVerification}.
     *
     * @return test scenario assert
     */
    public OutputVerification<AbstractErrorModel> theErrorOutput() {
        return theErrorOutput(AbstractErrorModel.class);
    }

    /**
     * Creates a reader for the aws-provided error stream output and initializes an {@link OutputVerification}.
     *
     * @param errorModelClass class for customized implementation of {@link AbstractErrorModel}
     * @param <T>             type of customized implementation of {@link AbstractErrorModel}
     * @return test scenario assert
     */
    public <T extends AbstractErrorModel> OutputVerification<T> theErrorOutput(Class<T> errorModelClass) {
        return theOutput("error_stream", errorModelClass);
    }
}
