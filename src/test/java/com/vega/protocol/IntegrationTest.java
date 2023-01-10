package com.vega.protocol;

import com.vega.protocol.client.api.VegaApiClient;
import com.vega.protocol.model.Config;
import com.vega.protocol.model.Market;
import com.vega.protocol.store.VegaStore;
import org.junit.jupiter.api.Assertions;
import org.mockito.MockedStatic;

import java.util.List;

import static org.mockito.Mockito.mockStatic;

public abstract class IntegrationTest {

    private MockedStatic<Config> configMock;

    protected String getActivePartyId() {
        VegaApiClient apiClient = new VegaApiClient();
        return apiClient.getOpenOrders().get(0).getPartyId();
    }

    protected String getActiveMarketId() {
        VegaApiClient apiClient = new VegaApiClient();
        List<Market> markets = apiClient.getMarkets();
        List<Market> activeMarkets = markets.stream()
                .filter(m -> m.getState().equals("STATE_ACTIVE"))
                .toList();
        Assertions.assertTrue(activeMarkets.size() > 0);
        return activeMarkets.get(0).getId();
    }

    protected void setup() {
        Config config = Config.getInstance();
        config.setNodeUrl("https://api.n11.testnet.vega.xyz/api/v2");
        config.setWsUrl("wss://api.n11.testnet.vega.xyz/graphql");
        config.setWalletUrl("http://localhost:1789");
        config.setWalletUsername("trading");
        config.setWalletPassword("password123");
        configMock = mockStatic(Config.class);
        configMock.when(Config::getInstance).thenReturn(config);
        config.setPartyId(getActivePartyId());
        config.setMarketId(getActiveMarketId());
        configMock.when(Config::getInstance).thenReturn(config);
    }

    protected void teardown() {
        VegaStore.getInstance().truncate();
        configMock.close();
    }
}