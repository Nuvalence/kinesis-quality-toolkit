package io.nuvalence.kiqt.core.inputs;


import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class StreamWriterTest {
    private KinesisClient client;
    private String streamName;

    @Before
    public void setup() {
        client = Mockito.mock(KinesisClient.class);
        streamName = UUID.randomUUID().toString();
    }

    @Test
    public void put_GivenRecords_ShouldTranslateEachToPutRecordsRequestEntry() throws IOException {
        RecordTranslator translatorSpy = Mockito.spy(new RecordTranslator());
        StreamWriter<SampleRecord> writer = new StreamWriter<>(client, streamName, translatorSpy);
        List<SampleRecord> records = ImmutableList.of(new SampleRecord());

        writer.put(records);

        Mockito.verify(translatorSpy).toEntries(records);
    }

    @Test
    public void put_GivenRecords_ShouldWriteToStream() throws IOException {
        StreamWriter<SampleRecord> writer = new StreamWriter<>(client, streamName, new RecordTranslator());

        writer.put(ImmutableList.of(new SampleRecord()));

        ArgumentCaptor<PutRecordsRequest> arg = ArgumentCaptor.forClass(PutRecordsRequest.class);
        Mockito.verify(client).putRecords(arg.capture());
        Assert.assertEquals(streamName, arg.getValue().streamName());
    }

    private static class SampleRecord { }

    private static class RecordTranslator extends AbstractPutRecordsRequestTranslator<SampleRecord> {
        @Override
        public PutRecordsRequestEntry toEntry(SampleRecord item) {
            return PutRecordsRequestEntry.builder().build();
        }
    }
}
