package io.nuvalence.kiqt.core.outputs;

import io.nuvalence.kiqt.core.resources.AwsResource;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provides a reader given a resource definition. This will act as the
 * extensibility point for configuring any custom destinations.
 */
public interface ReaderProvider {
    /**
     * Configures a reader for the described output.
     *
     * @param resource   the AWS resource to be read
     * @param mapper     object mapper used to deserialize records
     * @param <TOutput>  record type
     * @param recordType output record class
     * @return output reader
     */
    <TOutput> Reader<TOutput> get(AwsResource resource, ObjectMapper mapper, Class<TOutput> recordType);
}
