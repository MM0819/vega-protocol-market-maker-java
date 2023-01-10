package com.vega.protocol.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.vega.protocol.exception.TradingException;
import com.vega.protocol.store.VegaStore;
import com.vega.protocol.utils.DecimalUtils;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;

@Data
@Accessors(chain = true)
public class Position {
    private String partyId;
    private String marketId;
    private String openVolume;
    private String averageEntryPrice;
    @JsonAlias("unrealisedPNL")
    private String unrealisedPnl;
    @JsonAlias("realisedPNL")
    private String realisedPnl;
    public String getSide() {
        if(getOpenVolume() > 0) {
            return "BUY";
        } else if(getOpenVolume() < 0) {
            return "SELL";
        }
        return null;
    }
    public Market getMarket() {
        return VegaStore.getInstance().getMarketById(marketId).orElseThrow(() ->
                new TradingException(String.format("market not found: %s", marketId)));
    }
    public double getOpenVolume() {
        return DecimalUtils.convertToDecimals(getMarket().getPositionDecimalPlaces(),
                new BigInteger(openVolume));
    }
    public double getAverageEntryPrice() {
        if(StringUtils.isEmpty(averageEntryPrice)) return 0;
        return DecimalUtils.convertToDecimals(getMarket().getDecimalPlaces(),
                new BigInteger(averageEntryPrice));
    }
    public double getUnrealisedPnl() {
        if(StringUtils.isEmpty(unrealisedPnl)) return 0;
        return DecimalUtils.convertToDecimals(getMarket().getSettlementAsset().getDecimals(),
                new BigInteger(unrealisedPnl));
    }
    public double getRealisedPnl() {
        if(StringUtils.isEmpty(realisedPnl)) return 0;
        return DecimalUtils.convertToDecimals(getMarket().getSettlementAsset().getDecimals(),
                new BigInteger(realisedPnl));
    }
}