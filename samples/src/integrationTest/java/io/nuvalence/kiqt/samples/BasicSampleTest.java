package io.nuvalence.kiqt.samples;

import io.nuvalence.kiqt.core.kda.ApplicationIOProvider;
import io.nuvalence.kiqt.core.kda.ApplicationLifecycleManager;
import io.nuvalence.kiqt.junit.KinesisQualityTool;
import io.nuvalence.kiqt.samples.models.ComputedTemperature;
import io.nuvalence.kiqt.samples.models.WeatherSignal;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import org.hamcrest.Matchers;

/**
 * A very basic example of using the KiQT without extending or customizing it.
 */
public class BasicSampleTest {
    @Rule
    public ErrorCollector collector = new ErrorCollector();
    private KinesisQualityTool kiqt;

    @Before
    public void setup() throws InterruptedException {
        // configure KiQT
        String applicationName = SampleAppEnvironmentConfiguration.getSampleApplicationName();
        ApplicationIOProvider ioProvider = new ApplicationIOProvider(applicationName);
        kiqt = new KinesisQualityTool(ioProvider);

        // ensure the application is running
        ApplicationLifecycleManager lifecycleManager = new ApplicationLifecycleManager(applicationName);
        lifecycleManager.ensureRunning();
    }

    @Test
    public void givenSimulatedWeatherSignals_ShouldAccuratelyComputeAvgMinAndMax() throws IOException {
        RandomSampleDataGenerator.TestCase testCase =
            RandomSampleDataGenerator.multiplePostalCodesAndTimes();

        // get the application input stream
        kiqt.<WeatherSignal>theInputStream()
            // configure a response handler
            .withResponseHandler(response -> {
                // validate writing records was successful
                long failures = response.failedRecordCount().longValue();
                Assert.assertEquals(0, failures);
            })
            // write records to the stream
            .given(testCase.getInputs());

        // get the named output stream, specifying the output record type
        kiqt.theOutput("OUTPUT_STREAM", ComputedTemperature.class)
            // configure a timeout for awaiting a successful assertion
            .within(30, TimeUnit.SECONDS)
            // collect and report assertion errors at the end of the test
            .whileContinuingOnErrors(collector)
            // assert that the output stream contains the expected records
            .should(
                "expected output stream to contain computed results",
                testCase.getExpectedOutputsMatcher()
            );

        // get the error stream
        kiqt.theErrorOutput()
            .whileContinuingOnErrors(collector)
            // assert that the error stream contains no records
            .should("expected no errors", Matchers.empty());
    }
}
