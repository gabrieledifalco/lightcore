// LightCore Framework
// (c) 2025 Gabriele Difalco
// Licensed under the LightCore License – Personal Use & Attribution
// SPDX-License-Identifier: LicenseRef-LightCore

package utils;

public class LightCoreDocumentationTemplate {
    public static String getTemplate() {
        return("""
        LightCore – Lightweight Java Framework for Business Apps
        \s
        LightCore is a modular, lightweight backend framework for Java, with integrated SQL execution via Node.js.
        Designed for developers who want to build fast, without endless configuration.
        \s
        1. First Launch and Auto-generated Environment:
        -----------------------------------------------
            To bootstrap the framework, you need to:
            \s
            1. Annotate your main class with @LightCoreApplication
            2. Use LightCoreApplicationRunner.run() in your main method
            \s
            Example:
                ```java
                \s
                @LightCoreApplication
                public class MyApplication {
                    public static void main(String[] args) {
                        LightCoreApplicationRunner.run(MyApplication.class, args);
                    }
                }
                \s
                ```
            \s
            On first launch, LightCore automatically generates these resources in your resources/ directory:
            \s
            ├── lightcoreconfig/
            │   └── config.properties (contains DB and server configuration)
            │
            └── node-scripts/
                ├──  mysql/
                │     ├── node_modules/
                │     ├── package.json
                │     ├── package-lock.json
                │     └── query-executor.js (Node.js script for query execution)
                └── file-handler/
                    ├── node_modules/
                    ├── package.json
                    ├── package-lock.json
                    └── file-executor.js (Node.js script for file operations)
            \s
            Automatic maintenance features:
                - If node-scripts/ is missing → completely reinstalled
                - If node_modules/ is missing → reinstalled (useful when customizing scripts)
                - If .js scripts are missing → restored to default versions
                - If package.json is missing → regenerated
                - Scripts are automatically executed by the framework (no manual Node.js startup required)
            \s
            The .js scripts are customizable, if you encounter any problems due to the modification, just delete the file
            in question and at the next start it will be regenerated with the default script.
            \s
            The config.properties file is generated with only the keys, both for the DB configuration and for the server
            port configuration. By default the server will start on port 9123, but you can change it at run time (expect
            for DB credentials, which require a restart for security reasons).
            \s
           Important notes:
            - The config.properties contains sensitive DB credentials - keep it secure
            - You can customize the Node.js scripts, but changes will be overwritten if:
                * The file is missing
                * You run in recovery mode
            - The framework handles Node.js process lifecycle automatically
        \s
        2. Communication between Java and Node.js:
        ------------------------------------------
            LightCore communicates with Node.js through ProcessBuilder, passing JSON input via stdin and capturing output
            via stdout. Each Node script acts as a handler for a specific type of task (e.g. query, file).
        \s
        3. Repository Pattern and Query Definition:
        -------------------------------------------
        \s
            All database operations must be defined in repository interfaces (not concrete classes) using the @Query
            annotation with MySQL2-compatible syntax.
            \s
            Basic Rules:
                - Repository must be an interface and must be marked by @Repository annotation
                - Each method must be annotated with @Query
                - Parameters use MySQL2 placeholder syntax (?) of Node.js library
                - Method names should reflect the operation
                \s
                Example Repository:
                    ```java
                    \s
                    @Repository
                    public interface UserRepository {
                        @Query("SELECT * FROM users WHERE fiscalCode = ?")
                        SelectQueryResult findByFiscalCode(@Param("fiscalCode") String fiscalCode);
                        \s
                        @Query("INSERT INTO users (name, email) VALUES (?, ?)")
                        QueryExecutionResult insertUser(@Param("name") String name, @Param("email") String email);
                        \s
                        @Query("UPDATE users SET email = ? WHERE id = ?")
                        QueryExecutionResult updateEmail(@Param("email") String newEmail, @Param("id") Long userId);
                        \s
                        @Query("DELETE FROM users WHERE fiscalCode = ?)
                        QueryExecutionResult deleteUser(@Param("fiscalCode") String fiscalCode);
                        \s
                        @Query("UPDATE users SET username = ? WHERE fiscalCode = ?")
                        QueryExecutionResult updateUser(@Param("username") String username, @Param("fiscalCode") String fiscalCode);
                    }
                    \s
                    ```
                \s
                Parameter Binding:
                    - Use ? for positional parameters
                    - Parameters are bound in method argument order
                    - Each param of the query must be marked with @Param(<"nameOfParam">) annotation
                    - N.B.: all the syntax sugars of Node.js are NOT ALLOWED HERE!!! :(
        \s
        4. Query Execution (query-executor.js) and Return Types:
        --------------------------------------------------------
        \s
            LightCore standardizes the communication between your Java application and a Node.js query execution layer.
            You don't have to worry about raw SQL parsing or manual DB response handling.
            \s
            There are two types of return objects for your repository methods:
            \s
                A. SelectQueryResult:\s
                    * Used exclusively for SELECT queries
                    * Returns a JSON-mapped list of objects
                    * Each row is automatically mapped based on the target table's field names
                    * Example structure:
                    \s
                        [
                            {"id": 1, "name": "John", ...},
                            {"id": 2, "name": "Jane", ...}
                        ]
                \s
                B. QueryExecutionResult:
                    * Used for INSERT, UPDATE and DELETE operations
                    * Standardized response structure:
                    \s
                        {
                            "affectedRows": Number,  // Rows affected by the operation
                            "insertId": Number,     // Auto-generated ID (for INSERTs)
                            "message": String,     // Human-readable status
                            "error": String/null    // Error code if operation failed
                        }
                \s
                * Complete Java class documentation:
                \s
                    Purpose:
                        Represents the outcome of database modification operations
                    \s
                    Fields:
                        - affectedRows (int): Number of modified rows (0 if none)
                        - insertId (Long): Generated ID for INSERTs (null otherwise)
                        - message (String): Descriptive status message
                        - error (String): Error code if operation failed (null if success)
                    \s
                    Common Usage Patterns:
                    \s
                        1. Successful INSERT:
                            ```java
                            \s
                            return new QueryExecutionResult(
                                1,             // 1 row affected
                                42,            // New record ID
                                "User created successfully",
                                null           // No error
                            );
                            \s
                            ```
                        \s
                        2. Failed UPDATE (duplicate):
                            ```java
                            \s
                            return new QueryExecutionResult(
                                0,             // No rows affected
                                null,          // No ID generated
                                "Email already exists",
                                "DUPLICATE_EMAIL"  // Error code
                            );
                            \s
                            ```
                        \s
                        3. Successful DELETE:
                            ```java
                            \s
                            return new QueryExecutionResult(
                                deletedCount,   // Number of deleted rows
                                null,           // No ID relevant for DELETE
                                "Deleted " + deletedCount + " records",
                                null
                            );
                            \s
                            ```
                    \s
                    Best Practices:
                        - For success: Set affectedRows/insertId and descriptive message
                        - For failures: Set error code (consistent strings for logging)
                        - message should be user-friendly
                        - error should be machine-readable for error handling
                        \s
                        JSON Serialization Examples:
                        \s
                        Success Case:
                            ```json
                            \s
                            {
                                "affectedRows": 1,
                                "insertId": 42,
                                "message": "Operation successful",
                                "error": null
                                }
                            \s
                            ```
                        \s
                        Error Case:
                            ```json
                            \s
                            {
                                "affectedRows": 0,
                                "insertId": null,
                                "message": "Validation failed",
                                "error": "INVALID_INPUT"
                            }
                            \s
                            ```
            \s
        5. REST Controller Implementation:
        ----------------------------------
            LightCore provides annotation-based REST controller functionality with automatic
            dependency injection and request handling.
             \s
            Example UserController:
                ```java
                \s
                @RestController
                @RequestMapping("/users")
                public class UserController {
                    @Autowired
                    UserService userService;
                    \s
                    // Controller methods...
                }
                ```
                \s
            Key Annotations:
                - @RestController: Marks class as a REST controller
                - @RequestMapping: Defines base path for all endpoints (/users)
                - @Autowired: Handles dependency injection automatically
            \s
            Endpoint Documentation:
                A. @GetMapping: define a GET request, ex
                    \s
                    ```java
                    \s
                    @GetMapping("/findById")
                    public List<Map<String, Object>> findById(@RequestParam("id") int id)
                    \s
                    ```
                \s
                B. @PostMapping: define a POST request, ex
                    \s
                    ```java
                    \s
                    @PostMapping("/insertUser")
                    public QueryExecutionResult insertUser(@RequestBody UserRequest request)
                    \s
                    ```
                \s
                C. @DeleteMapping: define a DELETE request, ex
                    \s
                    ```java
                    \s
                    @DeleteMapping("/deleteUsers")
                    public QueryExecutionResult deleteUsers(@RequestBody List<Map<String, Object>> request)
                    \s
                    ```
                \s
                D. @PutMapping: define a PUT request, ex
                    \s
                    ```java
                    \s
                    @PutMapping("/updateUser")
                    public QueryExecutionResult updateUser(@RequestBody UserRequest request)
                    \s
                    ```
                \s
                E. @RequestParam: indicates that it is a parameter in the HTTP request url
                \s
                F. @RequestBody: indicate that it is an object in the HTTP request body
                \s
                Best Practices:
                    - Keep controllers focused on HTTP layer only
                    - Delegate business logic to services
                    - Use consistent naming (/users/{id}, /users/search, etc.)
                    - Document required fields for each operation
                    - Validate inputs at controller level
                    - Handle exceptions uniformly
                \s
                Common Response Codes:
                    - 200 OK: Successful GET requests
                    - 201 Created: Successful resource creation
                    - 400 Bad Request: Invalid input data
                    - 404 Not Found: Resource doesn't exist
                    - 500 Internal Server Error: Unexpected errors
        \s
        6. Service and Component Pattern:
        ---------------------------------
        \s
            Classes marked with the @Service annotation indicate that they are classes that handle all the
            business logic, ex
            \s
                ```java
                \s
                @Service
                public class UserService {
                    @Autowired
                    UserRepository userRepository;
                    \s
                    @Autowired
                    PasswordHandler passwordHandler;
                    \s
                    // Business logic
                \s
                ```
            \s
            Classes marked with the @Component annotation indicate that they are classes that handle utility
            operations, ex
            \s
                ```java
                \s
                @Component
                public class PasswordHandler {
                \s
                    // Utility methods
                \s
                ```
            \s
            For now there is no differentiation between the two, except for convention
            
        ----------------------------------------------------------------------------------------------------------------
        License
            This project is released under a custom open license:
            "LightCore License – Personal Use & Attribution".
            You may use this software freely but you are not allowed to modify or redistribute the code.
            See LICENSE.md on GitHub for full terms.
        ----------------------------------------------------------------------------------------------------------------
        """);
    }
}
