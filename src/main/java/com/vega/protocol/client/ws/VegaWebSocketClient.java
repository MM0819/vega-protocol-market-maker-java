package com.vega.protocol.client.ws;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vega.protocol.model.*;
import com.vega.protocol.store.VegaStore;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.protocols.Protocol;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.util.Collections;
import java.util.function.Consumer;

@Slf4j
public class VegaWebSocketClient extends WebSocketClient {

    private final Config config = Config.getInstance();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private static final String ACCOUNTS_SUBSCRIPTION =
            """
                subscription {
                    accounts(partyId: "PARTY_ID") {
                        balance
                        type
                        assetId
                        marketId
                        partyId
                    }
                }
            """;

    private static final String ORDERS_SUBSCRIPTION =
            """
                subscription {
                    orders(partyId: "PARTY_ID") {
                        id
                        price
                        side
                        type
                        size
                        remaining
                        status
                        marketId
                    }
                }
            """;

    private static final String POSITIONS_SUBSCRIPTION =
            """
                subscription {
                    positions(partyId: "PARTY_ID") {
                        openVolume
                        realisedPNL
                        unrealisedPNL
                        averageEntryPrice
                        marketId
                    }
                }
            """;

    private static final String MARKETS_DATA_SUBSCRIPTION =
            """
                subscription {
                    marketsData(marketIds: "MARKET_ID") {
                        marketId
                        marketState
                        marketTradingMode
                        bestBidPrice
                        bestOfferPrice
                        bestBidVolume
                        bestOfferVolume
                        markPrice
                        targetStake
                        suppliedStake
                        openInterest
                    }
                }
            """;

    public VegaWebSocketClient(
            final URI uri
    ) {
        super(uri, new Draft_6455(Collections.emptyList(),
                Collections.singletonList(new Protocol("graphql-ws"))));
    }

    /**
     * Open a new GraphQL subscription over the current WebSocket connection
     *
     * @param id unique ID for the new subscription
     * @param query the GraphQL query
     * @param marketId optional market ID, to pass to the GraphQL query
     * @param partyId optional party ID, to pass to the GraphQL query
     */
    private void openSubscription(String id, String query, String marketId, String partyId) {
        if(!StringUtils.isEmpty(marketId)) {
            query = query.replace("MARKET_ID", marketId);
        }
        if(!StringUtils.isEmpty(partyId)) {
            query = query.replace("PARTY_ID", partyId);
        }
        JSONObject queryObject = new JSONObject()
                .put("query", query);
        JSONObject subscriptionObject = new JSONObject()
                .put("id", id)
                .put("type", "start")
                .put("payload", queryObject);
        this.send(subscriptionObject.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        JSONObject init = new JSONObject()
                .put("type", "connection_init");
        this.send(init.toString());
        openSubscription("marketsData", MARKETS_DATA_SUBSCRIPTION, config.getMarketId(), null);
        openSubscription("orders", ORDERS_SUBSCRIPTION, null, config.getPartyId());
        openSubscription("positions", POSITIONS_SUBSCRIPTION, null, config.getPartyId());
        openSubscription("accounts", ACCOUNTS_SUBSCRIPTION, null, config.getPartyId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMessage(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            String id = jsonObject.optString("id");
            JSONObject payload = jsonObject.optJSONObject("payload");
            if(!StringUtils.isEmpty(id) && payload != null) {
                JSONObject data = payload.getJSONObject("data");
                switch (id) {
                    case "marketsData" -> handleMarketsData(data);
                    case "orders" -> handleOrders(data);
                    case "positions" -> handlePositions(data);
                    case "accounts" -> handleAccounts(data);
                    default -> log.warn("Unsupported message");
                }
            }
        } catch(Exception e) {
            log.info(message);
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Generic method to handle WS messages and update internal state
     *
     * @param data the JSON data
     * @param key the key to read the items from the JSON array
     * @param type the object type
     * @param save callback function, used to update internal state
     */
    private <T> void handleItems(JSONObject data, String key, Class<T> type, Consumer<T> save) {
        JSONArray arr = data.optJSONArray(key);
        if(arr == null) {
            return;
        }
        for(int i=0; i<arr.length(); i++) {
            try {
                JSONObject obj = arr.getJSONObject(i);
                T item = objectMapper.readValue(obj.toString(), type);
                save.accept(item);
            } catch(Exception e) {
                log.info(data.toString());
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Handle {@link Order} updates
     *
     * @param data order updates, as JSON
     */
    private void handleOrders(JSONObject data) {
        handleItems(data, "orders", Order.class, (item) -> VegaStore.getInstance().save(item));
    }

    /**
     * Handle {@link Account} updates
     *
     * @param data account updates, as JSON
     */
    private void handleAccounts(JSONObject data) {
        handleItems(data, "accounts", Account.class, (item) -> VegaStore.getInstance().save(item));
    }

    /**
     * Handle {@link Position} updates
     *
     * @param data position updates, as JSON
     */
    private void handlePositions(JSONObject data) {
        handleItems(data, "positions", Position.class, (item) -> VegaStore.getInstance().save(item));
    }

    /**
     * Handle {@link MarketData} updates
     *
     * @param data market data updates, as JSON
     */
    private void handleMarketsData(JSONObject data) {
        JSONArray arr = data.getJSONArray("marketsData");
        VegaStore store = VegaStore.getInstance();
        for(int i=0; i<arr.length(); i++) {
            try {
                JSONObject obj = arr.getJSONObject(i);
                String id = obj.getString("marketId");
                String state = obj.getString("marketState");
                String tradingMode = obj.getString("marketTradingMode");
                MarketData marketData = objectMapper.readValue(obj.toString(),  MarketData.class);
                store.getMarketById(id).ifPresent(market -> {
                    market.setMarketData(marketData);
                    market.setTradingMode(tradingMode);
                    market.setState(state);
                    store.save(market);
                });
            } catch(Exception e) {
                log.info(data.toString());
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.error("Closed: {}, {}, {} !!", code, reason, remote);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onError(Exception e) {
        log.error(e.getMessage(), e);
    }
}