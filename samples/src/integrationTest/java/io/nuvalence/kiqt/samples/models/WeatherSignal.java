package io.nuvalence.kiqt.samples.models;

import java.util.Objects;

/**
 * Input record model for the sample application.
 */
public class WeatherSignal {
    private String deviceId;

    private Long utcTime;

    private Double value;

    private String postalCode;

    /**
     * Gets the unique identifier for the reporting device.
     *
     * @return device id
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Sets the unique identifier for the reporting device.
     *
     * @param deviceId device id
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Gets the time of the temperature reading.
     *
     * @return epoch time
     */
    public Long getUtcTime() {
        return utcTime;
    }

    /**
     * Sets the time of the temperature reading.
     *
     * @param utcTime epoch time
     */
    public void setUtcTime(Long utcTime) {
        this.utcTime = utcTime;
    }

    /**
     * Gets the value of the temperature reading.
     *
     * @return temperature
     */
    public Double getValue() {
        return value;
    }

    /**
     * Sets the value of the temperature reading.
     *
     * @param value temperature
     */
    public void setValue(Double value) {
        this.value = value;
    }

    /**
     * Gets the postal code indicating where the device is located.
     *
     * @return postal code
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     * Sets the postal code indicating where the device is located.
     *
     * @param postalCode postal code
     */
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    @Override
    public String toString() {
        return "WeatherSignal{" +
            "deviceId='" + deviceId + '\'' +
            ", utcTime=" + utcTime +
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
        WeatherSignal that = (WeatherSignal) o;
        return deviceId.equals(that.deviceId) &&
            utcTime.equals(that.utcTime) &&
            value.equals(that.value) &&
            postalCode.equals(that.postalCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, utcTime, value, postalCode);
    }
}
