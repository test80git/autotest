package ru.sber.cb.ekp.annotation.processor;

import lombok.Getter;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

@Getter
public class EntityPathNode  extends PathNode{
    private final VariableElement from;
    public EntityPathNode(VariableElement from, VariableElement to, TypeMirror toType, String path) {
        super(to, toType, path);
        this.from = from;
    }

    @Override
    public String toString() {
        return "EntityPathNode{" +
                "from=" + from +
                ", to=" + to +
                ", toType=" + toType +
                ", path='" + path + '\'' +
                '}';
    }

}
