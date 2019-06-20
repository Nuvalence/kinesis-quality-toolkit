package io.nuvalence.kiqt.samples;

import io.nuvalence.kiqt.core.kda.ApplicationDetailSupplier;
import io.nuvalence.kiqt.core.kda.ApplicationLifecycleManager;

import software.amazon.awssdk.services.kinesisanalytics.KinesisAnalyticsClient;
import software.amazon.awssdk.services.kinesisanalytics.model.ApplicationStatus;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

public class ApplicationLifecycleManagementSampleTest {
    private String applicationName = SampleAppEnvironmentConfiguration.getSampleApplicationName();
    private ApplicationLifecycleManager manager = new ApplicationLifecycleManager(applicationName);

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Test
    public void it_ShouldManageApplicationState() throws InterruptedException {

        ApplicationDetailSupplier detailSupplier = new ApplicationDetailSupplier(
            KinesisAnalyticsClient.create(),
            applicationName
        );

        manager.ensureRunning();
        Assert.assertEquals(ApplicationStatus.RUNNING, detailSupplier.get().applicationStatus());

        manager.ensureStopped();
        Assert.assertEquals(ApplicationStatus.READY, detailSupplier.get().applicationStatus());

        manager.ensureRunning();
        Assert.assertEquals(ApplicationStatus.RUNNING, detailSupplier.get().applicationStatus());

    }
}
