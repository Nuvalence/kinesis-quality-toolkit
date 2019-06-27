package io.nuvalence.kiqt.core.errors;

import java.util.Date;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Abstract base representation of an error as produced by Kinesis Data Analytics (KDA).
 * The model contains all fields indicated here as well as a hex encoded, serialized
 * <code>DATA_ROW</code>. Subclasses can implement a custom {@link HexEncodedDataDeserializer}
 * to deserialize the data row as the expected data type. A {@link DefaultErrorModel} can
 * be used to get serialized data row object as a string.
 *
 * @param <TDataRow> type of object represented by the data row
 * @see <a href="https://docs.aws.amazon.com/kinesisanalytics/latest/dev/error-handling.html">Kinesis Analytics Error Handling</a>
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "PUMP_NAME",
    defaultImpl = DefaultErrorModel.class
)
public abstract class AbstractErrorModel<TDataRow> {
    private static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss.SSS";

    @JsonProperty("ERROR_TIME")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = AbstractErrorModel.DATE_FORMAT)
    private Date errorTime;

    @JsonProperty("ERROR_LEVEL")
    private String level;

    @JsonProperty("ERROR_NAME")
    private String name;

    @JsonProperty("MESSAGE")
    private String message;

    @JsonProperty("DATA_ROWTIME")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = AbstractErrorModel.DATE_FORMAT)
    private Date rowTime;

    @JsonProperty("PUMP_NAME")
    private String pumpName;

    /**
     * Gets the decoded, deserialized data row.
     *
     * @return data row
     */
    public abstract TDataRow getDataRow();

    /**
     * Gets the time when the error occurred within the KDA.
     *
     * @return error time
     */
    public Date getErrorTime() {
        return new Date(errorTime.getTime());
    }

    /**
     * Sets the error time.
     *
     * @param errorTime error time
     */
    public void setErrorTime(Date errorTime) {
        this.errorTime = new Date(errorTime.getTime());
    }

    /**
     * Gets the severity level of the error.
     *
     * @return error level
     */
    public String getLevel() {
        return level;
    }

    /**
     * Sets the error severity level.
     *
     * @param level error level
     */
    public void setLevel(String level) {
        this.level = level;
    }

    /**
     * Gets the error name. This is a short human readable name indicating the type of error.
     *
     * @return error name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the error name.
     *
     * @param name error name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the error message.
     *
     * @return error message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the error message.
     *
     * @param message error message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the row time of the incoming record.
     *
     * @return row time
     */
    public Date getRowTime() {
        return new Date(rowTime.getTime());
    }

    /**
     * Sets the row time.
     *
     * @param rowTime row time
     */
    public void setRowTime(Date rowTime) {
        this.rowTime = new Date(rowTime.getTime());
    }

    /**
     * Gets the name of the pump which produced the error.
     * <p>
     * <i><strong>Note:</strong> if it is null, the value will be serialized
     * as <code>"null"</code>. An undefined pump name indicates data from the
     * input stream was invalid, and the data row will contain the
     * input record data.</i>
     *
     * @return optional pump name
     */
    public String getPumpName() {
        return pumpName;
    }

    /**
     * Sets the pump name.
     *
     * @param pumpName pump name
     */
    public void setPumpName(String pumpName) {
        this.pumpName = pumpName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractErrorModel)) {
            return false;
        }
        AbstractErrorModel that = (AbstractErrorModel) o;
        return Objects.equals(errorTime, that.errorTime) &&
            Objects.equals(level, that.level) &&
            Objects.equals(name, that.name) &&
            Objects.equals(message, that.message) &&
            Objects.equals(rowTime, that.rowTime) &&
            Objects.equals(getDataRow(), that.getDataRow()) &&
            Objects.equals(pumpName, that.pumpName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(errorTime, level, name, message, rowTime, getDataRow(), pumpName);
    }

    @Override
    public String toString() {
        return "DefaultErrorModel{" +
            "errorTime=" + errorTime.getTime() +
            ", level='" + level + '\'' +
            ", name='" + name + '\'' +
            ", message='" + message + '\'' +
            ", rowTime=" + rowTime.getTime() +
            ", dataRow='" + getDataRow() + '\'' +
            ", pumpName='" + pumpName + '\'' +
            '}';
    }
}
