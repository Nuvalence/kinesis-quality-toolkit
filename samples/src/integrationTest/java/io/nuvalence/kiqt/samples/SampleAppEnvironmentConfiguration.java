package io.nuvalence.kiqt.samples;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Stores the configuration for the sample app as passed in via system properties.
 * Provides a centralized location for retrieving the information as this project
 * contains many different examples on how you may write tests using KiQT and
 * in every case you need to provide the name of the application under test.
 */
class SampleAppEnvironmentConfiguration {
    /**
     * Gets the name of the deployed sample application, as provided via system property.
     * @return sample application name
     */
    static String getSampleApplicationName() {
        String propertyName = "io.nuvalence.sample-application-name";
        String name = System.getProperty(propertyName);
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("must specify application using the '"
                + propertyName + "' system property");
        }
        return name;
    }
}
