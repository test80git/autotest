package ru.sber.cb.ekp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Аннотация используется для пометки сущностей, используемых при генерации шагов.
 */
@Target(ElementType.TYPE)
public @interface Step {
    /**
     * Определяет, будут ли поля сущности включены в генерацию только явно.
     * Если значение равно {@code true}, поля включаются в генерацию только при наличии аннотации {@link Include}.
     * По умолчанию равняется {@code false}, что означает автоматическое включение всех полей.
     */
    boolean onlyExplicitlyIncluded() default false;

    /**
     * Определяет, является ли эта сущность корневой в генерации.
     * По умолчанию равен {@code true}.
     */
    boolean root() default true;

    /**
     * Аннотация для исключения поля из генерации, если {@code onlyExplicitlyIncluded == false}.
     */
    @Target(ElementType.FIELD)
    @interface Exclude {
    }

    /**
     * Аннотация для включения поля в генерацию, если {@code onlyExplicitlyIncluded == true}.
     */
    @Target(ElementType.FIELD)
    @interface Include {
    }
}