# Kinesis Quality Toolkit (KiQT)
Quality assurance toolkit for testing [Kinesis Data Analytics](https://aws.amazon.com/kinesis/data-analytics/) applications. 

## Overview
Amazon Kinesis is a powerful, real-time streaming and analytics platform where a single streaming pipeline is composed
of multiple AWS components representing the application as well as its inputs and outputs. The  goal of this project is
to provide a single cohesive interface for:
 - Inspecting a running application to dynamically create input writers and output readers
 - Defining deterministic and repeatable functional test cases by:
    - serializing and writing data to the application input
    - reading from the application output(s), deserializing contents, and performing assertions on the output
    - managing application lifecycle


## Project Structure
This repository is currently divided in to 2 main projects:
 - **kiqt-core** is the set of resources needed to interact with Kinesis Analytics and Kinesis Streams without taking
 on any dependencies specific to testing
 - **kiqt-junit** provides a behavior driven development style API for writing JUnit functional tests for an application

## Example
Given a list of `inputRecords` and an expected list of `outputRecords`, an example test may look something like this:
```
// creates the test harness
KinesisQualityTool kiqt = new KinesisQualityTool(new ApplicationIOProvider("MyApplication"));
// gets the input kinesis stream
kiqt.theInputStream()
        // specifies a response handler
        .withResponseHandler(response -> Assert.assertEquals(0, response.failedRecordCount().longValue()))
        // writes the inputRecords to the input stream
        .given(inputRecords);
// gets the output stream named OUTPUT_STREAM_NAME and deserializes its records to type OutputRecord
kiqt.theOutput("OUTPUT_STREAM_NAME", OutputRecord.class)
        // sets up a timeout of 30 seconds for all assertions on this output
        .within(30, TimeUnit.SECONDS)
        // executes the assertion
        .should(Matchers.equalTo(expectedOutput));
```

For more sample code and use cases, [see the samples](samples/).

## Roadmap

### Publishing
We're working on setting up processes for publishing our artifacts to Maven Central.

### Supported Destinations
Currently only Kinesis Streams are supported as inputs and outputs, with the other integrations to come.
Following the project structure decribed above, our highest priorities are creating projects:
 - `kiqt-firehose` for parsing resource information from a Kinesis Firehose destination
 - `kiqt-s3` for reading data that has been written (via a firehose) to an S3 bucket

### Kinesis Best Practices Assertions
We'd like to add some  assertions specific to ensuring applications under test follow the guidelines
set in the [Best Practices](https://docs.aws.amazon.com/kinesisanalytics/latest/dev/best-practices.html) 
documentation for SQL applications. 

### Supported Test Frameworks
This isn't a high priority for us currently, we may be open to accepting PRs specialized to frameworks other than JUnit if there is demand.

## Contributing
See the [contributing guidelines](CONTRIBUTING.md) for more information.