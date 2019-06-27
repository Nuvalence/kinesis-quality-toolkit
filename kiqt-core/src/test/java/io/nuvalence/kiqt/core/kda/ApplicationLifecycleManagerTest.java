package io.nuvalence.kiqt.core.kda;

import software.amazon.awssdk.services.kinesisanalytics.KinesisAnalyticsClient;
import software.amazon.awssdk.services.kinesisanalytics.model.ApplicationDetail;
import software.amazon.awssdk.services.kinesisanalytics.model.ApplicationStatus;
import software.amazon.awssdk.services.kinesisanalytics.model.DescribeApplicationRequest;
import software.amazon.awssdk.services.kinesisanalytics.model.DescribeApplicationResponse;
import software.amazon.awssdk.services.kinesisanalytics.model.InputConfiguration;
import software.amazon.awssdk.services.kinesisanalytics.model.InputDescription;
import software.amazon.awssdk.services.kinesisanalytics.model.InputStartingPosition;
import software.amazon.awssdk.services.kinesisanalytics.model.StartApplicationRequest;
import software.amazon.awssdk.services.kinesisanalytics.model.StopApplicationRequest;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class ApplicationLifecycleManagerTest {
    private long statusPollingInterval = 10;
    private String applicationName = UUID.randomUUID().toString();
    private DescribeApplicationRequest describeApplicationRequest = DescribeApplicationRequest.builder()
        .applicationName(applicationName).build();
    private KinesisAnalyticsClient client = Mockito.mock(KinesisAnalyticsClient.class);
    private ApplicationLifecycleManager manager;

    @Before
    public void setup() {
        this.manager = new ApplicationLifecycleManager(client, applicationName, statusPollingInterval);
    }

    @Test
    public void ensureRunning_GivenRunningApplication_ShouldRequestStatusExactlyOnce() throws InterruptedException {
        Mockito.when(client.describeApplication(describeApplicationRequest))
            .thenReturn(withStatus(ApplicationStatus.RUNNING));
        manager.ensureRunning();
        Mockito.verify(client).describeApplication(describeApplicationRequest);
    }

    @Test
    public void ensureRunning_GivenStartingThenRunningApplication_ShouldRequestStatusTwice()
        throws InterruptedException {
        Mockito.when(client.describeApplication(describeApplicationRequest))
            .thenReturn(withStatus(ApplicationStatus.STARTING))
            .thenReturn(withStatus(ApplicationStatus.RUNNING));

        assertEnsureRunningRequestsDetailsTwice();
    }

    @Test
    public void ensureRunning_GivenUpdatingThenRunningApplication_ShouldRequestStatusTwice()
        throws InterruptedException {
        Mockito.when(client.describeApplication(describeApplicationRequest))
            .thenReturn(withStatus(ApplicationStatus.UPDATING))
            .thenReturn(withStatus(ApplicationStatus.RUNNING));

        assertEnsureRunningRequestsDetailsTwice();
    }

    @Test
    public void ensureRunning_GivenReadyThenRunningApplication_ShouldStartApplicationAndRequestStatusTwice()
        throws InterruptedException {
        Mockito.when(client.describeApplication(describeApplicationRequest))
            .thenReturn(withStatus(ApplicationStatus.READY))
            .thenReturn(withStatus(ApplicationStatus.RUNNING));

        assertEnsureRunningRequestsDetailsTwice();

        ArgumentCaptor<StartApplicationRequest> requestCaptor = ArgumentCaptor.forClass(StartApplicationRequest.class);
        Mockito.verify(client).startApplication(requestCaptor.capture());
        String actual = requestCaptor.getValue().applicationName();
        Assert.assertEquals("request to start application had incorrect application name", applicationName, actual);
    }

    @Test
    public void ensureRunning_GivenDeletingApplication_ShouldThrow() throws InterruptedException {
        Mockito.when(client.describeApplication(describeApplicationRequest))
            .thenReturn(withStatus(ApplicationStatus.DELETING));
        try {
            manager.ensureRunning();
            Assert.fail("expected exception starting an application that is being deleted");
        } catch (IllegalStateException expectedException) {
            // expected
        }
        Mockito.verify(client).describeApplication(describeApplicationRequest);
    }

    @Test
    public void ensureRunning_GivenTimeoutWaitingForRunning_ShouldThrow() throws InterruptedException {
        Mockito.when(client.describeApplication(describeApplicationRequest))
            .thenReturn(withStatus(ApplicationStatus.STARTING));
        try {
            manager.ensureRunning();
            Assert.fail("expected timeout waiting for application to finish starting");
        } catch (IllegalStateException expectedException) {
            // expected
        }
        Mockito.verify(client, Mockito.times(30))
            .describeApplication(describeApplicationRequest);
    }

    @Test
    public void ensureRunning_GivenReadyThenRunningApplication_ShouldStartApplicationFromLastStoppedPoint()
        throws InterruptedException {
        String inputId = UUID.randomUUID().toString();
        ApplicationDetail applicationDetail = ApplicationDetail.builder()
            .applicationStatus(ApplicationStatus.READY)
            .inputDescriptions(InputDescription.builder().inputId(inputId).build())
            .build();

        Mockito.when(client.describeApplication(describeApplicationRequest))
            .thenReturn(DescribeApplicationResponse.builder().applicationDetail(applicationDetail).build())
            .thenReturn(withStatus(ApplicationStatus.RUNNING));

        manager.ensureRunning();

        ArgumentCaptor<StartApplicationRequest> requestCaptor = ArgumentCaptor.forClass(StartApplicationRequest.class);
        Mockito.verify(client).startApplication(requestCaptor.capture());

        List<InputConfiguration> inputConfigurationList = requestCaptor.getValue().inputConfigurations();
        Assert.assertThat(
            "start request should have only one input configuration",
            inputConfigurationList,
            Matchers.hasSize(1)
        );

        InputConfiguration inputConfiguration = inputConfigurationList.get(0);

        Assert.assertEquals("input configuration id should match input description", inputId, inputConfiguration.id());

        InputStartingPosition actualStartingPosition = inputConfiguration
            .inputStartingPositionConfiguration().inputStartingPosition();
        Assert.assertEquals(
            "starting position should be last stopped point",
            InputStartingPosition.LAST_STOPPED_POINT,
            actualStartingPosition
        );
    }

    @Test
    public void ensureStopped_GivenReadyApplication_ShouldRequestStatusExactlyOnce() throws InterruptedException {
        Mockito.when(client.describeApplication(describeApplicationRequest))
            .thenReturn(withStatus(ApplicationStatus.READY));
        manager.ensureStopped();
        Mockito.verify(client).describeApplication(describeApplicationRequest);
    }

    @Test
    public void ensureStopped_GivenStoppingThenReadyApplication_ShouldRequestStatusTwice() throws InterruptedException {
        Mockito.when(client.describeApplication(describeApplicationRequest))
            .thenReturn(withStatus(ApplicationStatus.STOPPING))
            .thenReturn(withStatus(ApplicationStatus.READY));

        assertEnsureStoppedRequestsDetailsTwice();
    }

    @Test
    public void ensureStopped_GivenRunningThenReadyApplication_ShouldStartApplicationAndRequestStatusTwice()
        throws InterruptedException {
        Mockito.when(client.describeApplication(describeApplicationRequest))
            .thenReturn(withStatus(ApplicationStatus.RUNNING))
            .thenReturn(withStatus(ApplicationStatus.READY));

        assertEnsureStoppedRequestsDetailsTwice();

        ArgumentCaptor<StopApplicationRequest> requestCaptor = ArgumentCaptor.forClass(StopApplicationRequest.class);
        Mockito.verify(client).stopApplication(requestCaptor.capture());
        String actual = requestCaptor.getValue().applicationName();
        Assert.assertEquals("request to stop application had incorrect application name", applicationName, actual);
    }

    @Test
    public void ensureStopped_GivenDeletingApplication_ShouldThrow() throws InterruptedException {
        Mockito.when(client.describeApplication(describeApplicationRequest))
            .thenReturn(withStatus(ApplicationStatus.DELETING));
        try {
            manager.ensureStopped();
            Assert.fail("expected exception stopping an application that is being deleted");
        } catch (IllegalStateException expectedException) {
            // expected
        }
        Mockito.verify(client).describeApplication(describeApplicationRequest);
    }

    @Test
    public void ensureStopped_GivenTimeoutWaitingForStopped_ShouldThrow() throws InterruptedException {
        Mockito.when(client.describeApplication(describeApplicationRequest))
            .thenReturn(withStatus(ApplicationStatus.STOPPING));
        try {
            manager.ensureStopped();
            Assert.fail("expected timeout waiting for application to finish starting");
        } catch (IllegalStateException expectedException) {
            // expected
        }
        Mockito.verify(client, Mockito.times(30))
            .describeApplication(describeApplicationRequest);
    }

    private void assertEnsureRunningRequestsDetailsTwice() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        manager.ensureRunning();
        long duration = System.currentTimeMillis() - startTime;
        Mockito.verify(client, Mockito.times(2))
            .describeApplication(describeApplicationRequest);
        Assert.assertThat(
            "ensure application running should have taken longer than the polling interval",
            duration,
            Matchers.greaterThanOrEqualTo(statusPollingInterval)
        );
    }

    private void assertEnsureStoppedRequestsDetailsTwice() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        manager.ensureStopped();
        long duration = System.currentTimeMillis() - startTime;
        Mockito.verify(client, Mockito.times(2))
            .describeApplication(describeApplicationRequest);
        Assert.assertThat(
            "ensure application running should have taken longer than the polling interval",
            duration,
            Matchers.greaterThanOrEqualTo(statusPollingInterval)
        );
    }

    private DescribeApplicationResponse withStatus(ApplicationStatus status) {
        ApplicationDetail applicationDetail = ApplicationDetail.builder()
            .applicationStatus(status)
            .build();
        return DescribeApplicationResponse.builder()
            .applicationDetail(applicationDetail)
            .build();
    }
}
