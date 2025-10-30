package ru.sber.cb.ekp.annotation.dto;

import lombok.Data;import lombok.EqualsAndHashCode;

@Data@EqualsAndHashCode(callSuper = true)public abstract class TreeRag extends Rag {    private String tree;}