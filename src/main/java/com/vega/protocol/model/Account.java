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
public class Account {
    @JsonAlias("partyId")
    private String owner;
    private String marketId;
    private String type;
    private String balance;
    @JsonAlias("assetId")
    private String asset;
    public Asset getAsset() {
        return VegaStore.getInstance().getAssetById(asset).orElseThrow(() ->
                new TradingException(String.format("asset not found: %s", asset)));
    }
    public String getId() {
        return String.format("%s-%s-%s-%s", owner, marketId, type, asset);
    }
    public double getBalance() {
        if(StringUtils.isEmpty(balance)) return 0;
        return DecimalUtils.convertToDecimals(getAsset().getDecimals(),
                new BigInteger(balance));
    }
}