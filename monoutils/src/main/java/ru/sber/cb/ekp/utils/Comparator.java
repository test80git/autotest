package ru.sber.cb.ekp.utils;

import org.junit.Assert;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class Comparator {
    public static boolean safeCompare(ComparatorEnum equalsType, Object expected, Object target) {
        switch (equalsType) {
            case EQUALS -> {
                if (Objects.equals(getClass(expected).orElse(null),
                        getClass(target).orElse(null))) {
                    return Objects.equals(expected, target);
                } else {
                    return Objects.equals(toString(expected), toString(target));
                }
            }
            case NOT_EQUALS -> {
                if (Objects.equals(getClass(expected).orElse(null), getClass(target).orElse(null))) {
                    return !Objects.equals(expected, target);
                } else {
                    return !Objects.equals(toString(expected), toString(target));
                }
            }
            case LIKE -> {
                return Pattern.compile(toString(expected).orElse("^$")).matcher(toString(target).orElse("")).find();
            }
            case NOT_LIKE -> {
                return !Pattern.compile(toString(expected).orElse("^$")).matcher(toString(target).orElse("")).find();
            }
            case CONTAINS -> {
                return toString(target).orElse("").contains(toString(expected).orElse(""));
            }
            case NOT_CONTAINS -> {
                return !toString(target).orElse("").contains(toString(expected).orElse(""));
            }
        }
        return false;
    }


    public static boolean compare(ComparatorEnum equalsType, Object expected, Object target) {
        switch (equalsType) {
            case EQUALS -> {
                if (Objects.equals(getClass(expected).orElse(null), getClass(target).orElse(null))) {
                    Assert.assertEquals(expected, target);
                } else {
                    Assert.assertEquals(toString(expected), toString(target));
                }
            }
            case NOT_EQUALS -> {
                if (Objects.equals(getClass(expected).orElse(null), getClass(target).orElse(null))) {
                    Assert.assertNotEquals(expected, target);
                } else {
                    Assert.assertNotEquals(toString(expected), toString(target));
                }
            }
            case LIKE -> {
                Assert.assertTrue(Pattern.compile(toString(expected).orElse("^$")).matcher(toString(target).orElse("")).find());
            }
            case NOT_LIKE -> {
                Assert.assertFalse(Pattern.compile(toString(expected).orElse("^$")).matcher(toString(target).orElse("")).find());
            }
            case CONTAINS -> {
                Assert.assertTrue(toString(expected).orElse("").contains(toString(target).orElse("")));
            }
            case NOT_CONTAINS -> {
                Assert.assertFalse(toString(expected).orElse("").contains(toString(target).orElse("")));
            }
            default -> Assert.fail("Wrong comparing operator");
        }
        return true;
    }

    public static void all(ComparatorEnum equalsType, Object expected, Collection<Object> target) {
        target.forEach(obj -> compare(equalsType, expected, obj));
    }

    public static void any(ComparatorEnum equalsType, Object expected, Collection<Object> target) {
        Assert.assertTrue(target.stream().anyMatch(obj -> safeCompare(equalsType, expected, obj)));
    }

    public enum ComparatorEnum {
        EQUALS,
        LIKE,
        NOT_EQUALS,
        NOT_LIKE,
        CONTAINS,
        NOT_CONTAINS
    }

    private static Optional<Class<?>> getClass(Object object) {
        if (Objects.isNull(object)) return Optional.empty();
        else return Optional.of(object.getClass());
    }

    private static Optional<String> toString(Object object) {
        if (Objects.isNull(object)) return Optional.empty();
        else return Optional.of(object.toString());
    }
}
