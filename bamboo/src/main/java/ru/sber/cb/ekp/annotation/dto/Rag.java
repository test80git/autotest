package ru.sber.cb.ekp.annotation.dto;

import io.cucumber.core.internal.com.fasterxml.jackson.annotation.JsonSubTypes;
import io.cucumber.core.internal.com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DtoRag.class, name = "dto"),
        @JsonSubTypes.Type(value = EntityRag.class, name = "entity"),
        @JsonSubTypes.Type(value = ServiceRag.class, name = "service"),})
@Data
public abstract class Rag {
    private String description;
}

