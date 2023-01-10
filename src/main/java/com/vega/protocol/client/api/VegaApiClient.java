package com.vega.protocol.client.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vega.protocol.exception.ErrorCode;
import com.vega.protocol.exception.TradingException;
import com.vega.protocol.model.*;
import com.vega.protocol.submission.BatchMarketInstruction;
import com.vega.protocol.submission.OrderAmendment;
import com.vega.protocol.submission.OrderCancellation;
import com.vega.protocol.submission.OrderSubmission;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class VegaApiClient {

    private static final int MAX_PAGES = 5;

    private final Config config = Config.getInstance();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Execute HTTP request
     *
     * @param request {@link HttpRequest}
     *
     * @return {@link HttpResponse<String>}
     *
     * @throws Exception thrown if error occurs during HTTP request
     */
    private HttpResponse<String> executeHttpRequest(HttpRequest request) throws Exception {
        return HttpClient.newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Execute a GET request at the given path
     *
     * @param path the GET request path, e.g. /markets
     * @param key the key to extract results from JSON response, e.g. markets
     * @param type the return type, used when unmarshalling JSON response
     *
     * @return list of results
     */
    private <T> List<T> executeGetRequest(String path, String key, Class<T> type) {
        return executeGetRequest(path, key, type, new ArrayList<>(), null);
    }

    /**
     * Execute a GET request at the given path
     *
     * @param path the GET request path, e.g. /markets
     * @param key the key to extract results from JSON response, e.g. markets
     * @param type the return type, used when unmarshalling JSON response
     * @param results the results from the request
     * @param cursor optional cursor for pagination
     *
     * @return list of results
     */
    private <T> List<T> executeGetRequest(String path, String key, Class<T> type, List<T> results, String cursor) {
        try {
            String url = String.format("%s/%s", config.getNodeUrl(), path);
            if(!StringUtils.isEmpty(cursor)) {
                if(url.contains("?")) {
                    url = String.format("%s&pagination.after=%s", url, cursor);
                } else {
                    url = String.format("%s?pagination.after=%s", url, cursor);
                }
            }
            URI uri = URI.create(url);
            HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
            HttpResponse<String> response = executeHttpRequest(request);
            if(response.statusCode() != 200) {
                log.warn("Status code = {}", response.statusCode());
                return Collections.emptyList();
            }
            JSONArray edges = new JSONObject(response.body())
                    .getJSONObject(key).getJSONArray("edges");
            JSONObject pageInfo = new JSONObject(response.body())
                    .getJSONObject(key).getJSONObject("pageInfo");
            for(int i=0; i<edges.length(); i++) {
                JSONObject node = edges.getJSONObject(i).getJSONObject("node");
                results.add(objectMapper.readValue(node.toString(), type));
            }
            if(pageInfo.getBoolean("hasNextPage") && results.size() / 1000 < MAX_PAGES) {
                return executeGetRequest(path, key, type, results, pageInfo.getString("endCursor"));
            }
            return results;
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    /**
     * Submit a batch market instruction
     *
     * @param submissions {@link List<OrderSubmission>}
     * @param cancellations {@link List<OrderCancellation>}
     * @param amendments {@link List<OrderAmendment>}
     *
     * @return optional transaction hash
     */
    public Optional<String> sendBatchMarketInstruction(
            final List<OrderSubmission> submissions,
            final List<OrderCancellation> cancellations,
            final List<OrderAmendment> amendments
    ) {
        try {
            String token = getToken().orElseThrow(() -> new TradingException(ErrorCode.GET_VEGA_TOKEN_FAILED));
            BatchMarketInstruction batchMarketInstruction = new BatchMarketInstruction()
                    .setAmendments(amendments)
                    .setSubmissions(submissions)
                    .setCancellations(cancellations);
            JSONObject payload = new JSONObject()
                    .put("batchMarketInstructions",
                            new JSONObject(objectMapper.writeValueAsString(batchMarketInstruction)))
                    .put("pubKey", config.getPartyId())
                    .put("propagate", true);
            HttpRequest httpRequest = HttpRequest
                    .newBuilder(URI.create(String.format("%s/api/v1/command/sync", config.getWalletUrl())))
                    .header("Authorization", String.format("Bearer %s", token))
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();
            HttpResponse<String> response = executeHttpRequest(httpRequest);
            if(response.statusCode() != 200) {
                log.error(response.toString());
                return Optional.empty();
            }
            String txHash = new JSONObject(response.body()).getString("txHash");
            printErrorIfExists(txHash);
            return Optional.of(txHash);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Print error details
     *
     * @param txHash the transaction hash
     */
    private void printErrorIfExists(final String txHash) {
        printErrorIfExists(txHash, 0);
    }

    /**
     * Print error details
     *
     * @param txHash the transaction hash
     */
    private void printErrorIfExists(final String txHash, final int attempt) {
        try {
            String url = String.format("%s/tx?hash=0x%s", config.getTendermintUrl(), txHash);
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpClient.newBuilder()
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> response = executeHttpRequest(request);
            JSONObject json = new JSONObject(response.body());
            if(json.has("result")) {
                JSONObject txResult = json.getJSONObject("result").getJSONObject("tx_result");
                int code = txResult.getInt("code");
                if (code > 0) {
                    String error = txResult.getString("info");
                    log.error(error);
                }
            } else if(json.has("error")) {
                if(attempt < 10) {
                    Thread.sleep(250);
                    printErrorIfExists(txHash, attempt + 1);
                } else {
                    log.error("Transaction not found: {}", txHash);
                }
            }
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Get markets
     *
     * @return {@link List<Market>}
     */
    public List<Market> getMarkets() {
        return executeGetRequest("markets", "markets", Market.class);
    }

    /**
     * Get assets
     *
     * @return {@link List<Asset>}
     */
    public List<Asset> getAssets() {
        return executeGetRequest("assets", "assets", Asset.class);
    }

    /**
     * Get accounts by party
     *
     * @param partyId the party ID
     *
     * @return {@link List<Account>}
     */
    public List<Account> getAccounts(final String partyId) {
        String path = String.format("accounts?filter.partyIds=%s", partyId);
        return executeGetRequest(path, "accounts", Account.class);
    }

    /**
     * Get open orders by party
     *
     * @param partyId the party ID
     *
     * @return {@link List<Order>}
     */
    public List<Order> getOpenOrders(final String partyId) {
        String path = String.format("orders?partyId=%s&liveOnly=true", partyId);
        return executeGetRequest(path, "orders", Order.class);
    }

    /**
     * Get all orders by party
     *
     * @param partyId the party ID
     *
     * @return {@link List<Order>}
     */
    public List<Order> getOrders(final String partyId) {
        String path = String.format("orders?partyId=%s", partyId);
        return executeGetRequest(path, "orders", Order.class);
    }

    /**
     * Get positions by party
     *
     * @param partyId the party ID
     *
     * @return {@link List<Position>}
     */
    public List<Position> getPositions(final String partyId) {
        String path = String.format("positions?partyId=%s", partyId);
        return executeGetRequest(path, "positions", Position.class);
    }

    /**
     * Get accounts
     *
     * @return {@link List<Account>}
     */
    public List<Account> getAccounts() {
        return executeGetRequest("accounts", "accounts", Account.class);
    }

    /**
     * Get open orders by party
     *
     * @return {@link List<Order>}
     */
    public List<Order> getOpenOrders() {
        return executeGetRequest("orders?liveOnly=true", "orders", Order.class);
    }

    /**
     * Get all orders by party
     *
     * @return {@link List<Order>}
     */
    public List<Order> getOrders() {
        return executeGetRequest("orders", "orders", Order.class);
    }

    /**
     * Get an authorization token from the Vega wallet
     *
     * @return {@link Optional<String>}
     */
    public Optional<String> getToken() {
        try {
            JSONObject body = new JSONObject()
                    .put("wallet", config.getWalletUsername())
                    .put("passphrase", config.getWalletPassword());
            HttpRequest httpRequest = HttpRequest
                    .newBuilder(URI.create(String.format("%s/api/v1/auth/token", config.getWalletUrl())))
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();
            HttpResponse<String> response = executeHttpRequest(httpRequest);
            if(response.statusCode() != 200) {
                log.error(response.toString());
                return Optional.empty();
            }
            return Optional.of(new JSONObject(response.body()).getString("token"));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Optional.empty();
    }
}