package io.nuvalence.kiqt.samples.models;

import io.nuvalence.kiqt.core.errors.AbstractErrorModel;
import io.nuvalence.kiqt.core.errors.DefaultErrorModel;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Mixin for deserializing errors to polymorphic types depending on pump name.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "PUMP_NAME",
    defaultImpl = DefaultErrorModel.class
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = InvalidInputErrorModel.class, name = "null")
})
public abstract class PolymorphicErrorModelMixIn extends AbstractErrorModel<Object> {
}
