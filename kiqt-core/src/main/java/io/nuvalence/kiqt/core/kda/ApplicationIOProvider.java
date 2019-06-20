package io.nuvalence.kiqt.core.kda;

import io.nuvalence.kiqt.core.resources.AwsResource;

import software.amazon.awssdk.services.kinesisanalytics.KinesisAnalyticsClient;
import software.amazon.awssdk.services.kinesisanalytics.model.ApplicationDetail;
import software.amazon.awssdk.services.kinesisanalytics.model.InputDescription;
import software.amazon.awssdk.services.kinesisanalytics.model.OutputDescription;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.base.Suppliers;

/**
 * Provides input and output resources for a Kinesis Analytics Application.
 */
public class ApplicationIOProvider {
    private Supplier<ApplicationDetail> applicationDetailSupplier;

    /**
     * Creates an analytics application wrapper.
     *
     * @param applicationName name of the analytics application
     */
    public ApplicationIOProvider(String applicationName) {
        this(KinesisAnalyticsClient.create(), applicationName);
    }

    /**
     * Creates an analytics application wrapper.
     *
     * @param client          client used to interact with with the application
     * @param applicationName name of the analytics application
     */
    public ApplicationIOProvider(KinesisAnalyticsClient client, String applicationName) {
        this.applicationDetailSupplier = Suppliers.memoize(new ApplicationDetailSupplier(client, applicationName));
    }

    /**
     * Gets description for the first application input.
     *
     * @return input description
     */
    public AwsResource getInput() {
        InputDescription desc = applicationDetailSupplier.get().inputDescriptions().get(0);
        String arn;
        if (desc.kinesisStreamsInputDescription() != null) {
            arn = desc.kinesisStreamsInputDescription().resourceARN();
        } else {
            throw new IllegalStateException("Unknown type for input named: " + desc.namePrefix());
        }
        return new AwsResource(arn);
    }

    /**
     * Gets the output by name.
     *
     * @param name name of desired output.
     * @return output description
     */
    public AwsResource getOutput(String name) {
        Optional<OutputDescription> optionalOutputDescription = applicationDetailSupplier.get()
            .outputDescriptions().stream()
            .filter(d -> d.name().equalsIgnoreCase(name))
            .findFirst();
        if (!optionalOutputDescription.isPresent()) {
            String outputs = applicationDetailSupplier.get().outputDescriptions().stream()
                .map(OutputDescription::name)
                .collect(Collectors.joining(","));
            throw new IllegalArgumentException("no output by name: " + name + "; outputs: " + outputs);
        }

        OutputDescription desc = optionalOutputDescription.get();
        String arn;
        if (desc.kinesisStreamsOutputDescription() != null) {
            arn = desc.kinesisStreamsOutputDescription().resourceARN();
        } else if (desc.kinesisFirehoseOutputDescription() != null) {
            arn = desc.kinesisFirehoseOutputDescription().resourceARN();
        } else if (desc.lambdaOutputDescription() != null) {
            arn = desc.lambdaOutputDescription().resourceARN();
        } else {
            throw new IllegalStateException("Unknown destination for output named " + name);
        }

        return new AwsResource(arn);
    }
}
