package ru.sber.cb.ekp.utils;

import java.util.List;import java.util.Objects;import java.util.function.Function;

public class FieldHandler {    public static<T> Boolean checkListField(String arrayIdx, List<T> list, Function<T, Boolean> function) {        if (Objects.equals(arrayIdx, "all")) {            return list.stream().allMatch(function::apply);        }        if (Objects.equals(arrayIdx, "any")) {            return list.stream().anyMatch(function::apply);        }        return function.apply(list.get(Integer.parseInt(arrayIdx)));    }}