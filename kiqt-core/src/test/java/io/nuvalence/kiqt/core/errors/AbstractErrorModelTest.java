package io.nuvalence.kiqt.core.errors;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.hamcrest.Matchers;

public class AbstractErrorModelTest {
    private ObjectMapper om = new ObjectMapper();

    @Test
    public void deserialize_GivenError_ShouldBeADefaultModel() throws IOException {
        String error = "{\"ERROR_TIME\":\"2019-06-27 17:34:48.365\","
            + "\"ERROR_LEVEL\":\"WARNING\","
            + "\"ERROR_NAME\":\"Coercion error\","
            + "\"MESSAGE\":\"For input string: \\\"foo\\\"\","
            + "\"DATA_ROWTIME\":\"2019-06-27 17:34:48.365\","
            + "\"DATA_ROW\":\"696E76616C696420696E707574\","
            + "\"PUMP_NAME\":\"null\"}";

        Assert.assertThat(om.readValue(error, AbstractErrorModel.class), Matchers.instanceOf(DefaultErrorModel.class));
    }

    @Test
    public void equals_GivenEqualObjects_ShouldReturnTrue() {
        DefaultErrorModel dm = randomError();
        DefaultErrorModel clone = clone(dm);

        Assert.assertEquals(dm, clone);
    }


    @Test
    public void equals_GivenDifferentProperty_ShouldReturnFalse() {
        DefaultErrorModel dm = randomError();

        List<Consumer<DefaultErrorModel>> permutations = ImmutableList.of(
            e -> e.setErrorTime(new Date(1)),
            e -> e.setPumpName(UUID.randomUUID().toString()),
            e -> e.setLevel("ERROR"),
            e -> e.setName(UUID.randomUUID().toString()),
            e -> e.setMessage(UUID.randomUUID().toString()),
            e -> e.setRowTime(new Date(1)),
            e -> e.setSerializedRow(UUID.randomUUID().toString())
        );

        List<DefaultErrorModel> differingErrors = permutations.stream().map(c -> {
            DefaultErrorModel different = clone(dm);
            c.accept(different);
            return different;
        }).collect(Collectors.toList());

        Assert.assertThat(differingErrors, Matchers.everyItem(Matchers.not(Matchers.equalTo(dm))));

    }

    @Test
    public void hashCode_GivenEqualObjects_ShouldReturnSameValue() {
        DefaultErrorModel dm = randomError();
        DefaultErrorModel clone = clone(dm);

        Assert.assertEquals(dm.hashCode(), clone.hashCode());
    }

    private DefaultErrorModel randomError() {
        DefaultErrorModel dm = new DefaultErrorModel();
        dm.setPumpName(UUID.randomUUID().toString());
        dm.setSerializedRow(UUID.randomUUID().toString());
        dm.setRowTime(new Date(Instant.now().toEpochMilli()));
        dm.setMessage(UUID.randomUUID().toString());
        dm.setName(UUID.randomUUID().toString());
        dm.setLevel("WARNING");
        dm.setErrorTime(new Date(Instant.now().toEpochMilli()));
        return dm;
    }

    private DefaultErrorModel clone(DefaultErrorModel m) {
        DefaultErrorModel clone = new DefaultErrorModel();
        clone.setErrorTime(m.getErrorTime());
        clone.setLevel(m.getLevel());
        clone.setName(m.getName());
        clone.setMessage(m.getMessage());
        clone.setRowTime(m.getRowTime());
        clone.setSerializedRow(m.getDataRow());
        clone.setPumpName(m.getPumpName());
        return clone;
    }
}
