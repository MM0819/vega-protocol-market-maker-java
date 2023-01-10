package com.vega.protocol.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class AppState {
    private List<Market> markets = new ArrayList<>();
    private List<Order> orders = new ArrayList<>();
    private List<Position> positions = new ArrayList<>();
    private List<Asset> assets = new ArrayList<>();
    private List<Account> accounts = new ArrayList<>();
    private List<ReferencePrice> referencePrices = new ArrayList<>();
}