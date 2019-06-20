package io.nuvalence.kiqt.samples.models;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.math.DoubleMath;

/**
 * Represents a computed temperature (average, min, and max from the sensors)
 * for a postal code, at a point in time.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComputedTemperature {
    private static final double PRECISION = 0.000001;

    @JsonProperty("UTC_TIME")
    private Long utcTime;
    @JsonProperty("POSTAL_CODE")
    private String postalCode;
    @JsonProperty("MINIMUM")
    private Double minimum;
    @JsonProperty("AVERAGE")
    private Double average;
    @JsonProperty("MAXIMUM")
    private Double maximum;

    /**
     * Get time of temperature reading.
     *
     * @return epoch time
     */
    public Long getUtcTime() {
        return utcTime;
    }

    /**
     * Sets the time of temperature reading.
     *
     * @param utcTime epoch time
     */
    public void setUtcTime(Long utcTime) {
        this.utcTime = utcTime;
    }

    /**
     * Gets the postal code.
     *
     * @return postal code
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     * Sets the postal code.
     *
     * @param postalCode postal code
     */
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    /**
     * Gets the minimum temperature.
     *
     * @return minimum temperature
     */
    public Double getMinimum() {
        return minimum;
    }

    /**
     * Sets the minimum temperature.
     *
     * @param minimum minimum temperature
     */
    public void setMinimum(Double minimum) {
        this.minimum = minimum;
    }

    /**
     * Gets the average temperature.
     *
     * @return average temperature
     */
    public Double getAverage() {
        return average;
    }

    /**
     * Sets the average temperature.
     *
     * @param average average temperature
     */
    public void setAverage(Double average) {
        this.average = average;
    }

    /**
     * Gets the maximum temperature.
     *
     * @return maximum temperature
     */
    public Double getMaximum() {
        return maximum;
    }

    /**
     * Sets the maximum temperature.
     *
     * @param maximum maximum temperature
     */
    public void setMaximum(Double maximum) {
        this.maximum = maximum;
    }

    @Override
    public String toString() {
        return "ComputedTemperature{" +
            "utcTime=" + utcTime +
            ", postalCode='" + postalCode + '\'' +
            ", minimum=" + minimum +
            ", average=" + average +
            ", maximum=" + maximum +
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
        ComputedTemperature that = (ComputedTemperature) o;
        return utcTime.equals(that.utcTime) &&
            postalCode.equals(that.postalCode) &&
            // only care that temperature values are precise to 6 decimal places
            DoubleMath.fuzzyEquals(minimum, that.minimum, PRECISION) &&
            DoubleMath.fuzzyEquals(average, that.average, PRECISION) &&
            DoubleMath.fuzzyEquals(maximum, that.maximum, PRECISION);
    }

    @Override
    public int hashCode() {
        return Objects.hash(utcTime, postalCode, minimum, average, maximum);
    }
}
