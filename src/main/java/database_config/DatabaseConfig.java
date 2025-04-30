// LightCore Framework
// (c) 2025 Gabriele Difalco
// Licensed under the LightCore License – Limited Source Use
// SPDX-License-Identifier: LicenseRef-LightCore

package database_config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {
    private static final String CONFIG_FILE_PATH = "src/main/resources/lightcoreconfig/config.properties";

    private String username;
    private String password;
    private String database;
    private String host;
    private int port;

    public DatabaseConfig() {
        Properties properties = new Properties();

        try(InputStream inputStream = new FileInputStream((CONFIG_FILE_PATH))) {
            properties.load(inputStream);
            this.host = properties.getProperty("db.host");
            this.port = Integer.parseInt(properties.getProperty("db.port"));
            this.username = properties.getProperty("db.username");
            this.password = properties.getProperty("db.password");
            this.database = properties.getProperty("db.database");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabase() {
        return database;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}