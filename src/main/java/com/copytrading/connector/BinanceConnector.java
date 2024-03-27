package com.copytrading.connector;

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.copytrading.connector.model.BalanceDto;
import com.copytrading.connector.model.MarginType;
import com.copytrading.connector.model.OrderDto;
import com.copytrading.connector.model.PositionDto;
import com.copytrading.model.BaseAsset;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

import static com.copytrading.connector.config.BinanceConfig.futuresClient;
import static com.copytrading.connector.config.BinanceConfig.testClient;

/**
 * Binance service connector.
 */
public class BinanceConnector {
    private static final Gson gson = new Gson();
    private final UMFuturesClientImpl client;

    public BinanceConnector(boolean isProd) {
        this.client = isProd ? futuresClient() : testClient();
    }

    public OrderDto placeOrder(LinkedHashMap<String, Object> parameters) {
        String response = client.account().newOrder(parameters);
        return gson.fromJson(response, OrderDto.class);
    }

    public String exchangeInfo() {
        String result = client.market().exchangeInfo();
        return new JSONObject(result).toString(2);
    }

    public Double getMarkPrice(String symbol) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        String result = client.market().markPrice(parameters);
        return new JSONObject(result).getDouble("markPrice");
    }

    public HashMap<String, Double> getMarkPrices() {
        HashMap<String, Double> markPrices = new HashMap<>();
        UMFuturesClientImpl publicClient = new UMFuturesClientImpl();
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        JSONArray res = new JSONArray(publicClient.market().markPrice(parameters));
        for (int i = 0; i < res.length(); i++) {
            JSONObject markPrice = res.getJSONObject(i);
            markPrices.put(markPrice.getString("symbol"), markPrice.getDouble("markPrice"));
        }
        return markPrices;
    }

    public List<PositionDto> positionInfo() {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        String result = client.account().positionInformation(parameters);
        return Arrays.stream(gson.fromJson(result, PositionDto[].class)).filter(position -> position.getEntryPrice() != 0).collect(Collectors.toList());
    }

    public PositionDto positionInfo(String symbol) {
        return positionInfo().stream().filter(position -> position.getSymbol().equals(symbol)).findFirst().orElseThrow(() -> new IllegalArgumentException("Method positionInfo, position with symbol " + symbol + " not found."));
    }

    public List<BalanceDto> balance() {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        String response = client.account().futuresAccountBalance(parameters);
        return Arrays.stream(gson.fromJson(response, BalanceDto[].class)).collect(Collectors.toList());
    }

    public BalanceDto balance(BaseAsset symbol) {
        return balance().stream().filter(balance -> balance.getAsset().equals(symbol.name())).findFirst().orElseThrow(() -> new IllegalArgumentException("Get balance, symbol not found " + symbol));
    }

    /**
     * Gets collateral balance, for example from symbol BTCUSDT it gives USDT balance, from ETHBUSD -> BUSD
     * @param symbol currency pair
     * @return {@link BalanceDto} instance
     */
    public BalanceDto getCollateralBalanceOfSymbol(String symbol) {
        List<BalanceDto> balanceList = balance();
        for (int i = 0; i < symbol.length(); i++) {
            int finalI = i;
            Optional<BalanceDto> balanceDto = balanceList.stream()
                    .filter(balance -> balance.getAsset().equals(symbol.substring(finalI))).findFirst();
            if (balanceDto.isPresent())  {
                return balanceDto.get();
            }
        }
        throw new IllegalArgumentException("Exception in getCollateralBalanceOfSymbol. Not found balance for symbol " + symbol);
    }

    public OrderDto getOrder(String symbol, String orderId) {
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put("symbol", symbol);
        params.put("orderId", orderId);
        String response = client.account().queryOrder(params);
        return gson.fromJson(response, OrderDto.class);
    }

    public String setLeverage(String symbol, int leverage) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        parameters.put("leverage", leverage);
        String response = client.account().changeInitialLeverage(parameters);
        return new JSONObject(response).toString(2);
    }

    public int getLeverage(String symbol) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        String response = client.account().positionInformation(parameters);
        return new JSONArray(response).getJSONObject(0).getInt("leverage");
    }

    public OrderDto cancelOrder(String symbol, String orderId) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        parameters.put("orderId", orderId);
        String result = client.account().cancelOrder(parameters);
        return gson.fromJson(result, OrderDto.class);
    }

    public String cancelAll(String symbol) {
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put("symbol", symbol);
        String result = client.account().cancelAllOpenOrders(params);
        return new JSONObject(result).toString(2);
    }

    public List<OrderDto> openOrders() {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        String result = client.account().currentAllOpenOrders(parameters);
        return Arrays.asList(gson.fromJson(result, OrderDto[].class));
    }

    public List<OrderDto> allOrders(String symbol) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        String result = client.account().allOrders(parameters);
        return Arrays.asList(gson.fromJson(result, OrderDto[].class));
    }

    public String changeMarginType(String symbol, MarginType type) {
        LinkedHashMap<String,Object> params = new LinkedHashMap<>();
        params.put("symbol", symbol);
        params.put("marginType", type);
        String response = client.account().changeMarginType(params);
        JSONObject respObject = new JSONObject(response);
        if (Objects.equals(respObject.getString("msg"), "success"))
            return respObject.toString(2);
        else
            throw new RuntimeException("Exception while changing margin type.");
    }

    public Double getMinNotionalValue(String symbol) {
        JSONArray symbols = new JSONObject(exchangeInfo()).getJSONArray("symbols");
        for (int i = 0; i < symbols.length(); i++) {
            JSONObject symbolInfo = symbols.getJSONObject(i);
            if (symbolInfo.getString("symbol").equals(symbol)) {
                JSONArray filters = symbolInfo.getJSONArray("filters");
                for (int n = 0; n < filters.length(); n++) {
                    JSONObject filter = filters.getJSONObject(n);
                    if (filter.getString("filterType").equals("MIN_NOTIONAL")) {
                        return filter.getDouble("notional");
                    }
                }
            }
        }
        throw new IllegalArgumentException("Exception in getMinNotionalValue. Not found notional value for symbol " + symbol);
    }

}
