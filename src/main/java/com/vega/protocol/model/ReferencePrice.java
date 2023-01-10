package com.vega.protocol.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ReferencePrice {
    private String symbol;
    private double bidPrice;
    private double askPrice;
}
