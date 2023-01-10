package com.vega.protocol.store;

import com.vega.protocol.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class VegaStoreTest {

    @BeforeEach
    public void setup() {
        VegaStore.getInstance().truncate();
    }

    @Test
    public void testSaveMarket() {
        Optional<Market> market = VegaStore.getInstance().getMarketById("1");
        Assertions.assertFalse(market.isPresent());
        VegaStore.getInstance().save(new Market().setId("1"));
        Assertions.assertEquals(1, VegaStore.getInstance().getMarkets().size());
        market = VegaStore.getInstance().getMarketById("1");
        Assertions.assertTrue(market.isPresent());
        Assertions.assertEquals("1", market.get().getId());
    }

    @Test
    public void testSaveAsset() {
        VegaStore.getInstance().save(new Asset().setId("1"));
        Assertions.assertEquals(1, VegaStore.getInstance().getAssets().size());
        Optional<Asset> asset = VegaStore.getInstance().getAssetById("1");
        Assertions.assertTrue(asset.isPresent());
        Assertions.assertEquals("1", asset.get().getId());
    }

    @Test
    public void testSaveAccount() {
        VegaStore.getInstance().save(new Account().setOwner("1"));
        Assertions.assertEquals(1, VegaStore.getInstance().getAccounts().size());
    }

    @Test
    public void testSavePosition() {
        VegaStore.getInstance().save(new Position().setMarketId("1"));
        Assertions.assertEquals(1, VegaStore.getInstance().getPositions().size());
    }

    @Test
    public void testSaveOrder() {
        VegaStore.getInstance().save(new Order().setId("1").setStatus("STATUS_ACTIVE"));
        Assertions.assertEquals(1, VegaStore.getInstance().getOrders().size());
        Optional<Order> order = VegaStore.getInstance().getOrderById("1");
        Assertions.assertTrue(order.isPresent());
        Assertions.assertEquals("1", order.get().getId());
        VegaStore.getInstance().save(new Order().setId("1").setStatus("STATUS_CANCELLED"));
        Assertions.assertEquals(0, VegaStore.getInstance().getOrders().size());
        order = VegaStore.getInstance().getOrderById("1");
        Assertions.assertFalse(order.isPresent());
    }
}