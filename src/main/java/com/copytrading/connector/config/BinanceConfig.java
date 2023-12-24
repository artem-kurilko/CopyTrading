package com.copytrading.connector.config;

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import lombok.SneakyThrows;

import static com.copytrading.util.ConfigUtils.getProperty;

public class BinanceConfig {

    @SneakyThrows
    public static UMFuturesClientImpl futuresClient() {
        String api = getProperty("api");
        String secret = getProperty("secret");
        return new UMFuturesClientImpl(api, secret);
    }

    @SneakyThrows
    public static UMFuturesClientImpl testClient() {
        String testnetBaseUrl = "https://testnet.binancefuture.com";
        String testapi = getProperty("testapi");
        String testsecret = getProperty("testsecret");
        return new UMFuturesClientImpl(testapi, testsecret, testnetBaseUrl);
    }

}
