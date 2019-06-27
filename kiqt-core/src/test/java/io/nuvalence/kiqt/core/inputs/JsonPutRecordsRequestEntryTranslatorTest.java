package io.nuvalence.kiqt.core.inputs;

import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;

import java.io.IOException;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;

public class JsonPutRecordsRequestEntryTranslatorTest {
    private ObjectMapper objectMapperSpy = Mockito.spy(new ObjectMapper());

    @Test
    public void toEntry_GivenSampleRecord_ShouldDelegateSerializationToObjectMapper() throws IOException {
        JsonPutRecordsRequestEntryTranslator<SampleRecord> translator =
            new JsonPutRecordsRequestEntryTranslator<>(objectMapperSpy, o -> "key");

        SampleRecord item = new SampleRecord();
        PutRecordsRequestEntry entry = translator.toEntry(item);

        Mockito.verify(objectMapperSpy).writeValueAsBytes(item);
        Assert.assertEquals("{\"id\":\"" + item.id + "\"}", entry.data().asUtf8String());
    }

    @Test
    public void toEntry_GivenPartitionKeyProvider_ShouldUseSpecifiedPartitionKey() throws IOException {
        String partitionKey = UUID.randomUUID().toString();

        JsonPutRecordsRequestEntryTranslator<SampleRecord> translator =
            new JsonPutRecordsRequestEntryTranslator<>(objectMapperSpy, o -> partitionKey);

        PutRecordsRequestEntry entry = translator.toEntry(new SampleRecord());

        Assert.assertEquals(partitionKey, entry.partitionKey());
    }

    private static class SampleRecord {
        public String id = UUID.randomUUID().toString();
    }
}
