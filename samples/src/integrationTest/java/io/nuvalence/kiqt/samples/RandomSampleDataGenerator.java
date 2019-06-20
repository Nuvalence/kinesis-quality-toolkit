package io.nuvalence.kiqt.samples;

import io.nuvalence.kiqt.samples.models.ComputedTemperature;
import io.nuvalence.kiqt.samples.models.WeatherSignal;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableList;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

/**
 * Generates random sample data for the sample application.
 */
public class RandomSampleDataGenerator {
    public static final AtomicLong POSTAL_CODE_GENERATOR = new AtomicLong(11111);

    /**
     * Creates a unique postal code to use in a test (we could have multiple tests running
     * in parallel so we want to ensure each has distinct data).
     *
     * @return postal code
     */
    static String uniquePostalCode() {
        return String.valueOf(POSTAL_CODE_GENERATOR.getAndIncrement());
    }

    /**
     * Generates data for a list of records from many devices across
     * multiple postal codes over multiple event times.
     *
     * @return test case defining all generated input and expected outputs
     */
    static TestCase multiplePostalCodesAndTimes() {
        List<String> postCodes = ImmutableList.of(
            uniquePostalCode(),
            uniquePostalCode()
        );
        List<Long> times = ImmutableList.of(
            Instant.now().toEpochMilli(),
            Instant.now().toEpochMilli() + 10000
        );
        return generate(postCodes, times, 2);
    }

    /**
     * Generates inputs and expected outputs for a combination of postal codes,
     * event times, and a number of devices per postal code.
     *
     * @param postalCodes list of postal codes to generate data for
     * @param times       list of times to generate data for
     * @param deviceCount number of devices per postal code
     * @return a test case defining all generated inputs and expected outputs
     */
    static TestCase generate(List<String> postalCodes, List<Long> times, int deviceCount) {
        TestCase testCase = new TestCase();
        Function<String, List<TestDevice>> createDevicesForPostCode = pc -> IntStream.range(0, deviceCount)
            .mapToObj(i -> new TestDevice(pc))
            .collect(Collectors.toList());
        Map<String, List<TestDevice>> devicesByPostalCode = postalCodes.stream()
            .collect(Collectors.toMap(pc -> pc, createDevicesForPostCode));

        for (long time : times) {
            devicesByPostalCode.forEach((pc, devices) -> {
                List<WeatherSignal> inputs = devices.stream()
                    .map(d -> d.randomSignal(time))
                    .collect(Collectors.toList());
                testCase.addInputs(inputs);

                ComputedTemperature expected = new ComputedTemperature();
                expected.setUtcTime(time);
                expected.setPostalCode(pc);
                expected.setMinimum(inputs.stream().mapToDouble(WeatherSignal::getValue).min().getAsDouble());
                expected.setMaximum(inputs.stream().mapToDouble(WeatherSignal::getValue).max().getAsDouble());
                expected.setAverage(inputs.stream().mapToDouble(WeatherSignal::getValue).average().getAsDouble());
                testCase.addExpected(expected);
            });
        }

        return testCase;
    }

    /**
     * Represents inputs and expected outputs for a test case.
     */
    static class TestCase {

        private List<WeatherSignal> inputs = new LinkedList<>();
        private List<ComputedTemperature> expectedOutputs = new LinkedList<>();

        /**
         * Gets the inputs.
         *
         * @return inputs for test case
         */
        List<WeatherSignal> getInputs() {
            return inputs;
        }

        private void addInputs(List<WeatherSignal> additional) {
            inputs.addAll(additional);
        }

        /**
         * Gets the expected outputs.
         *
         * @return expected outputs for test case
         */
        List<ComputedTemperature> getExpectedOutputs() {
            return expectedOutputs;
        }

        private void addExpected(ComputedTemperature expected) {
            expectedOutputs.add(expected);
        }

        /**
         * Gets a matcher for matching to a collection containing all expected outputs.
         *
         * @return expected outputs matcher
         */
        Matcher<Iterable<ComputedTemperature>> getExpectedOutputsMatcher() {
            return Matchers.hasItems(getExpectedOutputs().toArray(new ComputedTemperature[0]));
        }
    }

    private static class TestDevice {
        private static final Random random = new Random();

        private String deviceId;
        private String postalCode;

        private TestDevice(String postalCode) {
            this(UUID.randomUUID().toString(), postalCode);
        }

        private TestDevice(String deviceId, String postalCode) {
            this.deviceId = deviceId;
            this.postalCode = postalCode;
        }

        private WeatherSignal randomSignal(Long timestamp) {
            WeatherSignal ws = new WeatherSignal();
            ws.setDeviceId(deviceId);
            ws.setPostalCode(postalCode);
            ws.setUtcTime(timestamp);
            ws.setValue(40 + random.nextDouble() * 60);
            return ws;
        }
    }
}
