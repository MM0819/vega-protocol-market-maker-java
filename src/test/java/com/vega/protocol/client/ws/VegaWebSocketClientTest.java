package com.vega.protocol.client.ws;

import com.vega.protocol.IntegrationTest;
import com.vega.protocol.client.api.VegaApiClient;
import com.vega.protocol.client.ws.VegaWebSocketClient;
import com.vega.protocol.model.Config;
import com.vega.protocol.store.VegaStore;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;

@Slf4j
public class VegaWebSocketClientTest extends IntegrationTest {

    @BeforeEach
    public void setup() {
        super.setup();
    }

    @AfterEach
    public void teardown() {
        super.teardown();
    }

    @Test
    public void testConnect() throws InterruptedException {
        VegaWebSocketClient webSocketClient = new VegaWebSocketClient(
                URI.create(Config.getInstance().getWsUrl()));
        VegaApiClient apiClient = new VegaApiClient();
        VegaStore store = VegaStore.getInstance();
        apiClient.getMarkets().forEach(store::save);
        webSocketClient.connect();
        Thread.sleep(5000L);
//        Assertions.assertTrue(store.getOrders().size() > 0);
//        Assertions.assertTrue(store.getPositions().size() > 0);
//        Assertions.assertTrue(store.getMarkets().size() > 0);
//        Assertions.assertTrue(store.getAccounts().size() > 0);
        webSocketClient.close();
        Thread.sleep(500L);
    }
}
