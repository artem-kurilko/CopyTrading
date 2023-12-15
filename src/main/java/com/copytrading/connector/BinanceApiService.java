package com.copytrading.connector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;

import static com.copytrading.util.ConfigUtils.getProperty;

public class BinanceApiService {

    public static void main(String[] args) throws IOException {
        String apikey = getProperty("apikey");
        String secretkey = getProperty("secretkey");
        /*Map<String, Object> parameters = new LinkedHashMap<>();

        SpotClient client = new SpotClientImpl(apikey, secretkey);
        String result = client.createWallet().getUserAsset(parameters);
        System.out.println(jsonPrettify(result));*/
        System.out.println(getProperty("fd"));
    }

    private static String jsonPrettify(String json) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement jsonElement = JsonParser.parseString(json);
        String prettyJsonString = gson.toJson(jsonElement);
        return prettyJsonString;
    }
}
