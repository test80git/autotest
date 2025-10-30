package ru.sber.cb.ekp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE_PARAMETER)
public @interface StepEntity {
    String packagePath();

    String entityManager();

    String transManager();
}