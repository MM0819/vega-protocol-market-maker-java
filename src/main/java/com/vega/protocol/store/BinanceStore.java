package com.vega.protocol.store;

import com.vega.protocol.model.ReferencePrice;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class BinanceStore {
    private final Map<String, ReferencePrice> referencePrices = new ConcurrentHashMap<>();
    private static final BinanceStore instance = new BinanceStore();
    private BinanceStore() {}

    /**
     * Get the {@link BinanceStore} singleton instance
     *
     * @return {@link BinanceStore}
     */
    public static BinanceStore getInstance() {
        return instance;
    }

    /**
     * Add or update reference price
     *
     * @param referencePrice {@link ReferencePrice}
     */
    public void save(final ReferencePrice referencePrice) {
        referencePrices.put(referencePrice.getSymbol(), referencePrice);
    }

    /**
     * Get reference price by market
     *
     * @param symbol the market symbol
     *
     * @return {@link Optional < ReferencePrice >}
     */
    public Optional<ReferencePrice> getReferencePriceByMarket(final String symbol) {
        ReferencePrice referencePrice = referencePrices.get(symbol);
        if(referencePrice == null) {
            return Optional.empty();
        }
        return Optional.of(referencePrice);
    }

    /**
     * Get all reference prices
     *
     * @return {@link List<ReferencePrice>}
     */
    public List<ReferencePrice> getReferencePrices() {
        return referencePrices.values().stream().toList();
    }
}