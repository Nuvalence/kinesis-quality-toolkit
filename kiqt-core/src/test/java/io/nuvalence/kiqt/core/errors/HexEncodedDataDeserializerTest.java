package io.nuvalence.kiqt.core.errors;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Charsets;
import org.apache.commons.codec.DecoderException;
import org.hamcrest.Matchers;

public class HexEncodedDataDeserializerTest {
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void deserialize_GivenHexEncodedString_ShouldDecodeString() throws IOException {
        String serialized = "{\"value\" : \"68656C6C6F20776F726C6421\"}";
        EncodedStringTestObject actual = mapper.readValue(serialized, EncodedStringTestObject.class);
        Assert.assertEquals("hello world!", actual.value);
    }

    @Test
    public void deserialize_GivenValueThatIsNotHexEncoded_ShouldThrowDecoderException() {
        String serialized = "{\"value\" : \"g\"}";
        try {
            mapper.readValue(serialized, EncodedStringTestObject.class);
            Assert.fail("should not deserialize illegal hex string");
        } catch (IOException expectedIOException) {
            // expected
            Assert.assertThat(expectedIOException.getCause(), Matchers.instanceOf(DecoderException.class));
        }
    }

    @Test
    public void deserialize_GivenDecoderWithCustomDeserializerAndValidBody_ShouldDeserialize() throws IOException {
        String serialized = "{\"value\" : \"3132\"}";
        EncodedIntegerTestObject actual = mapper.readValue(serialized, EncodedIntegerTestObject.class);
        Assert.assertEquals(12, actual.value.intValue());
    }

    private static class EncodedStringTestObject {
        @JsonDeserialize(using = HexEncodedStringDeserializer.class)
        private String value;

        private static class HexEncodedStringDeserializer extends HexEncodedDataDeserializer<String> {
            protected HexEncodedStringDeserializer() {
                super(String.class, (om, bytes) -> new String(bytes, Charsets.UTF_8));
            }
        }
    }

    private static class EncodedIntegerTestObject {
        @JsonDeserialize(using = HexEncodedIntegerDeserializer.class)
        private Integer value;

        private static class HexEncodedIntegerDeserializer extends HexEncodedDataDeserializer<Integer> {
            protected HexEncodedIntegerDeserializer() {
                super(Integer.class);
            }
        }
    }
}
