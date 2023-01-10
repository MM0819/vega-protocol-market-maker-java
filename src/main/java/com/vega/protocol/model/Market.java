package com.vega.protocol.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vega.protocol.exception.TradingException;
import com.vega.protocol.store.VegaStore;
import com.vega.protocol.utils.DecimalUtils;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigInteger;

@Data
@Accessors(chain = true)
public class Market {
    private String id;
    private String state;
    private String tradingMode;
    private int decimalPlaces;
    private int positionDecimalPlaces;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private TradableInstrument tradableInstrument;
    @JsonIgnore
    private MarketData marketData;
    @Data
    public static class TradableInstrument {
        private Instrument instrument;
    }
    @Data
    public static class Instrument {
        private String code;
        private String name;
        private Future future;
    }
    @Data
    public static class Future {
        private String settlementAsset;
    }
    public String getCode() {
        if(tradableInstrument == null) return null;
        return tradableInstrument.getInstrument().getCode();
    }
    public String getName() {
        if(tradableInstrument == null) return null;
        return tradableInstrument.getInstrument().getName();
    }
    public Asset getSettlementAsset() {
        if(tradableInstrument.getInstrument() == null) return null;
        String settlementAssetId = tradableInstrument.getInstrument().getFuture().getSettlementAsset();
        return VegaStore.getInstance().getAssetById(settlementAssetId).orElseThrow(() ->
                new TradingException(String.format("asset not found: %s", settlementAssetId)));
    }
    public double getMarkPrice() {
        if(marketData == null) return 0;
        return DecimalUtils.convertToDecimals(decimalPlaces,
                new BigInteger(marketData.getMarkPrice()));
    }
    public double getBestBidPrice() {
        if(marketData == null) return 0;
        return DecimalUtils.convertToDecimals(decimalPlaces,
                new BigInteger(marketData.getBestBidPrice()));
    }
    public double getBestOfferPrice() {
        if(marketData == null) return 0;
        return DecimalUtils.convertToDecimals(decimalPlaces,
                new BigInteger(marketData.getBestOfferPrice()));
    }
    public double getBestBidVolume() {
        if(marketData == null) return 0;
        return DecimalUtils.convertToDecimals(positionDecimalPlaces,
                new BigInteger(marketData.getBestBidVolume()));
    }
    public double getBestOfferVolume() {
        if(marketData == null) return 0;
        return DecimalUtils.convertToDecimals(positionDecimalPlaces,
                new BigInteger(marketData.getBestOfferVolume()));
    }
    public double getOpenInterest() {
        if(marketData == null) return 0;
        return DecimalUtils.convertToDecimals(positionDecimalPlaces,
                new BigInteger(marketData.getOpenInterest()));
    }
}