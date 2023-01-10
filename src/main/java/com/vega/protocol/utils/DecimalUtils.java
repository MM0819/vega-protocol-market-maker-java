package com.vega.protocol.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public final class DecimalUtils {

    /**
     * Convert integer representation of a number into decimal representation
     *
     * @param decimalPlaces number of decimal places
     * @param number the integer representation
     *
     * @return the decimal representation
     */
    public static double convertToDecimals(
            final int decimalPlaces,
            final BigInteger number
    ) {
        BigDecimal modifier = BigDecimal.valueOf(Math.pow(10, decimalPlaces));
        return new BigDecimal(number).divide(modifier, decimalPlaces, RoundingMode.HALF_DOWN)
                .setScale(decimalPlaces, RoundingMode.HALF_DOWN).doubleValue();
    }

    /**
     * Convert decimal representation of a number into integer representation
     *
     * @param decimalPlaces number of decimal places
     * @param number the decimal representation
     *
     * @return the integer representation
     */
    public static BigInteger convertFromDecimals(
            final int decimalPlaces,
            final double number
    ) {
        return BigDecimal.valueOf(Math.pow(10, decimalPlaces)).multiply(BigDecimal.valueOf(number))
                .setScale(0, RoundingMode.HALF_DOWN).toBigInteger();
    }
}