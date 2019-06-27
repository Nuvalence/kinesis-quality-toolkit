package io.nuvalence.kiqt.core.outputs;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

/**
 * Caches all outputs from a desired reader so that {@link #getRecords()} returns all records since inception.
 *
 * @param <T> record type
 */
public class OutputCache<T> implements Reader<T> {
    private final List<T> records = new LinkedList<>();
    private Reader<T> reader;
    private Timer timer = new Timer();

    /**
     * Creates an output cache for the specified reader.
     *
     * @param reader          output reader
     * @param refreshInterval period of time, in milliseconds between requesting
     *                        the reader's {@link Reader#getRecords()}
     */
    public OutputCache(Reader<T> reader, Long refreshInterval) {
        this(reader, refreshInterval, (ioe) -> {
            throw new RuntimeException("failed to fetch records", ioe);
        });
    }

    /**
     * Creates a polling reader with the specified refresh interval.
     *
     * @param reader           output reader
     * @param refreshInterval  period of time, in milliseconds between requesting
     *                         the reader's {@link Reader#getRecords()}
     * @param exceptionHandler handles {@link IOException}s from the
     *                         reader's {@link Reader#getRecords()}
     */
    public OutputCache(Reader<T> reader, Long refreshInterval, Consumer<IOException> exceptionHandler) {
        this.reader = reader;
        try {
            // want to refresh once synchronously, then poll asynchronously
            refresh();
        } catch (IOException e) {
            exceptionHandler.accept(e);
        }
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    refresh();
                } catch (IOException e) {
                    exceptionHandler.accept(e);
                }
            }
        }, refreshInterval, refreshInterval);
    }

    /**
     * Gets the list of all cached records from the nested reader's {@link Reader#getRecords()}.
     *
     * @return all cached records
     */
    @Override
    public List<T> getRecords() {
        return new LinkedList<>(records);
    }

    @Override
    public void setConfiguration(ReaderConfiguration configuration) {
        reader.setConfiguration(configuration);
    }

    /**
     * Fetches latest records via {@link Reader#getRecords()} and updates the cached records.
     *
     * @throws IOException on error fetching records
     */
    public void refresh() throws IOException {
        synchronized (records) {
            this.records.addAll(reader.getRecords());
        }
    }

    /**
     * Cancels the scheduled polling.
     */
    public void cancel() {
        this.timer.cancel();
    }
}
