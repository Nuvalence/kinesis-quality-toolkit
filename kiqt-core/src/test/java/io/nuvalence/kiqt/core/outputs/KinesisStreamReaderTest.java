package io.nuvalence.kiqt.core.outputs;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.DescribeStreamRequest;
import software.amazon.awssdk.services.kinesis.model.DescribeStreamResponse;
import software.amazon.awssdk.services.kinesis.model.GetRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.GetRecordsResponse;
import software.amazon.awssdk.services.kinesis.model.GetShardIteratorRequest;
import software.amazon.awssdk.services.kinesis.model.GetShardIteratorResponse;
import software.amazon.awssdk.services.kinesis.model.Record;
import software.amazon.awssdk.services.kinesis.model.Shard;
import software.amazon.awssdk.services.kinesis.model.ShardIteratorType;
import software.amazon.awssdk.services.kinesis.model.StreamDescription;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;

public class KinesisStreamReaderTest {
    private KinesisClient mockClient = Mockito.mock(KinesisClient.class);
    private String streamName = UUID.randomUUID().toString();
    private DescribeStreamRequest describeStreamRequest = DescribeStreamRequest.builder()
        .streamName(streamName).build();
    private JsonRecordTranslator<SampleRecord> translatorSpy = Mockito.spy(
        new JsonRecordTranslator<>(new ObjectMapper(), SampleRecord.class)
    );
    private KinesisStreamReader<SampleRecord> reader =
        new KinesisStreamReader<>(mockClient, streamName, translatorSpy);

    private static Record toKinesisRecord(SampleRecord r) {
        try {
            return Record.builder()
                .data(SdkBytes.fromByteArray(new ObjectMapper().writeValueAsBytes(r)))
                .build();
        } catch (JsonProcessingException e) {
            Assert.fail("could not transform: " + r);
            return null;
        }
    }

    @Test
    public void getRecords_GivenStreamWithSingleShard_ShouldRequestRecordsUsingShardIterator() throws IOException {
        Shard shard = Shard.builder().shardId(UUID.randomUUID().toString()).build();

        mockDescribeStream(shard);

        String shardIterator = mockGetShardIterator(shard.shardId());

        List<SampleRecord> records = Collections.singletonList(new SampleRecord());
        mockGetRecords(shardIterator, null, records);

        reader.getRecords();

        Mockito.verify(mockClient).describeStream(describeStreamRequest);
        verifyGetShardIterator(shard.shardId());
        verifyGetRecords(shardIterator);
    }

    @Test
    public void getRecords_GivenStreamWithSingleShardAndStartConfiguration_ShouldRequestRecordsUsingShardIterator()
        throws IOException {

        Instant startTime = Instant.now();

        ReaderConfiguration configuration = new ReaderConfiguration();
        configuration.setStartTime(startTime);
        reader.setConfiguration(configuration);

        Shard shard = Shard.builder().shardId(UUID.randomUUID().toString()).build();

        mockDescribeStream(shard);

        GetShardIteratorRequest shardIteratorRequest = GetShardIteratorRequest.builder()
            .shardIteratorType(ShardIteratorType.AT_TIMESTAMP)
            .timestamp(startTime)
            .shardId(shard.shardId())
            .streamName(streamName)
            .build();

        String shardIterator = UUID.randomUUID().toString();
        Mockito.when(mockClient.getShardIterator(shardIteratorRequest))
            .thenReturn(GetShardIteratorResponse.builder().shardIterator(shardIterator).build());

        List<SampleRecord> records = Collections.singletonList(new SampleRecord());
        mockGetRecords(shardIterator, null, records);

        reader.getRecords();

        Mockito.verify(mockClient).describeStream(describeStreamRequest);
        Mockito.verify(mockClient).getShardIterator(shardIteratorRequest);
        verifyGetRecords(shardIterator);
    }

    @Test
    public void getRecords_GivenSingleShardStream_OnSecondCall_ShouldNotRequestShardIterator() throws IOException {
        Shard shard = Shard.builder().shardId(UUID.randomUUID().toString()).build();

        mockDescribeStream(shard);

        String shardIterator = mockGetShardIterator(shard.shardId());

        List<SampleRecord> records = Collections.singletonList(new SampleRecord());
        String nextShardIterator = UUID.randomUUID().toString();
        mockGetRecords(shardIterator, nextShardIterator, records);
        mockGetRecords(nextShardIterator, null, records);

        reader.getRecords();
        reader.getRecords();

        Mockito.verify(mockClient, Mockito.times(2)).describeStream(describeStreamRequest);
        verifyGetShardIterator(shard.shardId());
        verifyGetRecords(shardIterator);
        verifyGetRecords(nextShardIterator);
    }

    @Test
    public void getRecords_GivenStreamWithSingleShard_ShouldReturnExpectedRecords() throws IOException {
        Shard shard = Shard.builder().shardId(UUID.randomUUID().toString()).build();

        mockDescribeStream(shard);
        String shardIterator = mockGetShardIterator(shard.shardId());
        List<SampleRecord> expected = Collections.singletonList(new SampleRecord());
        mockGetRecords(shardIterator, null, expected);

        List<SampleRecord> actual = reader.getRecords();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getRecords_GivenStreamWithMultipleShards_ShouldRequestRecordsForEachShard() throws IOException {
        Shard[] shards = new Shard[] {
            Shard.builder().shardId(UUID.randomUUID().toString()).build(),
            Shard.builder().shardId(UUID.randomUUID().toString()).build(),
            Shard.builder().shardId(UUID.randomUUID().toString()).build()
        };

        mockDescribeStream(shards);

        String[] iterators = new String[3];
        for (int i = 0; i < shards.length; i++) {
            iterators[i] = mockGetShardIterator(shards[i].shardId());
            List<SampleRecord> records = Collections.singletonList(new SampleRecord());
            mockGetRecords(iterators[i], null, records);
        }

        reader.getRecords();

        Mockito.verify(mockClient).describeStream(describeStreamRequest);

        for (int i = 0; i < shards.length; i++) {
            verifyGetShardIterator(shards[i].shardId());
            verifyGetRecords(iterators[i]);
        }
    }

    @Test
    public void getRecords_GivenStreamWithMultipleShards_ShouldReturnExpectedRecords() throws IOException {

        Shard[] shards = new Shard[] {
            Shard.builder().shardId(UUID.randomUUID().toString()).build(),
            Shard.builder().shardId(UUID.randomUUID().toString()).build(),
            Shard.builder().shardId(UUID.randomUUID().toString()).build()
        };

        mockDescribeStream(shards);

        List<SampleRecord> expected = new LinkedList<>();
        for (Shard shard : shards) {
            String iterator = mockGetShardIterator(shard.shardId());
            List<SampleRecord> records = Collections.singletonList(new SampleRecord());
            expected.addAll(records);
            mockGetRecords(iterator, null, records);
        }

        Assert.assertEquals(expected, reader.getRecords());
    }

    private void mockDescribeStream(Shard... shards) {
        DescribeStreamResponse response = DescribeStreamResponse.builder()
            .streamDescription(StreamDescription.builder().shards(shards).build())
            .build();
        Mockito.when(mockClient.describeStream(describeStreamRequest)).thenReturn(response);
    }

    private String mockGetShardIterator(String shardId) {
        String shardIterator = UUID.randomUUID().toString();
        GetShardIteratorRequest request = GetShardIteratorRequest.builder()
            .shardIteratorType(ShardIteratorType.TRIM_HORIZON)
            .shardId(shardId)
            .streamName(streamName)
            .build();
        Mockito.when(mockClient.getShardIterator(request))
            .thenReturn(GetShardIteratorResponse.builder().shardIterator(shardIterator).build());
        return shardIterator;
    }

    private void verifyGetShardIterator(String shardId) {
        GetShardIteratorRequest request = GetShardIteratorRequest.builder()
            .shardIteratorType(ShardIteratorType.TRIM_HORIZON)
            .shardId(shardId)
            .streamName(streamName)
            .build();
        Mockito.verify(mockClient).getShardIterator(request);
    }

    private void mockGetRecords(String shardIterator, String nextShardIterator, List<SampleRecord> records) {
        GetRecordsRequest getRecordsRequest = GetRecordsRequest.builder().shardIterator(shardIterator).build();
        GetRecordsResponse getRecordsResponse = GetRecordsResponse.builder()
            .nextShardIterator(nextShardIterator)
            .records(records.stream().map(KinesisStreamReaderTest::toKinesisRecord).collect(Collectors.toList()))
            .build();

        Mockito.when(mockClient.getRecords(getRecordsRequest))
            .thenReturn(getRecordsResponse);
    }

    private void verifyGetRecords(String shardIterator) {
        Mockito.verify(mockClient).getRecords(GetRecordsRequest.builder().shardIterator(shardIterator).build());
    }

    private static class SampleRecord {
        public String id = UUID.randomUUID().toString();

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof SampleRecord)) {
                return false;
            }

            return Objects.equals(this.id, ((SampleRecord) o).id);
        }
    }
}
