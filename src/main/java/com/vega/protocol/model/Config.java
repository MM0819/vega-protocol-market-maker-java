package com.vega.protocol.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Config {

    private String nodeUrl;
    private String tendermintUrl;
    private String wsUrl;
    private String walletUrl;
    private String walletUsername;
    private String walletPassword;
    private String marketId;
    private String partyId;
    private String binanceMarket;
    private String binanceWsUrl;

    private static Config config = new Config();

    public static Config getInstance() {
        String nodeUrl = System.getenv("NODE_URL");
        String tendermintUrl = System.getenv("TENDERMINT_URL");
        String walletUrl = System.getenv("WALLET_URL");
        String wsUrl = System.getenv("WS_URL");
        String walletUsername = System.getenv("WALLET_USERNAME");
        String walletPassword = System.getenv("WALLET_PASSWORD");
        String marketId = System.getenv("MARKET_ID");
        String partyId = System.getenv("PARTY_ID");
        String binanceMarket = System.getenv("BINANCE_MARKET");
        String binanceWsUrl = System.getenv("BINANCE_WS_URL");
        config.setNodeUrl(nodeUrl);
        config.setTendermintUrl(tendermintUrl);
        config.setWalletUrl(walletUrl);
        config.setWsUrl(wsUrl);
        config.setWalletUsername(walletUsername);
        config.setWalletPassword(walletPassword);
        config.setMarketId(marketId);
        config.setPartyId(partyId);
        config.setBinanceMarket(binanceMarket);
        config.setBinanceWsUrl(binanceWsUrl);
        return config;
    }
    private Config() {}
}