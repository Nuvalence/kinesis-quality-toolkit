# KiQT Core
Contains all logic for composing the pipeline IO and application management. 

This project does not have any test specific dependencies and could be consumed in any application to interact with a Kinesis pipeline.
The `ApplicationIOProvider` parses resource definitions for the inputs and outputs of a deployed Kinesis Analytics Application. 

```
// The IO provider responsible for getting input and output resources via application details, 
ApplicationIOProvider application = new ApplicationIOProvider("MyKinesisApp");
```

A consumer of the streaming application would implement an output reader as follows:
```
// A reader provider for providing a reader implementation for a given resource, 
ReaderProvider readerProvider = new DefaultReaderProvider();

/// And an output reader can be retrieved using:
Output<OutputRecord> output = readerProvider.get(application.getOutput("DESTINATION_STREAM"), new ObjectMapper(), OutputRecord.class)
```

And a producer to the streaming application would implement a writer as follows:
```
// And finally a writer provider for providing a writer implementation for a given resource,
WriterProvider writerProvider = (resource, mapper) -> new StreamWriter<>(resource.getResource(), mapper)

// Once these resources are created, an input writer can be retrieved using:
Writer<InputRecord, PutRecordsResponse> writer = writerProvider.get(application.getInput(), new ObjectMapper());
```
