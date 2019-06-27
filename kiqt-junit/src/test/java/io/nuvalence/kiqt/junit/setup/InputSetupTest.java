package io.nuvalence.kiqt.junit.setup;

import io.nuvalence.kiqt.core.inputs.Writer;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.hamcrest.Matchers;
import org.mockito.Mockito;

@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
public class InputSetupTest {
    private FakeWriter spyWriter = Mockito.spy(new FakeWriter());
    private InputSetup<String, String> setup = new InputSetup<>(spyWriter);

    @Test
    public void given_GivenRecords_ShouldDelegateToWriterPutMethod() throws IOException {
        List<String> expected = ImmutableList.of(UUID.randomUUID().toString());

        setup.given(expected);

        Mockito.verify(spyWriter).put(expected);
    }

    @Test
    public void given_GivenRecordsAndResponseHandler_ShouldInvokeHandlerWithPutResponse() throws IOException {
        List<String> records = ImmutableList.of(UUID.randomUUID().toString());
        AtomicReference<String> result = new AtomicReference<>();
        setup.withResponseHandler(result::set).given(records);
        Assert.assertEquals(records.toString(), result.get());
    }

    @Test
    public void thenWait_GivenMilliseconds_ShouldSleepForConfiguredTime() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        long waitFor = 100L;
        setup.thenWait(waitFor);
        Assert.assertThat(System.currentTimeMillis() - startTime, Matchers.greaterThanOrEqualTo(waitFor));
    }

    private static class FakeWriter implements Writer<String, String> {
        @Override
        public String put(List<String> records) {
            return records.toString();
        }
    }
}
