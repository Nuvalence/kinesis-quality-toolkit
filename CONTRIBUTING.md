# Contributing

## Prerequisites
 - Build: Java 8
 - Integration Test: [AWS credentials and configuration](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-configure.html) provided via either `aws configure` or environment variables.

## Checkstyle Configuration
The checkstyle configuration file can be found at [config/checkstyle/checkstyle.xml](config/checkstyle/checkstyle.xml).

## Build
To run the full build of all projects (including linting, unit tests, and coverage):
```
./gradlew build
```

## Integration Tests
To execute the integration tests in the samples project:
```
./gradlew integrationTest
```

While iterating on changes to the application a common workflow is to:

Create the sample application and run integration tests, but don't delete the sample immediately after tests:
```
./gradlew integrationTest -x deleteSampleApplication
```

To run tests from your IDE, set the Java system property `io.nuvalence.sample-application-name`
to the name of your deployed application. When running the tests from Gradle, the build system sets this property.

To rerun integration tests while the sample application is still deployed:
```
./gradlew integrationTest -x createSampleApplication -x deleteSampleApplication
```

To delete the sample application
```
./gradlew deleteSampleApplication -x createSampleApplication
```

Note that the application finishes deleting asynchronously, if you immediately try to recreate the application
you may see an error indicating the application still exists. 