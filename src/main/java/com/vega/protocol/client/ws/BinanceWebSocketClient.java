package com.vega.protocol.client.ws;

import com.vega.protocol.model.Config;
import com.vega.protocol.model.ReferencePrice;
import com.vega.protocol.store.BinanceStore;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
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
            JSONObject sub = new JSONObject()
                    .put("method", "ticker.24hr")
                    .put("params", new JSONObject().put("symbol", config.getBinanceMarket()))
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
            if(jsonObject.has("id") && jsonObject.getInt("id") == 1) {
                JSONObject data = jsonObject.getJSONObject("result");
                double askPrice = data.getDouble("askPrice");
                double bidPrice = data.getDouble("bidPrice");
                ReferencePrice referencePrice = new ReferencePrice()
                        .setSymbol(config.getBinanceMarket())
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