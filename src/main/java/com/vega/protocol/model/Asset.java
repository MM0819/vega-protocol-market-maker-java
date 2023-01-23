package com.vega.protocol.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Asset {
    private String id;
    private String status;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private AssetDetails details;
    public String getName() {
        if(details == null) return null;
        return details.getName();
    }
    public String getSymbol() {
        if(details == null) return null;
        return details.getSymbol();
    }
    public int getDecimals() {
        if(details == null) return 0;
        return Integer.parseInt(details.getDecimals());
    }
    @Data
    @Accessors(chain = true)
    public static class AssetDetails {
        private String name;
        private String symbol;
        private String decimals;
    }
}