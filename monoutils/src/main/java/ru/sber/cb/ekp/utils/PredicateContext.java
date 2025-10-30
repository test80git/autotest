package ru.sber.cb.ekp.utils;

import org.springframework.data.util.Pair;

import java.util.HashMap;
import java.util.function.Function;

public class PredicateContext {
    private static final HashMap<String, Pair<Function<Object, Boolean>, Class<?>>> predicates = new HashMap<>();


    public static void register(String predicateName, Function<Object, Boolean> predicate, Class<?> clazz) {
        predicates.put(predicateName, Pair.of(predicate, clazz));
    }

    public static Boolean check(String predicateName, Object object) {
        if (!predicates.containsKey(predicateName)) throw new RuntimeException("Predicate not found");

        var pair = predicates.get(predicateName);
        return pair.getFirst().apply(Caster.cast(object, pair.getSecond()));
    }
}