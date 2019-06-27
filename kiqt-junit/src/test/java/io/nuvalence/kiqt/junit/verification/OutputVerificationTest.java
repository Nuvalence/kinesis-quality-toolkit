package io.nuvalence.kiqt.junit.verification;

import io.nuvalence.kiqt.core.outputs.Reader;
import io.nuvalence.kiqt.core.outputs.ReaderConfiguration;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import com.google.common.collect.ImmutableList;
import org.awaitility.core.ConditionTimeoutException;
import org.hamcrest.Matchers;
import org.mockito.Mockito;

public class OutputVerificationTest {

    @Test
    public void within_GivenValueAndUnits_ShouldDelegateToWithinMilliseconds() {
        FakeReader reader = new FakeReader(Collections::emptyList);
        OutputVerification<String> spy = Mockito.spy(new OutputVerification<>(reader));
        spy.within(2, TimeUnit.MINUTES);

        Mockito.verify(spy).within(120000L);
    }

    @Test
    public void should_GivenMatcher_ShouldAssertReaderRecordsMatch() throws IOException {
        List<String> records = ImmutableList.of(UUID.randomUUID().toString());
        new OutputVerification<>(new FakeReader(() -> records))
            .should("contain expected records", Matchers.equalTo(records));
    }

    @Test
    public void should_GivenCollector_ShouldAddErrorToCollector() throws Throwable {
        FakeErrorCollector collector = new FakeErrorCollector();

        new OutputVerification<>(new FakeReader(Collections::emptyList))
            .whileContinuingOnErrors(collector)
            .should("expected failure", Matchers.not(Collections.emptyList()));

        try {
            collector.verify();
            Assert.fail("expected assertion error");
        } catch (AssertionError cte) {
            // expected`
        }
    }

    @Test
    public void should_GivenTimeout_ShouldWaitConfiguredTime() throws IOException {
        List<String> expected = ImmutableList.of(UUID.randomUUID().toString());
        long start = System.currentTimeMillis();
        FakeReader reader = new FakeReader(() -> {
            if (System.currentTimeMillis() - start > 100) {
                return expected;
            } else {
                return Collections.emptyList();
            }
        });

        new OutputVerification<>(reader)
            .within(300L)
            .should("expected failure", Matchers.equalTo(expected));
    }

    @Test
    public void should_GivenTimeoutAndErrorCollector_ShouldWaitConfiguredTime() throws Throwable {
        List<String> expected = ImmutableList.of(UUID.randomUUID().toString());
        FakeErrorCollector collector = new FakeErrorCollector();

        new OutputVerification<>(FakeReader.delay(500, expected))
            .whileContinuingOnErrors(collector)
            .within(300L)
            .should("expected failure", Matchers.equalTo(expected));

        try {
            collector.verify();
            Assert.fail("expected timeout");
        } catch (ConditionTimeoutException cte) {
            // expected`
        }
    }

    @Test(expected = ConditionTimeoutException.class)
    public void should_GivenTimeoutAndNoCollector_ShouldWaitAndThrowTimeoutException() throws Throwable {
        List<String> expected = ImmutableList.of(UUID.randomUUID().toString());

        new OutputVerification<>(FakeReader.delay(500, expected))
            .within(300L)
            .should("expected failure", Matchers.equalTo(expected));

        try {
            Assert.fail("expected timeout");
        } catch (ConditionTimeoutException cte) {
            // expected`
        }
    }

    private static class FakeErrorCollector extends ErrorCollector {
        public void verify() throws Throwable {
            super.verify();
        }
    }

    private static class FakeReader implements Reader<String> {
        private Supplier<List<String>> supplier;

        public FakeReader(Supplier<List<String>> supplier) {
            this.supplier = supplier;
        }

        public static FakeReader delay(long delayMillis, List<String> records) {
            long byTime = System.currentTimeMillis() + delayMillis;
            return new FakeReader(() -> {
                if (System.currentTimeMillis() > byTime) {
                    return records;
                } else {
                    return Collections.emptyList();
                }
            });
        }

        @Override
        public List<String> getRecords() {
            return supplier.get();
        }

        @Override
        public void setConfiguration(ReaderConfiguration configuration) {
            // no-op
        }
    }
}
