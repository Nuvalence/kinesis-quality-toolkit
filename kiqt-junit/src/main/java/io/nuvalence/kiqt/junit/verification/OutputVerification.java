package io.nuvalence.kiqt.junit.verification;

import io.nuvalence.kiqt.core.outputs.Reader;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.rules.ErrorCollector;

import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.hamcrest.Matcher;

/**
 * Wraps an output for performing assertions.
 *
 * @param <TOutput> output record type
 */
public class OutputVerification<TOutput> {
    private Reader<TOutput> output;
    private ErrorCollector collector;
    private Long timeout;

    /**
     * Creates an output harness wrapping the specified output within the test setup.
     *
     * @param output output reader representing an output of the application
     */
    public OutputVerification(Reader<TOutput> output) {
        this.output = output;
    }

    /**
     * Configures timeout for assertions made on this output.
     * Once configured, assertions on this output will be retried periodically
     * until either the assertion succeeds or the timeout is exceeded.
     *
     * @param millis timeout, in milliseconds
     * @return this
     */
    public OutputVerification<TOutput> within(Long millis) {
        this.timeout = millis;
        return this;
    }

    /**
     * Configures a timeout with the specified time unit.
     *
     * @param value    timeout value
     * @param timeUnit time unit
     * @return this
     * @see OutputVerification#within(Long)
     */
    public OutputVerification<TOutput> within(long value, TimeUnit timeUnit) {
        return within(new Duration(value, timeUnit).getValueInMS());
    }

    /**
     * Configures a collector for assertions on this reader so that tests will
     * continue on assertion error(s).
     *
     * @param collector error collector
     * @return this
     */
    public OutputVerification<TOutput> whileContinuingOnErrors(ErrorCollector collector) {
        this.collector = collector;
        return this;
    }

    /**
     * Asserts that the given in the output stream match the given predicate.
     *
     * @param reason  description of assertion / reason for failure
     * @param matcher output matcher
     * @return this
     * @throws IOException on error retrieving records
     */
    public OutputVerification<TOutput> should(
        String reason,
        Matcher<? super Collection<TOutput>> matcher
    ) throws IOException {
        return this.should(output -> Assert.assertThat(reason, output, matcher));
    }

    /**
     * Free form assertion on the output records.
     *
     * @param consumer custom assertion logic, consuming a list of output records
     * @return this
     * @throws IOException on error retrieving output records
     */
    public OutputVerification<TOutput> should(Consumer<Collection<TOutput>> consumer) throws IOException {
        try {
            if (timeout == null) {
                consumer.accept(output.getRecords());
            } else {
                Awaitility.await().atMost(timeout, TimeUnit.MILLISECONDS)
                    .untilAsserted(() -> consumer.accept(output.getRecords()));
            }
        } catch (Throwable t) {
            if (collector != null) {
                collector.addError(t);
            } else {
                throw t;
            }
        }
        return this;
    }

}
