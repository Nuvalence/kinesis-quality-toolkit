package io.nuvalence.kiqt.core.kda;

import io.nuvalence.kiqt.core.resources.AwsResource;

import software.amazon.awssdk.services.kinesisanalytics.KinesisAnalyticsClient;
import software.amazon.awssdk.services.kinesisanalytics.model.ApplicationDetail;
import software.amazon.awssdk.services.kinesisanalytics.model.DescribeApplicationRequest;
import software.amazon.awssdk.services.kinesisanalytics.model.DescribeApplicationResponse;
import software.amazon.awssdk.services.kinesisanalytics.model.InputDescription;
import software.amazon.awssdk.services.kinesisanalytics.model.KinesisFirehoseOutputDescription;
import software.amazon.awssdk.services.kinesisanalytics.model.KinesisStreamsInputDescription;
import software.amazon.awssdk.services.kinesisanalytics.model.KinesisStreamsOutputDescription;
import software.amazon.awssdk.services.kinesisanalytics.model.LambdaOutputDescription;
import software.amazon.awssdk.services.kinesisanalytics.model.OutputDescription;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import org.mockito.Mockito;

public class ApplicationIOProviderTest {
    private static final String streamArnPrefix = "arn:aws:kinesis:us-east-1:123456789012:stream/";
    private KinesisAnalyticsClient client = Mockito.mock(KinesisAnalyticsClient.class);
    private String applicationName = UUID.randomUUID().toString();
    private ApplicationIOProvider wrapper =
        new ApplicationIOProvider(client, applicationName);

    @Test
    public void getInput_GivenApplicationDetailSpecifyingExactlyOneInput_ShouldReturnThatInput() {
        AwsResource expected = new AwsResource(streamArnPrefix + "example-stream-name");
        configureInputs(inputStreamDescription(expected));
        Assert.assertEquals(expected, wrapper.getInput());
    }

    @Test
    public void getInput_OnSecondInvocation_ShouldNotRequestApplicationDetail() {
        AwsResource expected = new AwsResource(streamArnPrefix + "example-stream-name");
        configureInputs(inputStreamDescription(expected));

        // first invocation
        wrapper.getInput();
        Mockito.verify(client)
            .describeApplication(DescribeApplicationRequest.builder().applicationName(applicationName).build());

        // second invocation
        wrapper.getInput();
        Mockito.verifyNoMoreInteractions(client);
    }

    @Test(expected = IllegalStateException.class)
    public void getInput_GivenInputThatDoesNotHaveKinesisStreamDescription_ShouldThrow() {
        configureInputs(InputDescription.builder().namePrefix("foo").build());
        wrapper.getInput();
    }

    @Test
    public void getInput_GivenApplicationDetailSpecifyingMultipleInputs_ShouldReturnFirstInput() {
        // currently only one input is supported, so to avoid specifying the
        // name of an input we're just returning the first
        AwsResource expected = new AwsResource(streamArnPrefix + "example-stream-name");
        configureInputs(inputStreamDescription(expected), InputDescription.builder().namePrefix("b").build());
        Assert.assertEquals(expected, wrapper.getInput());
    }

    @Test
    public void getOutput_GivenApplicationDetailSpecifyingMultipleOutputStreams_ShouldReturnOutputByName() {
        AwsResource expected = new AwsResource(streamArnPrefix + "foo");

        configureOutputs(
            outputStreamDescription(expected),
            outputStreamDescription(new AwsResource(streamArnPrefix + "bar"))
        );

        Assert.assertEquals(expected, wrapper.getOutput("foo"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getOutput_GivenNameOfNonexistentOutput_ShouldThrow() {
        configureOutputs(OutputDescription.builder().name("asdf").build());
        wrapper.getOutput("doesnotexist");
    }

    @Test
    public void getOutput_OnSecondInvocation_ShouldNotRequestApplicationDetail() {
        configureOutputs(
            outputStreamDescription(new AwsResource(streamArnPrefix + "foo")),
            outputStreamDescription(new AwsResource(streamArnPrefix + "bar"))
        );

        // first invocation
        wrapper.getOutput("foo");
        Mockito.verify(client)
            .describeApplication(DescribeApplicationRequest.builder().applicationName(applicationName).build());

        // second invocation -- even for a different named output
        wrapper.getOutput("bar");
        Mockito.verifyNoMoreInteractions(client);
    }

    @Test
    public void getOutput_GivenFirehoseDestination_ShouldReturnFirehoseArn() {
        AwsResource firehose = new AwsResource(
            "arn:aws:firehose:us-east-1:123456789012:deliverystream/my-deliverystream"
        );
        KinesisFirehoseOutputDescription firehoseOutputDescription = KinesisFirehoseOutputDescription.builder()
            .resourceARN(firehose.getArn()).build();
        OutputDescription d = OutputDescription.builder().name(firehose.getResource())
            .kinesisFirehoseOutputDescription(firehoseOutputDescription).build();
        configureOutputs(d);

        Assert.assertEquals(firehose, wrapper.getOutput(firehose.getResource()));
    }

    @Test
    public void getOutput_GivenLambdaDestination_ShouldReturnLambdaArn() {
        AwsResource lambda = new AwsResource(
            "arn:aws:lambda:us-east-1:123456789012:function:ProcessKinesisRecords"
        );
        LambdaOutputDescription lambdaOutputDescription = LambdaOutputDescription.builder()
            .resourceARN(lambda.getArn()).build();
        OutputDescription d = OutputDescription.builder().name(lambda.getResource())
            .lambdaOutputDescription(lambdaOutputDescription).build();
        configureOutputs(d);

        Assert.assertEquals(lambda, wrapper.getOutput(lambda.getResource()));
    }

    @Test(expected = IllegalStateException.class)
    public void getOutput_GivenUnknownDestination_ShouldThrow() {
        String name = "INTENTIONALLY_UNRESOLVABLE_OUTPUT_DESTNATION";
        configureOutputs(OutputDescription.builder().name(name).build());

        wrapper.getOutput(name);
    }

    private InputDescription inputStreamDescription(AwsResource stream) {
        KinesisStreamsInputDescription d = KinesisStreamsInputDescription.builder()
            .resourceARN(stream.getArn()).build();
        return InputDescription.builder().namePrefix(stream.getResource())
            .kinesisStreamsInputDescription(d).build();
    }

    private OutputDescription outputStreamDescription(AwsResource stream) {
        KinesisStreamsOutputDescription d = KinesisStreamsOutputDescription.builder()
            .resourceARN(stream.getArn()).build();
        return OutputDescription.builder().name(stream.getResource())
            .kinesisStreamsOutputDescription(d).build();
    }

    private void configureInputs(InputDescription... inputDescriptions) {
        configureApplicationDetail(ApplicationDetail.builder().inputDescriptions(inputDescriptions).build());
    }

    private void configureOutputs(OutputDescription... outputDescriptions) {
        configureApplicationDetail(ApplicationDetail.builder().outputDescriptions(outputDescriptions).build());
    }

    private void configureApplicationDetail(ApplicationDetail detail) {
        DescribeApplicationRequest request = DescribeApplicationRequest.builder()
            .applicationName(applicationName).build();
        Mockito.when(client.describeApplication(request))
            .thenReturn(DescribeApplicationResponse.builder().applicationDetail(detail).build());
    }
}
