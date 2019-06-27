package io.nuvalence.kiqt.core.errors;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 * Decode and deserialize hex encoded data.
 *
 * @param <T> data type
 */
public class HexEncodedDataDeserializer<T> extends StdDeserializer<T> {
    private DecodedDataDeserializer<T> decodedDataDeserializer;

    /**
     * Creates a deserializer that decodes text and then deserializes
     * it as the specified type.
     *
     * @param c data type
     */
    protected HexEncodedDataDeserializer(Class<T> c) {
        this(c, (om, bytes) -> om.readValue(bytes, c));
    }

    /**
     * Creates a deserializer that decodes text and then deserializes it using
     * a custom function.
     *
     * @param c                       data type
     * @param decodedDataDeserializer custom decoded data deserializer
     */
    protected HexEncodedDataDeserializer(Class<T> c, DecodedDataDeserializer<T> decodedDataDeserializer) {
        super(c);
        this.decodedDataDeserializer = decodedDataDeserializer;
    }

    /**
     * Creates a deserializers that decodes texts then deserializes it using a custom function.
     * @param valueType data type
     * @param decodedDataDeserializer custom decoded data deserializer
     */
    public HexEncodedDataDeserializer(JavaType valueType, DecodedDataDeserializer<T> decodedDataDeserializer) {
        super(valueType);
        this.decodedDataDeserializer = decodedDataDeserializer;
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        try {
            return decodedDataDeserializer.deserialize(mapper, Hex.decodeHex(p.getText().toCharArray()));
        } catch (DecoderException e) {
            throw new IOException("Could not deserialize hex encoded data row", e);
        }
    }

    /**
     * Interface for specifying custom decoded data deserialization.
     *
     * @param <T> data type
     */
    public interface DecodedDataDeserializer<T> {
        /**
         * Deserialize data.
         *
         * @param mapper object mapper
         * @param bytes  data represented as bytes
         * @return deserialized object
         * @throws IOException on error deserializing data
         */
        T deserialize(ObjectMapper mapper, byte[] bytes) throws IOException;
    }
}
