package com.vega.protocol.client.ws;

import com.vega.protocol.model.Config;
import com.vega.protocol.model.ReferencePrice;
import com.vega.protocol.store.BinanceStore;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;

@Slf4j
public class BinanceWebSocketClient extends WebSocketClient {

    private final BinanceStore store = BinanceStore.getInstance();
    private final Config config = Config.getInstance();

    public BinanceWebSocketClient(URI uri) {
        super(uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onOpen(ServerHandshake handshake) {
        try {
            String symbol = config.getBinanceMarket().toLowerCase();
            JSONObject sub = new JSONObject()
                    .put("method", "SUBSCRIBE")
                    .put("params", new JSONArray().put(String.format("%s@ticker", symbol)))
                    .put("id", 1);
            this.send(sub.toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMessage(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            if(jsonObject.optString("e") != null &&
                    jsonObject.optString("e").equals("24hrTicker")) {
                String symbol = jsonObject.getString("s");
                double askPrice = jsonObject.getDouble("a");
                double bidPrice = jsonObject.getDouble("b");
                ReferencePrice referencePrice = new ReferencePrice()
                        .setSymbol(symbol)
                        .setAskPrice(askPrice)
                        .setBidPrice(bidPrice);
                store.save(referencePrice);
            }
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.error(reason);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onError(Exception e) {
        log.error(e.getMessage(), e);
    }
}