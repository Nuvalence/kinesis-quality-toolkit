package io.nuvalence.kiqt.core.errors;

import java.io.IOException;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DefaultErrorModelTest {
    private static ObjectMapper mapper = new ObjectMapper();

    @Test
    public void deserialize_GivenSerializedErrorModel_ShouldReturnErrorModel() throws IOException {
        String input = "{\"ERROR_TIME\":\"2019-06-18 19:23:59.393\","
            + "\"ERROR_LEVEL\":\"WARNING\","
            + "\"ERROR_NAME\":\"Coercion error\","
            + "\"MESSAGE\":\"For input string: \\\"a\\\"\","
            + "\"DATA_ROWTIME\":\"2019-06-18 19:23:59.393\","
            + "\"DATA_ROW\":"
            + "\"7B225554435F54494D45223A313536303838353833363932332C22494E545F56414C5545223A2261227D\","
            + "\"PUMP_NAME\":\"null\"}";
        DefaultErrorModel actual = mapper.readValue(input, DefaultErrorModel.class);
        DefaultErrorModel expected = new DefaultErrorModel();
        expected.setLevel("WARNING");
        expected.setName("Coercion error");
        expected.setMessage("For input string: \"a\"");
        expected.setPumpName(null);
        expected.setErrorTime(new Date(1560885839393L));
        expected.setRowTime(new Date(1560885839393L));
        expected.setSerializedRow("{\"UTC_TIME\":1560885836923,\"INT_VALUE\":\"a\"}");
        Assert.assertEquals(expected, actual);
    }
}
