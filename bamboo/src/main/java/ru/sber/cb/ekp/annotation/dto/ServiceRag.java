package ru.sber.cb.ekp.annotation.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceRag extends Rag {
    private String rq;
    private String rs;
}