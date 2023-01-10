package com.vega.protocol;

import com.vega.protocol.client.api.VegaApiClient;
import com.vega.protocol.model.AppState;
import com.vega.protocol.model.Config;
import com.vega.protocol.store.BinanceStore;
import com.vega.protocol.store.VegaStore;
import com.vega.protocol.strategy.SimpleMarketMaker;
import com.vega.protocol.strategy.TradingStrategy;
import com.vega.protocol.client.ws.BinanceWebSocketClient;
import com.vega.protocol.client.ws.VegaWebSocketClient;
import io.javalin.Javalin;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Application {

    private static VegaWebSocketClient vegaWebSocketClient;
    private static BinanceWebSocketClient binanceWebSocketClient;
    private static final VegaStore vegaStore = VegaStore.getInstance();
    private static final BinanceStore binanceStore = BinanceStore.getInstance();
    private static final VegaApiClient apiClient = new VegaApiClient();
    private static final Config config = Config.getInstance();
    private static final ExecutorService taskExecutor = Executors.newCachedThreadPool();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(8);

    public static void main(String[] args) {
        TradingStrategy tradingStrategy = new SimpleMarketMaker();
        taskExecutor.submit(Application::initializeWebSocketConnection);
        scheduler.scheduleAtFixedRate(Application::loadInitialData, 0, 30, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(Application::keepWebSocketsAlive, 3, 1, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(tradingStrategy::execute, 3, 5, TimeUnit.SECONDS);
        startApi();
    }

    private static void loadInitialData() {
        log.info("Refreshing data...");
        log.info("Saving assets");
        apiClient.getAssets().forEach(vegaStore::save);
        log.info("Saved {} assets", vegaStore.getAssets().size());
        log.info("Saving accounts");
        apiClient.getAccounts(config.getPartyId()).forEach(vegaStore::save);
        log.info("Saved {} accounts", vegaStore.getAccounts().size());
        log.info("Saving markets");
        apiClient.getMarkets().forEach(market -> {
            if(market.getId().equals(config.getMarketId())) {
                vegaStore.save(market);
            }
        });
        log.info("Saved {} markets", vegaStore.getMarkets().size());
        log.info("Saving orders");
        apiClient.getOpenOrders(config.getPartyId()).forEach(vegaStore::save);
        log.info("Saved {} orders", vegaStore.getOrders().size());
        log.info("Saving positions");
        apiClient.getPositions(config.getPartyId()).forEach(vegaStore::save);
        log.info("Saved {} positions", vegaStore.getPositions().size());
    }

    private static void initializeWebSocketConnection() {
        log.info("Connecting to Web Sockets...");
        vegaWebSocketClient = new VegaWebSocketClient(URI.create(config.getWsUrl()));
        vegaWebSocketClient.connect();
        binanceWebSocketClient = new BinanceWebSocketClient(URI.create(config.getBinanceWsUrl()));
        binanceWebSocketClient.connect();
    }

    private static void keepWebSocketsAlive() {
        if(vegaWebSocketClient.isClosed()) {
            log.warn("Vega Web Socket connection is closed, reconnecting...");
            vegaWebSocketClient.reconnect();
        }
        if(binanceWebSocketClient.isClosed()) {
            log.warn("Binance Web Socket connection is closed, reconnecting...");
            binanceWebSocketClient.reconnect();
        }
    }

    private static void startApi() {
        Javalin.create()
                .get("/state", ctx -> {
                    AppState appState = new AppState()
                            .setReferencePrices(binanceStore.getReferencePrices())
                            .setAccounts(vegaStore.getAccounts())
                            .setAssets(vegaStore.getAssets())
                            .setMarkets(vegaStore.getMarkets())
                            .setOrders(vegaStore.getOrders())
                            .setPositions(vegaStore.getPositions());
                    ctx.json(appState);
                }).start(7070);
    }
}