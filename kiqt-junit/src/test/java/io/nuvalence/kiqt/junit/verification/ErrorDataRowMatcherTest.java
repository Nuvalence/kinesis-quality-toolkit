package io.nuvalence.kiqt.junit.verification;

import io.nuvalence.kiqt.core.errors.DefaultErrorModel;

import java.util.Date;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import org.hamcrest.Matchers;

public class ErrorDataRowMatcherTest {

    @Test
    public void it_GivenErrorWithMatchingDataRow_ShouldMatch() {
        String row = UUID.randomUUID().toString();

        DefaultErrorModel e = new DefaultErrorModel();
        e.setSerializedRow(row);
        e.setRowTime(new Date(1));

        Assert.assertThat(e, new ErrorDataRowMatcher(row));
    }

    @Test
    public void it_GivenErrorWithDifferentRow_ShouldNotMatch() {
        DefaultErrorModel e = new DefaultErrorModel();
        e.setSerializedRow(UUID.randomUUID().toString());

        Assert.assertThat(e, Matchers.not(new ErrorDataRowMatcher(UUID.randomUUID().toString())));
    }
}
