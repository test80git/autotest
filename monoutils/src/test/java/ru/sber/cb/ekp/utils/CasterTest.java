package ru.sber.cb.ekp.utils;

import org.junit.jupiter.api.Test;import org.junit.jupiter.params.ParameterizedTest;import org.junit.jupiter.params.provider.Arguments;import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;import java.sql.Timestamp;import java.text.SimpleDateFormat;import java.time.LocalDateTime;import java.time.ZonedDateTime;import java.util.stream.Stream;

import static org.junit.Assert.*;import static org.junit.jupiter.params.provider.Arguments.arguments;import static ru.sber.cb.ekp.utils.Caster.cast;

public class CasterTest {


    @Test
    public void testCastNullToAnyType() {
        assertNull(cast(null, Object.class));
    }

    @Test
    public void testCastObjectToSameType() {
        final String input = "test";
        assertEquals(input, cast(input, String.class));
    }

    @Test
    public void testConvertNumberToString() {
        final int number = 12345;
        assertEquals(Integer.toString(number), cast(number, String.class));
    }

    @Test
    public void testConvertTimestampToString() {
        ZonedDateTime zdt = LocalDateTime.of(2023, 8, 17, 12, 30).atZone(ZonedDateTime.now().getZone());
        Timestamp ts = Timestamp.from(zdt.toInstant());
        String expected = "17/08/2023";
        assertEquals(expected, cast(ts, String.class));
    }

    @Test
    public void testConvertPrimitiveIntToInteger() {
        final int primitiveInt = 123;
        assertEquals(primitiveInt, cast(primitiveInt, Integer.class).intValue());
    }

    @Test
    public void testConvertPrimitiveLongToLong() {
        final long primitiveLong = 123L;
        assertEquals(primitiveLong, cast(primitiveLong, Long.class).longValue());
    }

    @Test
    public void testConvertBigDecimalToInteger() {
        final BigDecimal bigDecimal = new BigDecimal("123");
        assertEquals(bigDecimal.intValue(), cast(bigDecimal, Integer.class).intValue());
    }

    @Test
    public void testConvertBigDecimalWithFractionalPartToIntegerFails() {
        final BigDecimal bigDecimal = new BigDecimal("123.45");
        assertThrows(ArithmeticException.class,
                () -> cast(bigDecimal, Integer.class)
        );
    }

    @Test
    public void testConvertBigDecimalToLong() {
        final BigDecimal bigDecimal = new BigDecimal("123");
        assertEquals(bigDecimal.longValue(), cast(bigDecimal, Long.class).longValue());
    }

    @Test
    public void testConvertBigDecimalWithFractionalPartToLongFails() {
        final BigDecimal bigDecimal = new BigDecimal("123.45");
        assertThrows(ArithmeticException.class,
                () -> cast(bigDecimal, Long.class));
    }

    @Test
    public void testConvertStringToInteger() {
        final String input = "123";
        assertEquals(Integer.parseInt(input), cast(input, Integer.class).intValue());
    }

    @Test
    public void testConvertInvalidStringToIntegerFails() {
        final String invalidInput = "abc";
        assertThrows(IllegalArgumentException.class,
                () -> cast(invalidInput, Integer.class));
    }

    @Test
    public void testConvertStringToLong() {
        final String input = "123";
        assertEquals(Long.parseLong(input), cast(input, Long.class).longValue());
    }

    @Test
    public void testConvertInvalidStringToLongFails() {
        final String invalidInput = "abc";
        assertThrows(IllegalArgumentException.class,
                () -> cast(invalidInput, Long.class));
    }

    @Test
    public void testConvertStringToBigDecimal() {
        final String input = "123.45";
        assertEquals(new BigDecimal(input), cast(input, BigDecimal.class));
    }

    @Test
    public void testConvertInvalidStringToBigDecimalFails() {
        final String invalidInput = "abc";
        assertThrows(IllegalArgumentException.class,
                () -> cast(invalidInput, BigDecimal.class));
    }

    @Test
    public void testConvertStringToOtherTypesFails() {
        final String input = "123";
        assertThrows(UnsupportedOperationException.class,
                () -> cast(input, Boolean.class));
    }

    @Test
    public void testConvertNonConvertibleTypeToAnotherTypeFails() {
        final boolean boolVal = true;
        assertThrows(UnsupportedOperationException.class,
                () -> cast(boolVal, Integer.class));
    }

    @Test
    public void testConvertNumberToBigDecimal() {
        final double number = 123.45;
        assertEquals(BigDecimal.valueOf(number), cast(number, BigDecimal.class));
    }

    @Test
    public void testConvertNegativeNumberToBigDecimal() {
        final double negativeNumber = -123.45;
        assertEquals(BigDecimal.valueOf(negativeNumber), cast(negativeNumber, BigDecimal.class));
    }

    @Test
    public void testConvertIntegerToBigDecimal() {
        final int integer = 123;
        assertEquals(BigDecimal.valueOf(integer), cast(integer, BigDecimal.class));
    }

    @Test
    public void testConvertLargeIntegerToBigDecimal() {
        final long largeInteger = 1_000_000_000_000L;
        assertEquals(BigDecimal.valueOf(largeInteger), cast(largeInteger, BigDecimal.class));
    }

    @ParameterizedTest
    @MethodSource("dateFormatsProvider")
    public void testConvertStringToTimestampWithVariousFormats(String dateStr, String expectedDate) {
        Timestamp result = cast(dateStr, Timestamp.class);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        assertEquals(expectedDate, sdf.format(result));
    }

    private static Stream<Arguments> dateFormatsProvider() {
        return Stream.of(
                arguments("2020-08-10", "10/08/2020"),  // yyyy-MM-dd
                arguments("10/08/2020", "10/08/2020"),  // dd/MM/yyyy
                arguments("10-08-2020", "10/08/2020"),  // dd-MM-yyyy
                arguments("2020/08/10", "10/08/2020"),  // yyyy/MM/dd
                arguments("20200810 120000", "10/08/2020"),  // yyyyMMdd HHmmss
                arguments("10/08/2020 12:30:45", "10/08/2020"),  // dd/MM/yyyy HH:mm:ss
                arguments("10-08-2020 12:30:45", "10/08/2020"),  // dd-MM-yyyy HH:mm:ss
                arguments("2020/08/10 12:30:45", "10/08/2020")   // yyyy/MM/dd HH:mm:ss
        );
    }

    @Test
    public void testConvertInvalidDateStringToTimestampFails() {
        assertThrows(IllegalArgumentException.class,
                () -> cast("invalid-date", Timestamp.class));
    }

    @Test
    public void testConvertEmptyStringToTimestampReturnsNull() {
        assertNull(cast("", Timestamp.class));
    }

    @Test
    public void testConvertNullToTimestampReturnsNull() {
        assertNull(cast(null, Timestamp.class));
    }

    @ParameterizedTest
    @MethodSource("dateTimeFormatsProvider")
    public void testConvertStringWithTimeToTimestamp(String dateTimeStr, String expectedDateTime) {
        Timestamp result = cast(dateTimeStr, Timestamp.class);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        assertEquals(expectedDateTime, sdf.format(result));
    }

    private static Stream<Arguments> dateTimeFormatsProvider() {
        return Stream.of(
                arguments("10/08/2020 12:30:45", "10/08/2020 12:30:45"),
                arguments("10-08-2020 12:30:45", "10/08/2020 12:30:45"),
                arguments("2020/08/10 12:30:45", "10/08/2020 12:30:45"),
                arguments("2020-08-10 12:30:45", "10/08/2020 12:30:45")
        );
    }
}