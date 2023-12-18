package com.copytrading.connector;

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.copytrading.connector.model.MarginType;
import lombok.SneakyThrows;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Objects;

import static com.copytrading.util.ConfigUtils.getProperty;

/**
 * Binance service connector.
 */
public class BinanceConnector {

    public static JSONObject placeOrder(LinkedHashMap<String, Object> parameters) {
        String response = futuresClient().account().newOrder(parameters);
        return new JSONObject(response);
    }

    public static JSONObject changeMarginType(String symbol, MarginType type) {
        LinkedHashMap<String,Object> params = new LinkedHashMap<>();
        params.put("symbol", symbol);
        params.put("marginType", type);
        String response = futuresClient().account().changeMarginType(params);
        JSONObject respObject = new JSONObject(response);
        if (Objects.equals(respObject.getString("msg"), "success"))
            return respObject;
        else
            throw new RuntimeException("Exception while changing margin type.");
    }

    public static JSONArray openOrders(LinkedHashMap<String, Object> parameters) {
        String response = futuresClient().account().currentAllOpenOrders(parameters);
        return new JSONArray(response);
    }

    public static JSONArray trades(LinkedHashMap<String, Object> parameters) {
        String response = futuresClient().account().accountTradeList(parameters);
        return new JSONArray(response);
    }

    @SneakyThrows
    private static UMFuturesClientImpl futuresClient() {
        String testnetBaseUrl = "https://testnet.binancefuture.com";
        String testapi = getProperty("testapi");
        String testsecret = getProperty("testsecret");
        return new UMFuturesClientImpl(testapi, testsecret, testnetBaseUrl);
    }

}
