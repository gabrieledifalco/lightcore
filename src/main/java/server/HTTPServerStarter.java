// LightCore Framework
// (c) 2025 Gabriele Difalco
// Licensed under the LightCore License – Personal Use & Attribution
// SPDX-License-Identifier: LicenseRef-LightCore

package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import database_config.ConfigPropertiesGenerator;
import router.Router;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.util.Properties;
import java.util.logging.Logger;

public class HTTPServerStarter {
    private static final Logger logger = Logger.getLogger(HTTPServerStarter.class.getName());

    private static final String CONFIG_FILE_PATH = "src/main/resources/lightcoreconfig/config.properties";
    private static final int DEFAULT_SERVER_PORT = 9123;

    private static HttpServer server;
    private static int currentPort = -1;

    public static void start() throws IOException {
        int port = readPortFromConfig();
        startServer(port);
        watchConfigFile();
    }

    private static void startServer(int port) throws IOException {
        if(server != null) {
            logger.info("Stopping server on port " + currentPort);
            server.stop(0);
        }

        server = HttpServer.create(new InetSocketAddress(port), 0);
        currentPort = port;

        server.createContext("/", (HttpExchange exchange) -> {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            Router.Route route = Router.getHandler(method, path);

            if(route == null) {
                String notFound = "404 Not Found";
                exchange.sendResponseHeaders(404, notFound.length());

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFound.getBytes());
                }

                return;
            }

            try {
                Router.executeRoute(method, path, exchange);
            }
            catch (Exception e) {
                e.printStackTrace();
                String error = "500 Internal Server Error";
                exchange.sendResponseHeaders(500, error.length());

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(error.getBytes());
                }
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("HTTP Server started on port: " + port);
    }

    public static void watchConfigFile() {
        Thread watcherThread = new Thread(() -> {
            try {
                Path configPath = Paths.get(CONFIG_FILE_PATH).toAbsolutePath();
                Path dir = configPath.getParent();

                WatchService watchService = FileSystems.getDefault().newWatchService();
                dir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

                while (true) {
                    WatchKey key = watchService.take();
                    for(WatchEvent<?> event : key.pollEvents()) {
                        Path changed = (Path) event.context();

                        if(changed.toString().equals("config.properties")) {
                            Thread.sleep(200);
                            int newPort = readPortFromConfig();

                            if(newPort != currentPort) {
                                logger.info("Detected server port change. Restarting server on new port: " + newPort);
                                startServer(newPort);
                            }
                        }
                    }

                    key.reset();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });

        watcherThread.setDaemon(false);
        watcherThread.start();
    }

    private static int readPortFromConfig() {
        Properties properties = new Properties();
        File file = new File(CONFIG_FILE_PATH);

        if(!file.exists()) {
            logger.info("config.properties not found. Using default port " + DEFAULT_SERVER_PORT);
            return DEFAULT_SERVER_PORT;
        }

        try (InputStream inputStream = new FileInputStream(file)) {
            properties.load(inputStream);
            return Integer.parseInt(properties.getProperty("server.port", String.valueOf(DEFAULT_SERVER_PORT)));
        }
        catch (IOException | NumberFormatException e) {
            logger.info("Error reading port. Using default");
            return DEFAULT_SERVER_PORT;
        }
    }
}