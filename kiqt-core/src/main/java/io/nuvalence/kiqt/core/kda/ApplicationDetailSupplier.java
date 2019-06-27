package io.nuvalence.kiqt.core.kda;

import software.amazon.awssdk.services.kinesisanalytics.KinesisAnalyticsClient;
import software.amazon.awssdk.services.kinesisanalytics.model.ApplicationDetail;
import software.amazon.awssdk.services.kinesisanalytics.model.DescribeApplicationRequest;

import com.google.common.base.Supplier;

/**
 * Provides {@link ApplicationDetail} for a Kinesis Analytics Application.
 */
public class ApplicationDetailSupplier implements Supplier<ApplicationDetail> {

    private KinesisAnalyticsClient client;
    private DescribeApplicationRequest describeApplicationRequest;

    /**
     * Creates a detail supplier.
     *
     * @param client          client used to get details
     * @param applicationName name of application
     */
    public ApplicationDetailSupplier(KinesisAnalyticsClient client, String applicationName) {
        this.client = client;
        this.describeApplicationRequest = DescribeApplicationRequest.builder()
            .applicationName(applicationName).build();
    }

    @Override
    public ApplicationDetail get() {
        return client.describeApplication(describeApplicationRequest).applicationDetail();
    }
}
