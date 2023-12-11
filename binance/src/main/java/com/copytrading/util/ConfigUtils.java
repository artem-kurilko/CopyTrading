package com.copytrading.util;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigUtils {
    private static final String RESOURCES_PATH = "binance/src/main/resources/application.properties";

    public static String getProperty(String name) throws IOException {
        Properties props = new Properties();
        props.load(new FileReader(getPath().toFile()));
        return props.getProperty(name);
    }

    private static Path getPath() {
        return Paths.get(RESOURCES_PATH);
    }
}
