package com.vega.protocol.store;

import com.vega.protocol.model.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class VegaStore {

    private final Map<String, Market> markets = new ConcurrentHashMap<>();
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final Map<String, Position> positions = new ConcurrentHashMap<>();
    private final Map<String, Asset> assets = new ConcurrentHashMap<>();
    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    private static final VegaStore instance = new VegaStore();
    private VegaStore() {}

    /**
     * Get the {@link VegaStore} singleton instance
     *
     * @return {@link VegaStore}
     */
    public static VegaStore getInstance() {
        return instance;
    }

    /**
     * Get markets
     *
     * @return {@link List<Market>}
     */
    public List<Market> getMarkets() {
        return markets.values().stream().toList();
    }

    /**
     * Get open orders
     *
     * @return {@link List<Order>}
     */
    public List<Order> getOrders() {
        return orders.values().stream().toList();
    }

    /**
     * Get accounts
     *
     * @return {@link List<Account>}
     */
    public List<Account> getAccounts() {
        return accounts.values().stream().toList();
    }

    /**
     * Get positions
     *
     * @return {@link List<Position>}
     */
    public List<Position> getPositions() {
        return positions.values().stream().toList();
    }

    /**
     * Get assets
     *
     * @return {@link List<Asset>}
     */
    public List<Asset> getAssets() {
        return assets.values().stream().toList();
    }

    /**
     * Add or update market
     *
     * @param market {@link Market}
     */
    public void save(final Market market) {
        markets.put(market.getId(), market);
    }

    /**
     * Add or update order
     *
     * @param order {@link Order}
     */
    public void save(final Order order) {
        if(!order.getStatus().equals("STATUS_ACTIVE")) {
            orders.remove(order.getId());
        } else {
            orders.put(order.getId(), order);
        }
    }

    /**
     * Add or update position
     *
     * @param position {@link Position}
     */
    public void save(final Position position) {
        positions.put(position.getMarketId(), position);
    }

    /**
     * Add or update asset
     *
     * @param asset {@link Asset}
     */
    public void save(final Asset asset) {
        assets.put(asset.getId(), asset);
    }

    /**
     * Add or update account
     *
     * @param account {@link Account}
     */
    public void save(final Account account) {
        accounts.put(account.getId(), account);
    }

    /**
     * Get market by ID
     *
     * @param id the market ID
     *
     * @return {@link Optional<Market>}
     */
    public Optional<Market> getMarketById(final String id) {
        Market market = markets.get(id);
        if(market == null) {
            return Optional.empty();
        }
        return Optional.of(market);
    }

    /**
     * Get order by ID
     *
     * @param id the order ID
     *
     * @return {@link Optional<Order>}
     */
    public Optional<Order> getOrderById(final String id) {
        Order order = orders.get(id);
        if(order == null) {
            return Optional.empty();
        }
        return Optional.of(order);
    }

    /**
     * Get position by market ID
     *
     * @param marketId the market ID
     *
     * @return {@link Optional<Position>}
     */
    public Optional<Position> getPositionByMarketId(final String marketId) {
        Position position = positions.get(marketId);
        if(position == null) {
            return Optional.empty();
        }
        return Optional.of(position);
    }

    /**
     * Get asset by ID
     *
     * @param id the asset ID
     *
     * @return {@link Optional<Asset>}
     */
    public Optional<Asset> getAssetById(final String id) {
        return assets.values().stream().filter(a -> a.getId().equals(id)).findFirst();
    }

    /**
     * Clear all data from internal state
     */
    public void truncate() {
        assets.clear();
        accounts.clear();
        positions.clear();
        orders.clear();
        markets.clear();
    }
}