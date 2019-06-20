package io.nuvalence.kiqt.samples.models;

import io.nuvalence.kiqt.core.errors.AbstractErrorModel;
import io.nuvalence.kiqt.core.errors.HexEncodedDataDeserializer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Error model representing error due to invalid input.
 */
public class InvalidInputErrorModel extends AbstractErrorModel<InvalidWeatherSignal> {
    @JsonProperty("DATA_ROW")
    @JsonDeserialize(using = DataRowDeserializer.class)
    private InvalidWeatherSignal row;

    @Override
    public InvalidWeatherSignal getDataRow() {
        return row;
    }

    private static class DataRowDeserializer extends HexEncodedDataDeserializer<InvalidWeatherSignal> {
        protected DataRowDeserializer() {
            super(InvalidWeatherSignal.class);
        }
    }
}
