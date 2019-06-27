# KiQT Samples

This project contains a selection of sample tests which serve as functional tests for the framework itself in addition to providing examples of common KiQT use cases. 

- [BasicSampleTest](src/integrationTest/java/io/nuvalence/kiqt/samples/BasicSampleTest.java)
shows how to write a simple test generating inputs and asserting on the output of an application. 
- [ApplicationLifecycleManagementSampleTest](src/integrationTest/java/io/nuvalence/kiqt/samples/ApplicationLifecycleManagementSampleTest.java) 
shows how to manage the application lifecycle using KiQT. 
- [ErrorHandlingSampleTest](src/integrationTest/java/io/nuvalence/kiqt/samples/ErrorHandlingSampleTest.java) shows setting up a mixin for custom error deserialization
- [ExtendedQualityToolSampleTest](src/integrationTest/java/io/nuvalence/kiqt/samples/ExtendedQualityToolSampleTest.java) shows a simplified form of the BasicSampleTest but using a customized KinesisQualityToolkit