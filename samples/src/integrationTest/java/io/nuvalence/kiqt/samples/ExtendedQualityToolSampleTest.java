package io.nuvalence.kiqt.samples;

import io.nuvalence.kiqt.samples.models.ComputedTemperature;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import org.hamcrest.Matcher;


/**
 * This class demostrates writing tests using the {@link ExtendedQualityTool}.
 */
public class ExtendedQualityToolSampleTest {
    @Rule
    public ErrorCollector collector = new ErrorCollector();
    private ExtendedQualityTool kiqt;

    @Before
    public void setup() throws InterruptedException {
        kiqt = new ExtendedQualityTool(collector);
        kiqt.givenRunningApplication();
    }

    @After
    public void teardown() throws IOException {
        // at the end of each test, print out all of the output records
        kiqt.theOutput().should(records -> {
            if (records.isEmpty()) {
                System.out.println("the output stream is empty");
            } else {
                System.out.println("the output stream contains:");
                records.forEach(System.out::println);
            }
        });
        kiqt.theErrorOutput().should(records -> {
            if (records.isEmpty()) {
                System.out.println("the error stream is empty");
            } else {
                System.out.println("the error stream contains:");
                records.forEach(System.out::println);
            }
        });
    }

    /**
     * Compare this test to {@link BasicSampleTest#givenSimulatedWeatherSignals_ShouldAccuratelyComputeAvgMinAndMax()}
     * to see the value in extending KiQT to be specific to your use case.
     */
    @Test
    public void givenSimulatedWeatherSignals_ShouldAccuratelyComputeAvgMinAndMax() throws IOException {
        RandomSampleDataGenerator.TestCase testCase = RandomSampleDataGenerator.multiplePostalCodesAndTimes();

        Matcher<Iterable<ComputedTemperature>> expected = testCase.getExpectedOutputsMatcher();

        // given the input data
        kiqt.given(testCase.getInputs());
        // assert the output stream contains the expected results
        kiqt.theOutput().should("expected output stream to contain computed results", expected);
        // assert that there were no errors
        kiqt.shouldHaveNoErrors();
    }

    @Test
    public void givenLateArrivingData_ShouldAccuratelyComputeAvgMinAndMax() throws IOException, InterruptedException {
        List<String> postCodes = Collections.singletonList(RandomSampleDataGenerator.uniquePostalCode());
        List<Long> times = Collections.singletonList(Instant.now().toEpochMilli());
        int deviceCount = 5;

        RandomSampleDataGenerator.TestCase testCase = RandomSampleDataGenerator.generate(postCodes, times, deviceCount);

        // send all but one of the input signals
        kiqt.given(testCase.getInputs().subList(0, deviceCount - 1))
            // simulates the last device sending data at a 30 second delay
            .thenWait(30000L).given(testCase.getInputs().subList(deviceCount - 1, deviceCount));
        kiqt.theOutput()
            // extend the timeout to accommodate for the late arriving data
            .within(60, TimeUnit.SECONDS)
            .should("expected output stream to contain computed results", testCase.getExpectedOutputsMatcher());
        kiqt.shouldHaveNoErrors();
    }
}
