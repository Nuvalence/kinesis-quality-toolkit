package io.nuvalence.kiqt.core.resources;

import org.junit.Assert;
import org.junit.Test;

public class AwsResourceTest {
    // test cases for kinesis resources and services that could be the destination for a firehose
    private String firehoseArn = "arn:aws:firehose:us-east-1:123456789012:deliverystream/my-deliverystream";
    private String streamArn = "arn:aws:kinesis:us-east-1:123456789012:stream/example-stream-name";
    private String s3Arn = "arn:aws:s3:::my_corporate_bucket";
    private String elasticsearchArn = "arn:aws:es:us-east-1:123456789012:domain/streaming-logs";
    private String redshiftArn = "arn:aws:redshift:us-east-1:123456789012:cluster:my-cluster";
    private String lambdaArn = "arn:aws:lambda:us-east-1:123456789012:function:ProcessKinesisRecords";

    @Test(expected = IllegalArgumentException.class)
    public void ctor_GivenNullArn_ShouldThrowIllegalArgumentException() {
        new AwsResource(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ctor_GivenEmptyArn_ShouldThrowIllegalArgumentException() {
        new AwsResource("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ctor_GivenInvalidArn_ShouldThrowIllegalArgumentException() {
        new AwsResource("this:is:not:an:arn");
    }

    @Test
    public void getService_GivenFirehoseArn_ShouldReturnFirehose() {
        Assert.assertEquals("firehose", new AwsResource(firehoseArn).getService());
    }

    @Test
    public void getResource_GivenFirehoseArn_ShouldReturnDeliveryStreamName() {
        Assert.assertEquals("my-deliverystream", new AwsResource(firehoseArn).getResource());
    }

    @Test
    public void getRegion_GivenFirehoseArn_ShouldReturnUsEast1Region() {
        Assert.assertEquals("us-east-1", new AwsResource(firehoseArn).getRegion());
    }

    @Test
    public void getService_GivenStreamArn_ShouldReturnKinesis() {
        Assert.assertEquals("kinesis", new AwsResource(streamArn).getService());
    }

    @Test
    public void getResource_GivenStreamArn_ShouldReturnStreamName() {
        Assert.assertEquals("example-stream-name", new AwsResource(streamArn).getResource());
    }

    @Test
    public void getService_GivenS3Arn_ShouldReturnS3() {
        Assert.assertEquals("s3", new AwsResource(s3Arn).getService());
    }

    @Test
    public void getResource_GivenS3Arn_ShouldReturnBucketName() {
        Assert.assertEquals("my_corporate_bucket", new AwsResource(s3Arn).getResource());
    }

    @Test
    public void getRegion_GivenS3Arn_ShouldReturnNullRegion() {
        Assert.assertNull(new AwsResource(s3Arn).getRegion());
    }

    @Test
    public void getService_GivenElasticsearchArn_ShouldReturnEs() {
        Assert.assertEquals("es", new AwsResource(elasticsearchArn).getService());
    }

    @Test
    public void getResource_GivenElasticsearchArn_ShouldReturnClusterName() {
        Assert.assertEquals("streaming-logs", new AwsResource(elasticsearchArn).getResource());
    }

    @Test
    public void getService_GivenRedshiftArn_ShouldReturnRedshift() {
        Assert.assertEquals("redshift", new AwsResource(redshiftArn).getService());
    }

    @Test
    public void getResource_GivenRedshiftArn_ShouldReturnClusterName() {
        Assert.assertEquals("my-cluster", new AwsResource(redshiftArn).getResource());
    }

    @Test
    public void getService_GivenLambdaArn_ShouldReturnLambda() {
        Assert.assertEquals("lambda", new AwsResource(lambdaArn).getService());
    }

    @Test
    public void getResource_GivenLambdaArn_ShouldReturnFunctionName() {
        Assert.assertEquals("ProcessKinesisRecords", new AwsResource(lambdaArn).getResource());
    }
}
