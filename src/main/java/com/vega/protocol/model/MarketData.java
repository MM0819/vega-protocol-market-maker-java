package com.vega.protocol.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MarketData {
    private String markPrice;
    private String bestBidPrice;
    private String bestOfferPrice;
    private String bestBidVolume;
    private String bestOfferVolume;
    private String openInterest;
}