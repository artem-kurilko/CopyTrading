package com.copytrading.util;

import org.json.JSONObject;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

/**
 * Service config methods and parameters.
 */
public class ConfigUtils {
    public static final int PARSE_POSITIONS_DELAY = 5000; // time in milliseconds
    public static final String resourcePath = "src/main/resources/application.properties";

    public static String getProperty(String name) throws IOException {
        Properties props = new Properties();
        props.load(new FileReader(Paths.get(resourcePath).toFile()));
        return props.getProperty(name);
    }

    public static Map<String, String> getHeaders() {
        Map<String, String> headers = new Hashtable<>();
        headers.put("Accept", "*/*");
        headers.put("Accept-Language", "en-GB,en;q=0.9,en-US;q=0.8,fr;q=0.7,ar;q=0.6,es;q=0.5,de;q=0.4");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Connection", "keep-alive");
        headers.put("DNT", "1");
        headers.put("Upgrade-Insecure-Requests", "1");
        headers.put("Sec-Fetch-Dest", "empty");
        headers.put("Sec-Fetch-Mode", "cors");
        headers.put("Sec-Fetch-Site", "same-site");
        headers.put("Content-Type", "application/json; charset=utf-8");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:120.0) Gecko/20100101 Firefox/120.0");
        return headers;
    }

}
