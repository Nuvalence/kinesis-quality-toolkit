package io.nuvalence.kiqt.samples.models;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A weather signal that does not match the input schema of the sample application.
 */
public class InvalidWeatherSignal {
    public String deviceId;
    // Specifying a string instead of an integer for utc time will result in a coercion error.
    @JsonProperty(value = "utcTime")
    public String notAValidTime;
    public Double value;
    public String postalCode;

    /**
     * Creates an invalid weather signal with random values.
     */
    public InvalidWeatherSignal() {
        this.deviceId = UUID.randomUUID().toString();
        this.notAValidTime = UUID.randomUUID().toString();
        this.value = 10.0;
        this.postalCode = "12180";
    }

    @Override
    public String toString() {
        return "InvalidWeatherSignal{" +
            "deviceId='" + deviceId + '\'' +
            ", notAValidTime='" + notAValidTime + '\'' +
            ", value=" + value +
            ", postalCode='" + postalCode + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InvalidWeatherSignal that = (InvalidWeatherSignal) o;
        return Objects.equals(deviceId, that.deviceId) &&
            Objects.equals(notAValidTime, that.notAValidTime) &&
            Objects.equals(value, that.value) &&
            Objects.equals(postalCode, that.postalCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, notAValidTime, value, postalCode);
    }
}
