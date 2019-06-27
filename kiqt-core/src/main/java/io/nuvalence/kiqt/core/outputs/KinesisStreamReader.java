package io.nuvalence.kiqt.core.outputs;

import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.DescribeStreamRequest;
import software.amazon.awssdk.services.kinesis.model.GetRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.GetRecordsResponse;
import software.amazon.awssdk.services.kinesis.model.GetShardIteratorRequest;
import software.amazon.awssdk.services.kinesis.model.Record;
import software.amazon.awssdk.services.kinesis.model.Shard;
import software.amazon.awssdk.services.kinesis.model.ShardIteratorType;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Polling reader for a Kinesis stream. Polls and caches output given at a fixed interval.
 *
 * @param <T> record type
 */
public class KinesisStreamReader<T> implements Reader<T> {
    private final String streamName;
    private final AbstractKinesisRecordTranslator<T> recordTranslator;
    private ReaderConfiguration configuration;
    private KinesisClient client;
    private Map<String, String> shardIdNextIteratorMap = new HashMap<>();

    /**
     * Creates a reader.
     *
     * @param streamName       name of stream
     * @param recordTranslator function to translate a kinesis record to the expected record type
     */
    public KinesisStreamReader(String streamName, AbstractKinesisRecordTranslator<T> recordTranslator) {
        this(KinesisClient.create(), streamName, recordTranslator);
    }

    /**
     * Creates a reader.
     *
     * @param client           client used to interact with the stream
     * @param streamName       name of stream
     * @param recordTranslator function to translate a kinesis record to the expected record type
     */
    public KinesisStreamReader(KinesisClient client,
                               String streamName,
                               AbstractKinesisRecordTranslator<T> recordTranslator) {
        this.configuration = new ReaderConfiguration();
        this.client = client;
        this.streamName = streamName;
        this.recordTranslator = recordTranslator;
    }

    @Override
    public List<T> getRecords() throws IOException {
        List<Shard> shards = this.client
            .describeStream(DescribeStreamRequest.builder().streamName(streamName).build())
            .streamDescription().shards();
        List<T> items = new LinkedList<>();
        for (Shard shard : shards) {
            items.addAll(recordTranslator.toValues(getRecordsForShard(shard)));
        }
        return items;
    }

    @Override
    public void setConfiguration(ReaderConfiguration configuration) {
        this.configuration = configuration;
    }

    private List<Record> getRecordsForShard(Shard shard) {
        GetRecordsResponse response = this.client.getRecords(GetRecordsRequest.builder()
            .shardIterator(getShardIterator(shard))
            .build());

        shardIdNextIteratorMap.put(shard.shardId(), response.nextShardIterator());

        return response.records();
    }

    private String getShardIterator(Shard shard) {
        if (shardIdNextIteratorMap.containsKey(shard.shardId())) {
            return shardIdNextIteratorMap.get(shard.shardId());
        } else {
            GetShardIteratorRequest.Builder requestBuilder = GetShardIteratorRequest.builder()
                .streamName(streamName)
                .shardId(shard.shardId());

            if (configuration != null && configuration.getStartTime() != null) {
                requestBuilder.shardIteratorType(ShardIteratorType.AT_TIMESTAMP)
                    .timestamp(configuration.getStartTime());
            } else {
                requestBuilder.shardIteratorType(ShardIteratorType.TRIM_HORIZON);
            }

            return this.client.getShardIterator(requestBuilder.build()).shardIterator();
        }
    }
}
