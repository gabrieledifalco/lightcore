// LightCore Framework
// (c) 2025 Gabriele Difalco
// Licensed under the LightCore License – Personal Use & Attribution
// SPDX-License-Identifier: LicenseRef-LightCore

package node_executor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import database_config.DatabaseConfig;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class NodeExecutor {
    private static final Logger logger = Logger.getLogger(NodeExecutor.class.getName());

    private static final String SCRIPT_PATH_MYSQL = "src/main/resources/node-scripts/mysql/query-executor.js";
    private static final String SCRIPT_PATH_FILE = "src/main/resources/node-scripts/file-handler/file-executor.js";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Object executeQuery(List<QueryRequest> batch) throws IOException {
        try {
            DatabaseConfig config = new DatabaseConfig();

            File tempJson = File.createTempFile("batch", ".json");
            objectMapper.writeValue(tempJson, batch);

            List<String> command = List.of(
                    "node", SCRIPT_PATH_MYSQL,
                    config.getHost(),
                    String.valueOf(config.getPort()),
                    config.getUsername(),
                    config.getPassword(),
                    config.getDatabase(),
                    tempJson.getAbsolutePath()
            );

            System.out.println("Executing: " + "query-executor.js");

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File("."));
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            StringBuilder jsonOutput = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                jsonOutput.append(line);
            }
            while((line = errorReader.readLine()) != null) {
                errorOutput.append(line);
            }

            int exitCode = process.waitFor();
            if(exitCode != 0) {
                throw new RuntimeException("Node script failed: " + errorOutput.toString());
            }

            String result = jsonOutput.toString().trim();

            if (result.isEmpty()) {
                System.out.println("Node script returned empty output.");
                return List.of(Map.of("affectedRows", 0));
            }

            try {
                List<Map<String, Object>> rowResult = objectMapper.readValue(result, new TypeReference<List<Map<String, Object>>>(){});
                return rowResult;
            }
            catch (Exception e) {
                throw new RuntimeException("Failed to parse Node script output: " + result, e);
            }
        } catch (Exception e) {
            throw new IOException("Failed to execute query-executor.js script", e);
        }
    }

    public static Object executeFileHandler (List<FileRequest> batch) throws IOException{
        try {
            File tempJson = File.createTempFile("file.ops", ".json");
            objectMapper.writeValue(tempJson, batch);

            List<String> command = List.of(
                    "node",
                    SCRIPT_PATH_FILE,
                    tempJson.getAbsolutePath()
            );

            logger.info("Executing: file-executor.js");

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File("."));
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            StringBuilder jsonOutput = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                jsonOutput.append(line);
            }
            while((line = errorReader.readLine()) != null) {
                errorOutput.append(line);
            }

            int exitCode = process.waitFor();
            if(exitCode != 0) {
                throw new RuntimeException("Node script failed: " + errorOutput.toString());
            }

            String result = jsonOutput.toString().trim();

            if (result.isEmpty()) {
                System.out.println("Node script returned empty output.");
                return List.of(Map.of("affectedRows", 0));
            }

            try {
                List<Map<String, Object>> rowResult = objectMapper.readValue(result, new TypeReference<List<Map<String, Object>>>(){});
                return rowResult;
            }
            catch (Exception e) {
                throw new RuntimeException("Failed to parse Node script output: " + result, e);
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Execution interrupted");
        }
        catch (Exception e) {
            throw new IOException("Failed to execute file-executor.js script");
        }
    }
}