package io.nuvalence.kiqt.samples;

import io.nuvalence.kiqt.core.errors.AbstractErrorModel;
import io.nuvalence.kiqt.core.errors.DefaultErrorModel;
import io.nuvalence.kiqt.core.kda.ApplicationIOProvider;
import io.nuvalence.kiqt.core.kda.ApplicationLifecycleManager;
import io.nuvalence.kiqt.junit.KinesisQualityTool;
import io.nuvalence.kiqt.junit.verification.ErrorDataRowMatcher;
import io.nuvalence.kiqt.samples.models.InvalidInputErrorModel;
import io.nuvalence.kiqt.samples.models.InvalidWeatherSignal;
import io.nuvalence.kiqt.samples.models.PolymorphicErrorModelMixIn;
import io.nuvalence.kiqt.samples.models.WeatherSignal;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.core.CombinableMatcher;

public class ErrorHandlingSampleTest {
    private KinesisQualityTool kiqt;

    @Before
    public void setup() throws InterruptedException {
        // configure KiQT
        String applicationName = SampleAppEnvironmentConfiguration.getSampleApplicationName();
        ApplicationIOProvider ioProvider = new ApplicationIOProvider(applicationName);
        kiqt = new KinesisQualityTool(ioProvider);

        // add a mixin for polymorphic deserialization of the errors
        kiqt.getObjectMapper().addMixIn(AbstractErrorModel.class, PolymorphicErrorModelMixIn.class);

        // ensure the application is running
        ApplicationLifecycleManager lifecycleManager = new ApplicationLifecycleManager(applicationName);
        lifecycleManager.ensureRunning();
    }

    @Test
    public void givenInputThatDoesNotMatchSchema_ShouldGenerateIncludeInvalidInputInDataRow() throws IOException {
        InvalidWeatherSignal invalidInput = new InvalidWeatherSignal();

        kiqt.<InvalidWeatherSignal>theInputStream()
            .given(Collections.singletonList(invalidInput));

        kiqt.theErrorOutput().within(30, TimeUnit.SECONDS)
            .should("expected invalid input error", Matchers.contains(new ErrorDataRowMatcher(invalidInput)));
    }

    @Test
    public void givenInputWithNullFields_ShouldGenerateValueArrayError() throws IOException {
        // this case will generate an error because we have columns in the KDA configured as non null
        WeatherSignal input = new WeatherSignal();

        kiqt.<WeatherSignal>theInputStream().given(Collections.singletonList(input));

        Matcher<AbstractErrorModel> expectedError = Matchers.instanceOf(DefaultErrorModel.class);

        kiqt.theErrorOutput().within(30, TimeUnit.SECONDS)
            .should("expected an error for null input", Matchers.contains(expectedError));
    }
}
