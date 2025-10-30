package ru.sber.cb.ekp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({
        ElementType.TYPE,
        ElementType.FIELD,
        ElementType.METHOD,
        ElementType.PACKAGE})
public @interface Description {    String[] value() default {};
}
