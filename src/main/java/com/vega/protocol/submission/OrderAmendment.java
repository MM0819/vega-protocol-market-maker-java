package com.vega.protocol.submission;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OrderAmendment {
    private String orderId;
    private String sizeDelta;
    private String price;
}