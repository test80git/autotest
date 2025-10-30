package ru.sber.cb.ekp.annotation;

public @interface StepDto {
    Class<?> rqDto();


    Class<?> rsDto();
}