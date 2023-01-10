package com.vega.protocol.client.api;

import com.vega.protocol.IntegrationTest;
import com.vega.protocol.client.api.VegaApiClient;
import com.vega.protocol.model.Account;
import com.vega.protocol.model.Asset;
import com.vega.protocol.model.Order;
import com.vega.protocol.model.Position;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class VegaApiClientTest extends IntegrationTest {

    @BeforeEach
    public void setup() {
        super.setup();
    }

    @AfterEach
    public void teardown() {
        super.teardown();
    }

    @Test
    public void testGetOrdersByPartyId() {
        VegaApiClient apiClient = new VegaApiClient();
        List<Order> orders = apiClient.getOrders(getActivePartyId());
        Assertions.assertTrue(orders.size() > 0);
    }

    @Test
    public void testGetOrders() {
        VegaApiClient apiClient = new VegaApiClient();
        List<Order> orders = apiClient.getOrders();
        Assertions.assertTrue(orders.size() > 0);
    }

    @Test
    public void testGetOpenOrdersByPartyId() {
        VegaApiClient apiClient = new VegaApiClient();
        List<Order> orders = apiClient.getOpenOrders(getActivePartyId());
        Assertions.assertTrue(orders.size() > 0);
    }

    @Test
    public void testGetOpenOrders() {
        VegaApiClient apiClient = new VegaApiClient();
        List<Order> orders = apiClient.getOpenOrders();
        Assertions.assertTrue(orders.size() > 0);
    }

    @Test
    public void testGetAccountsByPartyId() {
        VegaApiClient apiClient = new VegaApiClient();
        List<Account> accounts = apiClient.getAccounts(getActivePartyId());
        Assertions.assertTrue(accounts.size() > 0);
    }

    @Test
    public void testGetAccounts() {
        VegaApiClient apiClient = new VegaApiClient();
        List<Account> accounts = apiClient.getAccounts();
        Assertions.assertTrue(accounts.size() > 0);
    }

    @Test
    public void testGetAssets() {
        VegaApiClient apiClient = new VegaApiClient();
        List<Asset> assets = apiClient.getAssets();
        Assertions.assertTrue(assets.size() > 0);
    }

    @Test
    public void testGetPositionsByPartyId() {
        VegaApiClient apiClient = new VegaApiClient();
        List<Position> positions = apiClient.getPositions(getActivePartyId());
        Assertions.assertTrue(positions.size() > 0);
    }
}