package com.vega.protocol.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

public class DecimalUtilsTest {

    @Test
    public void testConvertToDecimals() {
        double result = DecimalUtils.convertToDecimals(2, new BigInteger("1000"));
        Assertions.assertEquals(result, 10.00, 0d);
    }

    @Test
    public void testConvertFromDecimals() {
        BigInteger result = DecimalUtils.convertFromDecimals(2, 10.00);
        Assertions.assertEquals(result.toString(), "1000");
    }

    @Test
    public void testInstantiation() {
        new DecimalUtils();
    }
}