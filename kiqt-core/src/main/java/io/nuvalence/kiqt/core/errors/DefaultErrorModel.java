package io.nuvalence.kiqt.core.errors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Charsets;

/**
 * Default error model representing a data row as a string.
 */
public class DefaultErrorModel extends AbstractErrorModel<String> {
    @JsonProperty("DATA_ROW")
    @JsonDeserialize(using = DataRowDeserializer.class)
    private String serializedRow;

    @Override
    public String getDataRow() {
        return serializedRow;
    }

    /**
     * Sets the data row.
     *
     * @param serializedRow serialized data row object
     */
    public void setSerializedRow(String serializedRow) {
        this.serializedRow = serializedRow;
    }

    private static class DataRowDeserializer extends HexEncodedDataDeserializer<String> {
        protected DataRowDeserializer() {
            super(String.class, (om, bytes) -> new String(bytes, Charsets.UTF_8));
        }
    }
}
