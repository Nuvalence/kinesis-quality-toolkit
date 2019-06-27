package io.nuvalence.kiqt.core.kda;

import software.amazon.awssdk.services.kinesisanalytics.KinesisAnalyticsClient;
import software.amazon.awssdk.services.kinesisanalytics.model.ApplicationDetail;
import software.amazon.awssdk.services.kinesisanalytics.model.ApplicationStatus;
import software.amazon.awssdk.services.kinesisanalytics.model.InputConfiguration;
import software.amazon.awssdk.services.kinesisanalytics.model.InputStartingPosition;
import software.amazon.awssdk.services.kinesisanalytics.model.InputStartingPositionConfiguration;
import software.amazon.awssdk.services.kinesisanalytics.model.StartApplicationRequest;
import software.amazon.awssdk.services.kinesisanalytics.model.StartApplicationResponse;
import software.amazon.awssdk.services.kinesisanalytics.model.StopApplicationRequest;
import software.amazon.awssdk.services.kinesisanalytics.model.StopApplicationResponse;

import java.util.function.Supplier;

/**
 * Lifecycle management for a Kinesis Analytics Application.
 */
public class ApplicationLifecycleManager {

    private KinesisAnalyticsClient client;
    private String applicationName;
    private Supplier<ApplicationDetail> detailSupplier;
    private long statusPollingInterval;

    /**
     * Creates an application lifecycle manager for the specified application.
     *
     * @param applicationName name of the analytics application
     */
    public ApplicationLifecycleManager(String applicationName) {
        this(KinesisAnalyticsClient.create(), applicationName, 10000);
    }

    /**
     * Creates an application lifecycle manager for the specified application
     * using a non default client.
     *
     * @param client                  client used to manage the application
     * @param applicationName         application name
     * @param statusPollingIntervalMs period, in milliseconds, between polling for status changes
     */
    public ApplicationLifecycleManager(KinesisAnalyticsClient client,
                                       String applicationName,
                                       long statusPollingIntervalMs) {
        this.client = client;
        this.applicationName = applicationName;
        this.detailSupplier = new ApplicationDetailSupplier(client, applicationName);
        this.statusPollingInterval = statusPollingIntervalMs;
    }

    /**
     * Ensures the application is running. If the application is currently
     * stopped, will start the application and wait for it to be running.
     *
     * @throws IllegalStateException if the application is not one of
     *                               {@link ApplicationStatus#READY}, {@link ApplicationStatus#STARTING}, or
     *                               {@link ApplicationStatus#RUNNING}
     * @throws InterruptedException  if the thread is interrupted while awaiting state change
     */
    public void ensureRunning() throws InterruptedException {
        for (int i = 0; i < 30; i++) {
            ApplicationDetail detail = detailSupplier.get();
            ApplicationStatus currentStatus = detail.applicationStatus();
            switch (currentStatus) {
                case READY:
                    start(detail);
                    break;
                case STARTING:
                case UPDATING:
                    break;
                case RUNNING:
                    return;
                default:
                    throw new IllegalStateException("application in unexpected state: " + currentStatus);
            }
            Thread.sleep(statusPollingInterval);
        }
        throw new IllegalStateException("timed out waiting for application to start");
    }

    /**
     * Ensures the application is stopped. If the application is currently
     * running, will stop the application and wait for it to be stopped.
     *
     * @throws IllegalStateException if the application is not one of
     *                               {@link ApplicationStatus#RUNNING}, {@link ApplicationStatus#STOPPING}, or
     *                               {@link ApplicationStatus#READY}
     * @throws InterruptedException  if the thread is interrupted while awaiting state change
     */
    public void ensureStopped() throws InterruptedException {
        for (int i = 0; i < 30; i++) {
            ApplicationStatus currentStatus = detailSupplier.get().applicationStatus();
            switch (currentStatus) {
                case RUNNING:
                    stop();
                    break;
                case STOPPING:
                    break;
                case READY:
                    return;
                default:
                    throw new IllegalStateException("application in unexpected state: " + currentStatus);
            }
            Thread.sleep(statusPollingInterval);
        }
        throw new IllegalStateException("timed out waiting for application to stop");
    }

    private StartApplicationResponse start(ApplicationDetail detail) {
        InputStartingPositionConfiguration startingPositionConfiguration = InputStartingPositionConfiguration.builder()
            .inputStartingPosition(InputStartingPosition.LAST_STOPPED_POINT).build();

        InputConfiguration[] inputConfigurations = detail.inputDescriptions().stream()
            .map(it -> InputConfiguration.builder()
                .id(it.inputId())
                .inputStartingPositionConfiguration(startingPositionConfiguration)
                .build()
            ).toArray(InputConfiguration[]::new);

        StartApplicationRequest request = StartApplicationRequest.builder()
            .applicationName(applicationName)
            .inputConfigurations(inputConfigurations)
            .build();
        return client.startApplication(request);
    }

    private StopApplicationResponse stop() {
        StopApplicationRequest request = StopApplicationRequest.builder()
            .applicationName(applicationName).build();
        return client.stopApplication(request);
    }
}
