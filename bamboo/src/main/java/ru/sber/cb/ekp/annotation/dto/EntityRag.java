package ru.sber.cb.ekp.annotation.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EntityRag extends TreeRag {
    private String repos;
}