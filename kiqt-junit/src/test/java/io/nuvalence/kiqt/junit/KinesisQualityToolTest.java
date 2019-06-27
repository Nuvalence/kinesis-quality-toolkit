package io.nuvalence.kiqt.junit;

import io.nuvalence.kiqt.core.errors.AbstractErrorModel;
import io.nuvalence.kiqt.core.inputs.Writer;
import io.nuvalence.kiqt.core.inputs.WriterProvider;
import io.nuvalence.kiqt.core.kda.ApplicationIOProvider;
import io.nuvalence.kiqt.core.outputs.Reader;
import io.nuvalence.kiqt.core.outputs.ReaderProvider;
import io.nuvalence.kiqt.core.resources.AwsResource;

import java.util.UUID;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.mockito.Mockito;

@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
public class KinesisQualityToolTest {
    private ApplicationIOProvider mockDetails = Mockito.mock(ApplicationIOProvider.class);
    private ObjectMapper mockObjectMapper = Mockito.mock(ObjectMapper.class);
    private KinesisQualityTool scenario = new KinesisQualityTool(mockDetails);

    public KinesisQualityToolTest() {
        scenario.setObjectMapper(mockObjectMapper);
    }

    @Test
    public void getObjectMapper_ShouldReturnMutableObjectMapper() {
        scenario.getObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        Mockito.verify(mockObjectMapper).enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Test
    public void theInput_GivenWriterProvider_ShouldRequestInputFromAppDetailsAndInvokeProvider() {
        FakeWriterProvider providerSpy = Mockito.spy(new FakeWriterProvider());
        AwsResource mockInput = Mockito.mock(AwsResource.class);

        Mockito.when(mockDetails.getInput()).thenReturn(mockInput);

        scenario.theInput(providerSpy);

        Mockito.verify(mockDetails).getInput();
        Mockito.verify(providerSpy).get(mockInput, mockObjectMapper);
    }

    @Test
    public void theOutput_GivenReaderProvider_ShouldRequestOutputFromAppDetailsAndInvokeProvider() {
        FakeReaderProvider providerSpy = Mockito.spy(new FakeReaderProvider());
        AwsResource mockOutput = Mockito.mock(AwsResource.class);
        String outputName = UUID.randomUUID().toString();

        Mockito.when(mockDetails.getOutput(outputName)).thenReturn(mockOutput);

        scenario.setReaderProvider(providerSpy);
        scenario.theOutput(outputName, String.class);

        Mockito.verify(mockDetails).getOutput(outputName);
        Mockito.verify(providerSpy).get(mockOutput, mockObjectMapper, String.class);
    }

    @Test
    public void theErrorOutput_GivenReaderProvider_ShouldRequestErrorOutputFromAppDetailsAndInvokeProvider() {
        FakeReaderProvider providerSpy = Mockito.spy(new FakeReaderProvider());
        AwsResource mockOutput = Mockito.mock(AwsResource.class);
        String outputName = "error_stream";

        Mockito.when(mockDetails.getOutput(outputName)).thenReturn(mockOutput);

        scenario.setReaderProvider(providerSpy);
        scenario.theErrorOutput();

        Mockito.verify(mockDetails).getOutput(outputName);
        Mockito.verify(providerSpy).get(mockOutput, mockObjectMapper, AbstractErrorModel.class);
    }

    private static class FakeWriterProvider implements WriterProvider<String, String> {
        @Override
        public Writer<String, String> get(AwsResource resource, ObjectMapper mapper) {
            return null;
        }
    }

    private static class FakeReaderProvider implements ReaderProvider {
        @Override
        public <TOutput> Reader<TOutput> get(AwsResource resource, ObjectMapper mapper, Class<TOutput> recordType) {
            return null;
        }
    }

}
