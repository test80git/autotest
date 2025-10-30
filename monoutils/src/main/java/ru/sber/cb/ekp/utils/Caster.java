package ru.sber.cb.ekp.utils;

import java.math.BigDecimal;import java.sql.Timestamp;import java.text.ParseException;import java.text.SimpleDateFormat;import java.util.Arrays;import java.util.Date;import java.util.List;

import static java.lang.Math.toIntExact;

public class Caster {


    // Поддерживаемые форматы дат
    private static final List<String> DATE_FORMATS = Arrays.asList(
            "yyyy-MM-dd HH:mm:ss",
            "yyyy/MM/dd HH:mm:ss",
            "dd-MM-yyyy HH:mm:ss",
            "dd/MM/yyyy HH:mm:ss",
            "yyyy-MM-dd",
            "yyyy/MM/dd",
            "dd/MM/yyyy",
            "dd-MM-yyyy",
            "yyyyMMdd HHmmss"
    );

    public static <T> T cast(Object obj, Class<T> clazz) {
        if (obj == null || clazz.isInstance(obj)) {
            return clazz.cast(obj);
        }

        // Булевое преобразование
        if (clazz == boolean.class || clazz == Boolean.class) {
            if (obj instanceof String) {
                String str = ((String) obj).toLowerCase();
                if ("true".equals(str) || "false".equals(str)) {
                    return (T) Boolean.valueOf(str);
                }
                throw new UnsupportedOperationException(
                        "Unsupported string value for Boolean conversion: " + obj);
            } else if (obj instanceof Number) {
                return (T) Boolean.valueOf(((Number) obj).intValue() != 0);
            } else {
                throw new UnsupportedOperationException(
                        "Unsupported type conversion from " + obj.getClass().getSimpleName()
                                + " to " + clazz.getSimpleName());
            }
        }

        // Обработка Timestamp
        if (clazz == Timestamp.class && obj instanceof String) {
            return (T) parseTimestamp((String) obj);
        }

        if (clazz == String.class) {
            if (obj instanceof Number) {
                return (T) obj.toString();
            } else if (obj instanceof Date) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                return (T) sdf.format(obj);
            } else {
                throw new UnsupportedOperationException(
                        "Unsupported type conversion from " + obj.getClass().getSimpleName()
                                + " to " + clazz.getSimpleName());
            }
        }

        if (obj instanceof BigDecimal) {
            BigDecimal bd = (BigDecimal) obj;
            if (bd.scale() <= 0) {
                if (clazz == Integer.class || clazz == int.class) {
                    return (T) Integer.valueOf(bd.intValueExact());

                } else if (clazz == Long.class || clazz == long.class) {
                    return (T) Long.valueOf(bd.longValueExact());

                } else {
                    throw new UnsupportedOperationException(
                            "Unsupported type conversion from " + obj.getClass().getSimpleName()
                                    + " to " + clazz.getSimpleName());
                }
            } else {
                throw new ArithmeticException("Cannot convert non-integer BigDecimal to integer type");
            }
        }

        if (obj instanceof Number) {
            if (clazz == Integer.class || clazz == int.class) {
                long value = ((Number) obj).longValue();
                return (T) Integer.valueOf(toIntExact(value));

            } else if (clazz == Long.class || clazz == long.class) {
                return (T) Long.valueOf(((Number) obj).longValue());

            } else if (clazz == BigDecimal.class) {
                return (T) new BigDecimal(String.valueOf(obj));

            } else {
                throw new UnsupportedOperationException(
                        "Unsupported type conversion from " + obj.getClass().getSimpleName()
                                + " to " + clazz.getSimpleName());
            }
        }

        if (obj instanceof String) {
            String str = (String) obj;
            if (clazz == Integer.class || clazz == int.class) {
                try {
                    return (T) Integer.valueOf(str);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Invalid string format for Integer conversion");
                }
            } else if (clazz == Long.class || clazz == long.class) {
                try {
                    return (T) Long.valueOf(str);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Invalid string format for Long conversion");
                }
            } else if (clazz == BigDecimal.class) {
                try {
                    return (T) new BigDecimal(str);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Invalid string format for BigDecimal conversion");
                }
            } else {
                throw new UnsupportedOperationException(
                        "Unsupported type conversion from " + obj.getClass().getSimpleName()
                                + " to " + clazz.getSimpleName());
            }
        }

        throw new UnsupportedOperationException(
                "Unsupported type conversion from " + obj.getClass().getSimpleName()
                        + " to " + clazz.getSimpleName());
    }

    private static Timestamp parseTimestamp(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        // Пробуем все форматы по очереди
        for (String format : DATE_FORMATS) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(format);
                dateFormat.setLenient(false); // Строгая проверка формата
                Date parsedDate = dateFormat.parse(dateStr);
                return new Timestamp(parsedDate.getTime());
            } catch (ParseException e) {
                // Пробуем следующий формат
            }
        }

        throw new IllegalArgumentException(String.format(
                "Неправильный формат даты. Поддерживаемые форматы: %s. Получено: '%s'",
                DATE_FORMATS, dateStr
        ));
    }
}