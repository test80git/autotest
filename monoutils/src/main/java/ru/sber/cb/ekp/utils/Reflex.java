package ru.sber.cb.ekp.utils;

import java.lang.reflect.Field;import java.util.Optional;import java.util.regex.Matcher;import java.util.regex.Pattern;

public class Reflex {    private static final Pattern pathPat = Pattern.compile("\\.(?<field>\\w+)");


    public static Optional<Object> parseObjectFields(Object obj, String path) throws NoSuchFieldException, IllegalAccessException {
        Class<?> currentClass = obj.getClass();
        Field field;

        Matcher matcher = pathPat.matcher(path);
        while (matcher.find()) {
            if (obj == null) return Optional.empty();

            String fieldName = matcher.group("field");
            boolean found = false;

            // Обходим все классы вверх по цепочке наследования
            for (; currentClass != null; currentClass = currentClass.getSuperclass()) {
                try {
                    field = currentClass.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    obj = field.get(obj);
                    found = true;
                    currentClass = obj.getClass();
                    break;
                } catch (NoSuchFieldException e) {
                    continue;
                }
            }

            if (!found) throw new NoSuchFieldException("Field '" + fieldName + "' not found in class hierarchy.");
        }

        return Optional.ofNullable(obj);
    }
}