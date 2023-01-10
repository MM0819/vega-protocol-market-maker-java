package com.vega.protocol.submission;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OrderSubmission {
    private String marketId;
    private String size;
    private String price;
    private String timeInForce;
    private String type;
    private String side;
}