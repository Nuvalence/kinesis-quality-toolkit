package io.nuvalence.kiqt.core.outputs;

import software.amazon.awssdk.services.kinesis.model.Record;

import java.io.IOException;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import org.hamcrest.Matchers;

public class AbstractKinesisRecordTranslatorTest {
    private UnimplementedTranslator translator = new UnimplementedTranslator();

    @Test
    public void toValues_GivenNullItems_ShouldReturnEmptyList() throws IOException {
        Assert.assertThat(translator.toValues(null), Matchers.empty());
    }

    @Test
    public void toValues_GivenEmptyList_ShouldReturnEmptyList() throws IOException {
        Assert.assertThat(translator.toValues(Collections.emptyList()), Matchers.empty());
    }

    private static class UnimplementedTranslator extends AbstractKinesisRecordTranslator<String> {
        @Override
        String toValue(Record record) throws IOException {
            throw new RuntimeException("this translator is meant to test translating a null or empty input list");
        }
    }
}
