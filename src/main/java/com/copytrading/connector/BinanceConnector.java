package com.copytrading.connector;

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.copytrading.connector.model.BalanceDto;
import com.copytrading.connector.model.MarginType;
import com.copytrading.connector.model.OrderDto;
import com.copytrading.connector.model.PositionDto;
import com.google.gson.Gson;
import lombok.SneakyThrows;
import okhttp3.*;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Binance service connector.
 */
public class BinanceConnector {
    private static final Gson gson = new Gson();
    private final UMFuturesClientImpl client;

    public BinanceConnector(UMFuturesClientImpl client) {
        this.client = client;
    }

    public OrderDto placeOrder(LinkedHashMap<String, Object> parameters) {
        String response = client.account().newOrder(parameters);
        return gson.fromJson(response, OrderDto.class);
    }

    public String exchangeInfo() {
        String result = client.market().exchangeInfo();
        return new JSONObject(result).toString(2);
    }

    public List<PositionDto> positionInfo() {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        String result = client.account().positionInformation(parameters);
        return Arrays.stream(gson.fromJson(result, PositionDto[].class)).filter(position -> position.getEntryPrice() != 0).collect(Collectors.toList());
    }

    @SneakyThrows
    public List<PositionDto> positionInfoV2() {
        String api = client.getApiKey();
        String url = "https://fapi.binance.com/fapi/v2/positionRisk";
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
        addTimestampAndRecvWindow(urlBuilder);
        signRequest(urlBuilder);

        Request request = new Request.Builder().url(urlBuilder.build().toString())
                .addHeader("X-MBX-APIKEY", api).build();
        OkHttpClient clientd = new OkHttpClient().newBuilder().build();
        Call call = clientd.newCall(request);
        Response response = call.execute();
        String result = response.body().string();
        response.close();
        try {
            return Arrays
                    .stream(gson.fromJson(result, PositionDto[].class))
                    .filter(position -> position.getEntryPrice() != 0)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.out.println();
            System.out.println("EXCEPTION: " + result);
            throw e;
        }
    }

//    private final long timestamp = System.currentTimeMillis();

    @SneakyThrows
    private void addTimestampAndRecvWindow(HttpUrl.Builder urlBuilder){
        long recvWindow = 20000;
        long timestamp = System.currentTimeMillis();

        /*
        OkHttpClient clientd = new OkHttpClient().newBuilder().build();
        String url = "https://fapi.binance.com/fapi/v1/time";
        Request request = new Request.Builder().url(url).build();
        Call call = clientd.newCall(request);
        Response response = call.execute();
        String responseBody = response.body().string();
        try {
            timestamp = new JSONObject(responseBody).getLong("serverTime");
        } catch (Exception e) {
            System.out.println("RESPONSE BODY EXCEPTION: " + responseBody);
            throw e;
        }
        response.close();
        */
        urlBuilder.addQueryParameter("recvWindow", String.valueOf(recvWindow));
        urlBuilder.addQueryParameter("timestamp", String.valueOf(timestamp));
    }

    @SneakyThrows
    private void signRequest(HttpUrl.Builder urlBuilder){
        String secret = client.getSecretKey();
        String url = urlBuilder.build().toString();
        urlBuilder.addQueryParameter("signature", sign(getQueryParameters(url), secret));
    }

    private String getQueryParameters(String url){
        String[] queryParts = url.split("\\?");
        return queryParts[1];
    }

    public String sign(String message, String secret) {
        try{
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secretKeySpec);
            return new String(Hex.encodeHex(sha256_HMAC.doFinal(message.getBytes())));
        } catch (Exception e) {
            throw new RuntimeException("Unable to sign message.", e);
        }
    }

    public PositionDto positionInfo(String symbol) {
        return positionInfo().stream().filter(position -> position.getSymbol().equals(symbol)).findFirst().orElseThrow(() -> new IllegalArgumentException("Method positionInfo, position with symbol " + symbol + " not found."));
    }

    public List<BalanceDto> balance() {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        String response = client.account().futuresAccountBalance(parameters);
        return Arrays.stream(gson.fromJson(response, BalanceDto[].class)).collect(Collectors.toList());
    }

    public BalanceDto balance(String symbol) {
        return balance().stream().filter(balance -> balance.getAsset().equals(symbol)).findFirst().orElseThrow(() -> new IllegalArgumentException("Get balance, symbol not found " + symbol));
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
