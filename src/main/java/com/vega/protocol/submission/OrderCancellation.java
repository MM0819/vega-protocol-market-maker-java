package com.vega.protocol.submission;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OrderCancellation {
    private String orderId;
    private String marketId;
}