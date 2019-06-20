package io.nuvalence.kiqt.core.inputs;

import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;

import java.io.IOException;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import org.hamcrest.Matchers;

public class AbstractPutRecordsRequestTranslatorTest {
    private UnimplementedTranslator translator = new UnimplementedTranslator();

    @Test
    public void toEntries_GivenNullItems_ShouldReturnEmptyList() throws IOException {
        Assert.assertThat(translator.toEntries(null), Matchers.empty());
    }

    @Test
    public void toEntries_GivenEmptyList_ShouldReturnEmptyList() throws IOException {
        Assert.assertThat(translator.toEntries(Collections.emptyList()), Matchers.empty());
    }

    private static class UnimplementedTranslator extends AbstractPutRecordsRequestTranslator<String> {

        @Override
        PutRecordsRequestEntry toEntry(String item) throws IOException {
            throw new RuntimeException("this translator is meant to test translating a null or empty input list");
        }
    }
}
