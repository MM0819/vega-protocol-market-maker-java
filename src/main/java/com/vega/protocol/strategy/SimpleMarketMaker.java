package com.vega.protocol.strategy;

import com.vega.protocol.client.api.VegaApiClient;
import com.vega.protocol.model.*;
import com.vega.protocol.store.BinanceStore;
import com.vega.protocol.store.VegaStore;
import com.vega.protocol.submission.OrderCancellation;
import com.vega.protocol.submission.OrderSubmission;
import com.vega.protocol.utils.DecimalUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class SimpleMarketMaker implements TradingStrategy {

    private final Config config = Config.getInstance();

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() {
        log.info("Executing trading strategy...");
        VegaStore vegaStore = VegaStore.getInstance();
        BinanceStore binanceStore = BinanceStore.getInstance();
        String marketId = config.getMarketId();
        VegaApiClient apiClient = new VegaApiClient();
        vegaStore.getMarketById(marketId).ifPresent(market -> {
            log.info("Updating quotes for {}", market.getTradableInstrument().getInstrument().getName());
            ReferencePrice referencePrice = binanceStore.getReferencePriceByMarket(config.getBinanceMarket())
                    .orElse(new ReferencePrice().setAskPrice(0).setBidPrice(0));
            double bestOfferPrice = referencePrice.getAskPrice();
            double bestBidPrice = referencePrice.getBidPrice();
            if(bestBidPrice > 0 && bestOfferPrice > 0) {
                Optional<Position> positionOptional = vegaStore.getPositionByMarketId(market.getId());
                Position position = positionOptional.orElse(new Position().setOpenVolume("0").setAverageEntryPrice("0"));
                double openVolume = position.getOpenVolume();
                double averageEntryPrice = position.getAverageEntryPrice();
                double balance = getTotalBalance(market.getSettlementAsset().getId());
                double bidVolume = (balance * 0.5) - (openVolume * averageEntryPrice);
                double offerVolume = (balance * 0.5) + (openVolume * averageEntryPrice);
                bidVolume = Math.max(bidVolume, 0);
                offerVolume = Math.max(offerVolume, 0);
                double notionalExposure = Math.abs(openVolume * averageEntryPrice);
                log.info("Open volume = {}; Entry price = {}; Notional exposure = {}",
                        openVolume, averageEntryPrice, notionalExposure);
                log.info("Bid volume = {}; Offer volume = {}", bidVolume, offerVolume);
                List<Order> orders = vegaStore.getOrders();
                List<OrderCancellation> cancellations = orders.stream().map(o ->
                        new OrderCancellation().setOrderId(o.getId()).setMarketId(o.getMarketId())).toList();
                List<OrderSubmission> submissions = new ArrayList<>();
                addOrderSubmissions(submissions, bestBidPrice, "BUY", market, bidVolume);
                addOrderSubmissions(submissions, bestOfferPrice, "SELL", market, offerVolume);
                log.info("Cancellations = {}; Amendments = {}; Submissions = {}",
                        cancellations.size(), 0, submissions.size());
                Optional<String> txHash = apiClient.sendBatchMarketInstruction(
                        submissions, cancellations, Collections.emptyList());
                txHash.ifPresent(s -> log.info("Updated quotes {}", s));
            }
        });
    }

    /**
     * Get the total for settlement asset
     *
     * @param settlementAssetId the settlement asset ID
     *
     * @return total balance
     */
    private double getTotalBalance(final String settlementAssetId) {
        return VegaStore.getInstance()
                .getAccounts()
                .stream()
                .filter(a -> a.getAsset().getId().equals(settlementAssetId) &&
                        a.getOwner().equals(config.getPartyId()))
                .mapToDouble(Account::getBalance)
                .sum();
    }

    /**
     * Add an {@link OrderSubmission} to the submissions array
     *
     * @param submissions array of new orders
     * @param referencePrice the reference price to anchor the orders to (typically best bid or offer)
     * @param side the side of the book for the new orders
     * @param market the target market
     * @param targetVolume the sum of all quoted volume
     */
    private void addOrderSubmissions(
            final List<OrderSubmission> submissions,
            final double referencePrice,
            final String side,
            final Market market,
            final double targetVolume
    ) {
        double size = targetVolume / (5 * referencePrice);
        for(int i=1; i<=5; i++) {
            double price = side.equals("BUY") ?
                    referencePrice * (1 - (i * 0.002)) :
                    referencePrice * (1 + (i * 0.002));
            String sizeAsString = DecimalUtils.convertFromDecimals(
                    market.getPositionDecimalPlaces(), size).toString();
            String priceAsString = DecimalUtils.convertFromDecimals(
                    market.getDecimalPlaces(), price).toString();
            submissions.add(new OrderSubmission()
                    .setMarketId(market.getId())
                    .setSize(sizeAsString)
                    .setPrice(priceAsString)
                    .setSide(String.format("SIDE_%s", side))
                    .setType("TYPE_LIMIT")
                    .setTimeInForce("TIME_IN_FORCE_GTC"));
        }
    }
}
