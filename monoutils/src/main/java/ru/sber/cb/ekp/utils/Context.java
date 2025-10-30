package ru.sber.cb.ekp.utils;

import java.math.BigDecimal;import java.util.Arrays;import java.util.Collections;import java.util.HashMap;import java.util.List;import java.util.Optional;import java.util.regex.Matcher;import java.util.regex.Pattern;import java.util.stream.Collectors;

import static ru.sber.cb.ekp.utils.Reflex.parseObjectFields;

@SuppressWarnings("unchecked")public class Context {    private static final HashMap<String, Object> vault = new HashMap<>();


    private static final Pattern stringPat = Pattern.compile("\"(?<val>.*?)\"");
    private static final Pattern numberPat = Pattern.compile("(?<main>-?\\d+)((?<ll>L)|(?<dec>\\.\\d+))?");
    private static final Pattern pathPat = Pattern.compile("(?<obj>[A-Z]\\w+)(?<path>(\\.\\w+)*)");

    public static <T> Optional<T> getContext(String key, Class<T> type) {
        if (!vault.containsKey(key)) {
            return Optional.empty();
        }
        Object obj = vault.get(key);
        if (String.class.equals(type) && !String.class.equals(obj.getClass())) {
            return Optional.ofNullable((T) key);
        }
        T result = (T) vault.get(key); // ClassCastException otherwise
        return Optional.ofNullable(result);
    }

    public static Optional<Object> getContext(String key) {
        return Optional.ofNullable(vault.get(key));
    }

    public static <T> void setContext(String key, T entity) {
        vault.put(key, entity);
    }

    public static boolean hasContext(String key) {
        return vault.containsKey(key);
    }

    public static Optional<Object> getSmartContext(String key) {
        Matcher matcher = stringPat.matcher(key);
        if (matcher.matches()) {
            return Optional.of(matcher.group("val"));
        }

        matcher = numberPat.matcher(key);
        if (matcher.matches()) {
            if (matcher.group("ll") != null) {
                return Optional.of(Long.valueOf(matcher.group("main")));
            }
            if (matcher.group("dec") != null) {
                return Optional.of(new BigDecimal(key));
            }
            try {
                return Optional.of(Integer.valueOf(key));
            } catch (NumberFormatException exception1) {
                try {
                    return Optional.of(Long.valueOf(key));
                } catch (NumberFormatException exception2) {
                    return Optional.of(new BigDecimal(key));
                }
            }
        }
        if (key.equalsIgnoreCase("true") || key.equalsIgnoreCase("false")) {
            return Optional.of(Boolean.parseBoolean(key));
        }
        if (key.equals("null")) return Optional.empty();

        matcher = pathPat.matcher(key);
        if (matcher.matches()) {
            Optional<Object> oObj = getContext(matcher.group("obj"));
            if (oObj.isEmpty() || matcher.group("path").isEmpty()) return oObj;

            try {
                return parseObjectFields(oObj.orElseThrow(), matcher.group("path"));
            } catch (NoSuchFieldException | IllegalAccessException exception) {
                return Optional.empty();
            }
        }

        return getContext(key);
    }

    public static List<String> getListFromContext(String key) {
        Optional<Object> value = getSmartContext(key);
        if (value.isEmpty()) {
            return Collections.emptyList();
        }

        String stringValue = value.get().toString();
        return Arrays.stream(stringValue.split("\\s*,\\s*"))
                .map(s -> s.replaceAll("^\"|\"$", ""))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public static void clear() {
        vault.clear();
    }
}
