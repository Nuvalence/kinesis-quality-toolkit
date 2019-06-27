package io.nuvalence.kiqt.core.outputs;

import java.time.Instant;

/**
 * Configuration options for a {@link Reader}.
 */
public class ReaderConfiguration {
    private Instant startTime;

    /**
     * Configures a start time for the reader to read records that were
     * generated at or after that time. If not set, the reader should provide
     * a default configuration.
     *
     * @return start time
     */
    public Instant getStartTime() {
        return startTime;
    }

    /**
     * Gets the configured start time.
     *
     * @param startTime start time
     */
    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }
}
