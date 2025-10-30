package ru.sber.cb.ekp.annotation.processor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

@Getter
@AllArgsConstructor
@ToString
public class PathNode {
    protected final VariableElement to;
    protected final TypeMirror toType;
    protected final String path;
}