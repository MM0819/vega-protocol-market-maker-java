package com.vega.protocol.model;

import com.vega.protocol.exception.TradingException;
import com.vega.protocol.store.VegaStore;
import com.vega.protocol.utils.DecimalUtils;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;

@Data
@Accessors(chain = true)
public class Order {
    private String id;
    private String marketId;
    private String size;
    private String remaining;
    private String price;
    private String type;
    private String timeInForce;
    private String status;
    private String partyId;
    public Market getMarket() {
        return VegaStore.getInstance().getMarketById(marketId).orElseThrow(() ->
                new TradingException(String.format("market not found: %s", marketId)));
    }
    public double getPrice() {
        if(StringUtils.isEmpty(price)) return 0;
        return DecimalUtils.convertToDecimals(getMarket().getDecimalPlaces(),
                new BigInteger(price));
    }
    public double getSize() {
        if(StringUtils.isEmpty(size)) return 0;
        return DecimalUtils.convertToDecimals(getMarket().getPositionDecimalPlaces(),
                new BigInteger(size));
    }
}