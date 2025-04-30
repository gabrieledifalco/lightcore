// LightCore Framework
// (c) 2025 Gabriele Difalco
// Licensed under the LightCore License – Limited Source Use
// SPDX-License-Identifier: LicenseRef-LightCore

package node_executor;

import utils.LightCoreDocumentationTemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

public class NodeScriptGenerator {
    private static final Logger logger = Logger.getLogger(NodeScriptGenerator.class.getName());

    private static final String LIGHTCORE_DOC = "src/main/resources/LightCore_Doc.txt";

    private static final String SCRIPT_DIR = "src/main/resources/node-scripts";
    private static final String SCRIPT_MYSQL_DIR = SCRIPT_DIR + "/mysql";
    private static final String SCRIPT_FILE_DIR = SCRIPT_DIR + "/file-handler";
    private static final String SCRIPT_CUSTOM_DIR = SCRIPT_DIR + "/custom";


    private static final String PACKAGE_JSON_PATH_MYSQL = SCRIPT_MYSQL_DIR + "/package.json";
    private static final String PACKAGE_JSON_PATH_FILE = SCRIPT_FILE_DIR + "/package.json";
    private static final String PACKAGE_JSON_PATH_CUSTOM = SCRIPT_CUSTOM_DIR + "/package.json";

    private static final String SCRIPT_PATH_MYSQL_EXECUTOR = SCRIPT_MYSQL_DIR + "/query-executor.js";
    private static final String SCRIPT_PATH_FILE_EXECUTOR = SCRIPT_FILE_DIR + "/file-executor.js";
    private static final String SCRIPT_PATH_CUSTOM_SCRIPT_EXECUTOR = SCRIPT_CUSTOM_DIR + "/custom-executor.js";


    private static final String SCRIPT_CONTENT_QUERY_EXECUTOR = """
    // This script is automatically generated and fully customizable.
    // Modifications are allowed, but use at your own risk.
    // LightCore will regenerate this file with defaults if deleted.
    
    const fs = require('fs');
    const mysql = require('mysql2/promise');
    \s
    (async function() {
        const [host, port, user, password, database, jsonPath] = process.argv.slice(2);
        \s
        let connection;
        let pool;
        let shouldRollback = false;
        \s
        try {
            const batchQueries = JSON.parse(fs.readFileSync(jsonPath, 'utf8'));
            \s
            pool = mysql.createPool({
                host,
                port: parseInt(port),
                user,
                password,
                database,
                waitForConnections: true,
                connectionLimit: 10,
                queueLimit: 0
            });
            \s
            const needsTransaction = batchQueries.some(q =>
                q.sql.trim().toUpperCase().startsWith('INSERT') ||
                q.sql.trim().toUpperCase().startsWith('UPDATE') ||
                q.sql.trim().toUpperCase().startsWith('DELETE')
            );
            \s
            connection = await pool.getConnection();
            \s
            if (needsTransaction) {
                await connection.beginTransaction();
                shouldRollback = true;
            }
            \s
            const results = [];
            \s
            for (const { sql, params } of batchQueries) {
                try {
                    const [rows] = await connection.execute({ sql, timeout: 1000 }, params);
                    const sqlTrimmed = sql.trim().toUpperCase();
                    const isSelect = sqlTrimmed.startsWith('SELECT');
                    \s
                    if (isSelect) {
                        if (rows.length > 0) {
                            const processedRows = rows.map(row => {
                                const processedRow = {};
                                \s
                                for (const [key, value] of Object.entries(row)) {
                                    if (value instanceof Buffer) {
                                        processedRow[key] = Array.from(value);
                                    } else if (value instanceof Date) {
                                        processedRow[key] = value.toISOString();
                                    } else if (typeof value === 'bigint') {
                                        processedRow[key] = value.toString();
                                    } else if (typeof value === 'number' && !Number.isInteger(value)) {
                                        processedRow[key] = value.toString();
                                    } else {
                                        processedRow[key] = value;
                                    }
                                }
                                \s
                                return processedRow;
                            });
                            \s
                            results.push({
                                type: "SELECT",
                                affectedRows: rows.length,
                                insertId: null,
                                message: rows.length + " rows found",
                                rows: processedRows
                            });
                        } else {
                            results.push({
                                type: "SELECT",
                                affectedRows: 0,
                                insertId: null,
                                message: "No rows found",
                                rows: []
                            });
                        }
                    } else {
                        results.push({
                            type: sqlTrimmed.split(' ')[0],
                            affectedRows: rows.affectedRows ?? 0,
                            insertId: rows.insertId ?? null,
                            message: `${sqlTrimmed.split(' ')[0]} execute successfully`,
                            error: null
                        });
                    }
                } catch (error) {
                    throw {
                        error: error.message,
                        sql: sql,
                        params: params
                    };
                }
            }
            \s
            if (needsTransaction) {
                await connection.commit();
                shouldRollback = false;
            }
            \s
            console.log(JSON.stringify(results));
        } catch (error) {
            if (connection && shouldRollback) {
                try {
                    await connection.rollback();
                } catch (rollbackError) {
                    error.rollbackError = rollbackError.message;
                }
            }
            \s
            console.error(JSON.stringify({
                error: error.error || error.message,
                details: {
                    sql: error.sql,
                    params: error.params,
                    stack: process.env.NODE_ENV === 'development' ? error.stack : undefined
                }
            }));
            \s
            process.exit(1);
        } finally {
            if (connection) {
                connection.release();
            }
            \s
            if (pool) {
                await pool.end();
            }
        }
    })();
    """;

    private static final String SCRIPT_CONTENT_FILE_EXECUTOR = """
    // This script is automatically generated and fully customizable.
    // Modifications are allowed, but use at your own risk.
    // LightCore will regenerate this file with defaults if deleted.
    
    const fs = require('fs').promises;
    const path = require('path');
    
    (async function() {
        const [jsonPath] = process.argv.slice(2);
    
        try {
            const operations = JSON.parse(await fs.readFile(jsonPath, 'utf8'));
            const output = {
                success: true,
                results: []
            };
    
            for (const op of operations) {
                const resultEntry = {
                    action: op.action,
                    path: op.path || op.from,  // Usa 'from' per operazioni move/copy
                    result: null,
                    error: null
                };
    
                try {
                    switch (op.action) {
                        case 'readFile':
                            resultEntry.result = await handleReadFile(op);
                            break;
                        case 'writeFile':
                            resultEntry.result = await handleWriteFile(op);
                            break;
                        case 'deleteFile':
                            resultEntry.result = await handleDeleteFile(op);
                            break;
                        case 'listDir':
                            resultEntry.result = await handleListDir(op);
                            break;
                        case 'createDir':
                            resultEntry.result = await handleCreateDir(op);
                            break;
                        case 'moveFile':
                            resultEntry.result = await handleMoveFile(op);
                            resultEntry.path = op.from;  // Mantieni il path originale
                            break;
                        case 'copyFile':
                            resultEntry.result = await handleCopyFile(op);
                            resultEntry.path = op.from;  // Mantieni il path originale
                            break;
                        case 'fileStats':
                            resultEntry.result = await handleFileStats(op);
                            break;
                        default:
                            throw new Error(`Unsupported operation: ${op.action}`);
                    }
                } catch (error) {
                    output.success = false;
                    resultEntry.error = error.message;
                }
    
                output.results.push(resultEntry);
            }
    
            console.log(JSON.stringify(output));
        } catch (error) {
            console.error(JSON.stringify({
                success: false,
                error: error.message,
                stack: process.env.NODE_ENV === 'development' ? error.stack : undefined
            }));
            process.exit(1);
        }
    })();
    
    async function handleReadFile(op) {
        const content = await fs.readFile(op.path, op.encoding || 'utf8');
        return {
            path: op.path,
            content: op.encoding === 'binary' ? content.toString('base64') : content,
            encoding: op.encoding || 'utf8'
        };
    }
    
    async function handleWriteFile(op) {
        const content = op.encoding === 'binary'
            ? Buffer.from(op.content, 'base64')
            : op.content;
        await fs.writeFile(op.path, content, op.encoding || 'utf8');
        return {
            path: op.path,
            bytesWritten: content.length
        };
    }
    
    async function handleDeleteFile(op) {
        await fs.unlink(op.path);
        return {
            path: op.path,
            deleted: true
        };
    }
    
    async function handleListDir(op) {
        const files = await fs.readdir(op.path);
        return {
            path: op.path,
            files: files,
            count: files.length
        };
    }
    
    async function handleCreateDir(op) {
        await fs.mkdir(op.path, { recursive: op.recursive || false });
        return {
            path: op.path,
            created: true
        };
    }
    
    async function handleMoveFile(op) {
        await fs.rename(op.from, op.to);
        return {
            from: op.from,
            to: op.to,
            moved: true
        };
    }
    
    async function handleCopyFile(op) {
        await fs.copyFile(op.from, op.to);
        return {
            from: op.from,
            to: op.to,
            copied: true
        };
    }
    
    async function handleFileStats(op) {
        const stats = await fs.stat(op.path);
        return {
            path: op.path,
            isFile: stats.isFile(),
            isDirectory: stats.isDirectory(),
            size: stats.size,
            createdAt: stats.birthtime,
            modifiedAt: stats.mtime,
            accessedAt: stats.atime
        };
    }
    """;

    private static final String SCRIPT_CONTENT_CUSTOM_SCRIPT_EXECUTOR = """
        function f() {
            console.log("Hello World!");
        }
    """;


    private static final String PACKAGE_JSON_CONTENT_MYSQL = """
    {
        "name": "lightcore-node",
        "version": "1.0.0",
        "description": "Node script for executing MySQL queries in LightCore",
        "main": "query-executor.js",
        "dependencies": {
            "mysql2": "^3.6.0"
        }
    }
    """;

    private static final String PACKAGE_JSON_CONTENT_FILE = """
    {
        "name": "lightcore-node",
        "version": "1.0.0",
        "description": "Node script for handling files operations in LightCore",
        "main": "file-executor.js",
        "dependencies": {
            "fs-extra": "^11.0.0"
        }
    }
    """;

    private static final String PACKAGE_JSON_CONTENT_CUSTOM = """
    {
        "name": "lightcore-node",
        "version": "1.0.0",
        "description": "Node script for executing custom scripts in LightCore",
        "main": "custom-executor.js",
        "dependencies": {
    
        }
    }
    """;

    public static void generateIfMissing() throws IOException {
        File documentation = new File(LIGHTCORE_DOC);
        if(!documentation.exists()) {
            logger.info("Creating LightCore_Doc.txt...");
            try(FileWriter fw = new FileWriter(documentation)) {
                fw.write(LightCoreDocumentationTemplate.getTemplate());
            }
        }

        // create base directories
        File scriptMysqlDir = generateScriptDir(SCRIPT_MYSQL_DIR);
        File scriptFileDir = generateScriptDir(SCRIPT_FILE_DIR);
        //File scriptCustomDir = generateScriptDir(SCRIPT_CUSTOM_DIR);

        // generate script files
        generateExecutor(SCRIPT_PATH_MYSQL_EXECUTOR, SCRIPT_CONTENT_QUERY_EXECUTOR);
        generateExecutor(SCRIPT_PATH_FILE_EXECUTOR, SCRIPT_CONTENT_FILE_EXECUTOR);
        //generateExecutor(SCRIPT_PATH_CUSTOM_SCRIPT_EXECUTOR, SCRIPT_CONTENT_CUSTOM_SCRIPT_EXECUTOR);

        // generate package.json files
        generatePackageJson(PACKAGE_JSON_PATH_MYSQL, PACKAGE_JSON_CONTENT_MYSQL);
        generatePackageJson(PACKAGE_JSON_PATH_FILE, PACKAGE_JSON_CONTENT_FILE);
        //generatePackageJson(PACKAGE_JSON_PATH_CUSTOM, PACKAGE_JSON_CONTENT_CUSTOM);

        // generate node_modules
        generateNodeModules(SCRIPT_MYSQL_DIR, scriptMysqlDir);
        generateNodeModules(SCRIPT_FILE_DIR, scriptFileDir);
        //generateNodeModules(SCRIPT_CUSTOM_DIR, scriptCustomDir);
    }

    private static File generateScriptDir(String scriptDirPath) {
        File scriptDir = new File(scriptDirPath);
        if(!scriptDir.exists()) {
            scriptDir.mkdirs();
        }

        return scriptDir;
    }

    private static void generateExecutor(String executorPath, String executorContent) throws  IOException {
        File scriptExecutor = new File(executorPath);
        if(!scriptExecutor.exists()) {
            logger.info("Creating script executor.js " + executorPath + "...");
            try(FileWriter fw = new FileWriter(scriptExecutor)) {
                fw.write(executorContent);
            }
        }
    }

    private static void generatePackageJson(String packageJsonPath, String packageJsonContent) throws IOException {
        File packageJsonFile = new File(packageJsonPath);
        if(!packageJsonFile.exists()) {
            logger.info("Creating package.json in " + packageJsonPath + "...");
            try(FileWriter fw = new FileWriter(packageJsonFile)) {
                fw.write(packageJsonContent);
            }
        }
    }

    private static void generateNodeModules(String nodeModulesPath, File scriptDir) {
        File nodeModulesDir = new File(nodeModulesPath, "node_modules");
        if(!nodeModulesDir.exists()) {
            logger.info("node_modules not found in " + scriptDir + ". Starting 'npm install'...");
            try {
                ProcessBuilder pb = new ProcessBuilder("npm", "install");
                pb.directory(scriptDir);
                pb.inheritIO();

                Process process = pb.start();
                int exitCode = process.waitFor();

                if(exitCode != 0) {
                    logger.info("'npm install' finished with status code: " + exitCode);
                }
                else {
                    logger.info("'npm install' completed with success, refresh the project");
                }
            }
            catch (IOException | InterruptedException e) {
                logger.info("Error during the execution of 'npm install' command " + e.getMessage());
            }
        }
    }

    public static void resetAll() {
        File nodeScripts = new File(SCRIPT_DIR);
        if (nodeScripts.exists()){
            deleteRecursive(nodeScripts);
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
}
