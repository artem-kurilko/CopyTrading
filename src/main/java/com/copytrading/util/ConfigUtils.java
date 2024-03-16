package com.copytrading.util;

import lombok.SneakyThrows;

import java.io.FileReader;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Service config methods and parameters.
 */
public class ConfigUtils {
    public static final String resourcePath = "src/main/resources/application.properties";

    @SneakyThrows
    public static String getProperty(String name) {
        Properties props = new Properties();
        props.load(new FileReader(Paths.get(resourcePath).toFile()));
        return props.getProperty(name);
    }
}
