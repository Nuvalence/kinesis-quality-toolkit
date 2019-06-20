package io.nuvalence.kiqt.samples;

import io.nuvalence.kiqt.core.errors.AbstractErrorModel;
import io.nuvalence.kiqt.core.kda.ApplicationIOProvider;
import io.nuvalence.kiqt.core.kda.ApplicationLifecycleManager;
import io.nuvalence.kiqt.junit.KinesisQualityTool;
import io.nuvalence.kiqt.junit.setup.InputSetup;
import io.nuvalence.kiqt.junit.verification.OutputVerification;
import io.nuvalence.kiqt.samples.models.ComputedTemperature;
import io.nuvalence.kiqt.samples.models.PolymorphicErrorModelMixIn;
import io.nuvalence.kiqt.samples.models.WeatherSignal;

import software.amazon.awssdk.services.kinesis.model.PutRecordsResponse;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.rules.ErrorCollector;

import org.hamcrest.Matchers;


/**
 * Extension of the {@link KinesisQualityTool} specific to the sample
 * application for the sake of brevity in the tests. Extending the KiQT allows
 * you to extend the underlying implementation so that it is specific to your
 * application. For example: creating methods for retrieving specific outputs,
 * setting default timeouts, and configuring serialization.
 */
public class ExtendedQualityTool extends KinesisQualityTool {
    private static final String DEFAULT_OUTPUT_STREAM_NAME = "OUTPUT_STREAM";
    private ErrorCollector errorCollector;
    private String applicationName;

    /**
     * Creates a test scenario for the application specified by system property
     * given the specified error collector.
     *
     * @param errorCollector junit error collector for collecting test failures
     */
    public ExtendedQualityTool(ErrorCollector errorCollector) {
        this(SampleAppEnvironmentConfiguration.getSampleApplicationName(), errorCollector);
    }

    /**
     * Creates a test scenario around the specified application given the specified error collector.
     *
     * @param applicationName application name
     * @param errorCollector  junit error collector for collecting test failures
     */
    public ExtendedQualityTool(String applicationName, ErrorCollector errorCollector) {
        super(new ApplicationIOProvider(applicationName));
        this.applicationName = applicationName;
        this.errorCollector = errorCollector;
        this.getObjectMapper().addMixIn(AbstractErrorModel.class, PolymorphicErrorModelMixIn.class);
    }

    /**
     * Asserts the response indicates zero failed records.
     *
     * @param response put records response
     */
    private static void assertSuccessfulPutRecordsResponse(PutRecordsResponse response) {
        Assert.assertEquals("Failed records: " + response.toString(), 0, response.failedRecordCount().longValue());
    }

    /**
     * Ensures the application is running via {@link ApplicationLifecycleManager#ensureRunning()}.
     *
     * @throws InterruptedException if the thread is interrupted while waiting for the application to start
     */
    public void givenRunningApplication() throws InterruptedException {
        new ApplicationLifecycleManager(applicationName).ensureRunning();
    }

    /**
     * Writes a list of records to the input stream and ensures the put response is successful.
     *
     * @param records records to write to the input stream
     * @return this
     * @throws IOException on error writing records to the stream
     */
    public InputSetup<WeatherSignal, PutRecordsResponse> given(List<WeatherSignal> records) throws IOException {
        return this.<WeatherSignal>theInputStream()
            .withResponseHandler(ExtendedQualityTool::assertSuccessfulPutRecordsResponse)
            .given(records);
    }

    /**
     * The default application output stream.
     *
     * @return an {@link OutputVerification} for the default output stream
     */
    public OutputVerification<ComputedTemperature> theOutput() {
        return this.theOutput(DEFAULT_OUTPUT_STREAM_NAME, ComputedTemperature.class).within(30, TimeUnit.SECONDS);
    }

    @Override
    public <TOutput> OutputVerification<TOutput> theOutput(String name, Class<TOutput> outputClass) {
        return super.theOutput(name, outputClass).whileContinuingOnErrors(errorCollector);
    }

    /**
     * Assert that the application error stream is empty.
     *
     * @throws IOException on error retrieving error stream contents.
     */
    public void shouldHaveNoErrors() throws IOException {
        this.theErrorOutput().should("expected the application to have no errors", Matchers.empty());
    }
}
