// LightCore Framework
// (c) 2025 Gabriele Difalco
// Licensed under the LightCore License – Limited Source Use
// SPDX-License-Identifier: LicenseRef-LightCore

package database_config;

import node_executor.NodeScriptGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

public class ConfigPropertiesGenerator {
    private static final Logger logger = Logger.getLogger(ConfigPropertiesGenerator.class.getName());

    private static final String CONFIG_DIR = "src/main/resources/lightcoreconfig";
    private static final String CONFIG_PATH = CONFIG_DIR + "/config.properties";

    private static final String CONFIG_CONTENT = """
    #Database Configuration
    db.username=
    db.password=
    db.database=
    db.host=
    db.port=
    
    #Server Configuration
    server.port=
    """;

    public static void generateIfMissing() throws IOException {
        File configDir = new File(CONFIG_DIR);
        if(!configDir.exists()) {
            configDir.mkdirs();
        }

        File configFile = new File(CONFIG_PATH);
        if(!configFile.exists()) {
            logger.info("Creating confi.properties...");
            try (FileWriter fw = new FileWriter(configFile)) {
                fw.write(CONFIG_CONTENT);
            }
        }
    }
}
