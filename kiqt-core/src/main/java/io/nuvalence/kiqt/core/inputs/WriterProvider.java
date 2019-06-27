package io.nuvalence.kiqt.core.inputs;

import io.nuvalence.kiqt.core.resources.AwsResource;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provides a writer for a generic input.
 *
 * @param <TResponse>    given record response type
 * @param <TInputRecord> input record type
 */
public interface WriterProvider<TInputRecord, TResponse> {
    /**
     * Gets a writer for input.
     *
     * @param resource aws resource
     * @param mapper   default object mapper used for serializing objects
     * @return writer
     */
    Writer<TInputRecord, TResponse> get(AwsResource resource, ObjectMapper mapper);
}
