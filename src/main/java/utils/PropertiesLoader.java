// LightCore Framework
// (c) 2025 Gabriele Difalco
// Licensed under the LightCore License – Limited Source Use
// SPDX-License-Identifier: LicenseRef-LightCore

package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class PropertiesLoader {
    private static final Logger logger = Logger.getLogger(PropertiesLoader.class.getName());

    private static final String CONFIG_PATH = "src/main/resources/lightcoreconfig";
    private static final String CONFIG_FILE = "config.properties";

    private static final Properties properties = new Properties();

    static {
        try(InputStream input = PropertiesLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if(input == null) {
                throw new RuntimeException("Configuration file 'config.properties' not found");
            }
            properties.load(input);
        }
        catch (IOException e) {
            throw new RuntimeException("Error during the loading of the configuration file ", e);
        }
    }

    public static void generateIfMissing() throws IOException{
        File scriptDir = new File(CONFIG_PATH);
        if(!scriptDir.exists()) {
            scriptDir.mkdirs();
        }

        File config = new File(CONFIG_FILE);
        if(!config.exists()) {
            logger.info("Creating config.properties...");
            try(FileWriter fw = new FileWriter(config)) {
                fw.write(LightCoreDocumentationTemplate.getTemplate());
            }
        }
    }

    public static void resetAll() {
        File config = new File(CONFIG_PATH);
        if (config.exists()){
            deleteRecursive(config);
        }

        try {
            generateIfMissing();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteRecursive(File file) {
        if(file.isDirectory()) {
            for(File f : file.listFiles()) {
                deleteRecursive(f);
            }
        }

        file.delete();
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }
    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    public static String getOrDefault(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static int getIntOrDefault(String key, int defaultValue) {
        String value = get(key);
        return (value != null) ? Integer.parseInt(value) : defaultValue;
    }
}