package com.copytrading.util;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Service config methods and parameters.
 */
public class ConfigUtils {
    public static final String resourcePath = "src/main/resources/application.properties";

    public static String getProperty(String name) throws IOException {
        Properties props = new Properties();
        props.load(new FileReader(Paths.get(resourcePath).toFile()));
        return props.getProperty(name);
    }
}
