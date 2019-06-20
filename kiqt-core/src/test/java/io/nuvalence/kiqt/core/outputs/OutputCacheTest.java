package io.nuvalence.kiqt.core.outputs;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import org.awaitility.Awaitility;
import org.hamcrest.Matchers;
import org.mockito.Mockito;

public class OutputCacheTest {

    private TestReader readerSpy = Mockito.spy(new TestReader());

    @Test
    public void givenReader_ShouldInvokeGetRecords() throws IOException {
        OutputCache<String> cache = new OutputCache<>(readerSpy, 100L);
        Awaitility.await()
            .atMost(90, TimeUnit.MILLISECONDS)
            .pollInterval(5, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                Assert.assertThat(cache.getRecords(), Matchers.hasSize(1));
            });
        Mockito.verify(readerSpy).getRecords();
    }

    @Test
    public void givenReader_AfterRefreshInterval_ShouldHaveInvokedGetRecordsTwice() throws IOException {
        OutputCache<String> cache = new OutputCache<>(readerSpy, 100L);
        Awaitility.await()
            .atMost(190, TimeUnit.MILLISECONDS)
            .pollInterval(5, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                Assert.assertThat(cache.getRecords(), Matchers.hasSize(2));
            });
        Mockito.verify(readerSpy, Mockito.times(2)).getRecords();
    }

    @Test
    public void setConfiguration_ShouldProxyToUnderlyingReader() {
        ReaderConfiguration mockConfiguration = Mockito.mock(ReaderConfiguration.class);
        OutputCache<String> cache = new OutputCache<>(readerSpy, 100L);
        cache.setConfiguration(mockConfiguration);
        Mockito.verify(readerSpy).setConfiguration(mockConfiguration);
    }

    @Test
    public void givenReader_OnErrorInRefresh_ShouldDelegateToErrorHandler() throws IOException {
        IOException expected = new IOException("EXPECTED EXCEPTION");
        Reader<String> throwingReader = new Reader<String>() {

            @Override
            public List<String> getRecords() throws IOException {
                throw expected;
            }

            @Override
            public void setConfiguration(ReaderConfiguration configuration) {
                // no-op
            }
        };
        List<IOException> caught = new LinkedList<>();
        new OutputCache<>(throwingReader, 100L, caught::add);
        Awaitility.await().atMost(90, TimeUnit.MILLISECONDS)
            .pollInterval(5, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> Assert.assertEquals(caught, Collections.singletonList(expected)));
    }

    @Test
    public void cancel_ShouldStopPolling() throws InterruptedException, IOException {
        OutputCache<String> cache = new OutputCache<>(readerSpy, 100L);
        Awaitility.await()
            .atMost(50, TimeUnit.MILLISECONDS)
            .pollInterval(5, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                Assert.assertThat(cache.getRecords(), Matchers.hasSize(1));
            });
        cache.cancel();
        Mockito.verify(readerSpy).getRecords();
        Thread.sleep(300);
        Mockito.verifyNoMoreInteractions(readerSpy);
    }

    private static class TestReader implements Reader<String> {
        @Override
        public List<String> getRecords() {
            return Collections.singletonList(UUID.randomUUID().toString());
        }

        @Override
        public void setConfiguration(ReaderConfiguration configuration) {
            // no-op
        }
    }
}
