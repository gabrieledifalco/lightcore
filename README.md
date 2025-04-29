# LightCore – Lightweight Java Framework for Business Apps

LightCore is a modular, lightweight backend framework for Java, with integrated SQL execution via Node.js.  
Designed for developers who want to build fast, without endless configuration.

## 1. First Launch and Auto-generated Environment

To bootstrap the framework, you need to:

1. Annotate your main class with `@LightCoreApplication`
2. Use `LightCoreApplicationRunner.run()` in your main method

```java
@LightCoreApplication
public class MyApplication {
    public static void main(String[] args) {
        LightCoreApplicationRunner.run(MyApplication.class, args);
    }
}
```

On first launch, LightCore automatically generates these resources in your `resources/` directory:

```
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
```

**Automatic maintenance features**:
- If `node-scripts/` is missing → completely reinstalled
- If `node_modules/` is missing → reinstalled (useful when customizing scripts)
- If `.js` scripts are missing → restored to default versions
- If `package.json` is missing → regenerated
- Scripts are automatically executed by the framework (no manual Node.js startup required)

The `.js` scripts are customizable. If you encounter any problems due to the modification, just delete the file in question and at the next start it will be regenerated with the default script.

The `config.properties` file is generated with only the keys, both for the DB configuration and for the server port configuration.  
By default, the server will start on port 9123, but you can change it at runtime (except for DB credentials, which require a restart for security reasons).

> **Important notes**:
> - The config.properties contains sensitive DB credentials – keep it secure
> - You can customize the Node.js scripts, but changes will be overwritten if:
>   - The file is missing
>   - You run in recovery mode
> - The framework handles Node.js process lifecycle automatically

## 2. Communication between Java and Node.js

LightCore communicates with Node.js through `ProcessBuilder`, passing JSON input via `stdin` and capturing output via `stdout`.  
Each Node script acts as a handler for a specific type of task (e.g., query, file).

## 3. Repository Pattern and Query Definition

All database operations must be defined in repository interfaces (not concrete classes) using the `@Query` annotation with MySQL2-compatible syntax.

**Basic Rules**:
- Repository must be an interface and marked by `@Repository`
- Each method must be annotated with `@Query`
- Parameters use MySQL2 placeholder syntax (`?`)
- Method names should reflect the operation

```java
@Repository
public interface UserRepository {
    @Query("SELECT * FROM users WHERE fiscalCode = ?")
    SelectQueryResult findByFiscalCode(@Param("fiscalCode") String fiscalCode);

    @Query("INSERT INTO users (name, email) VALUES (?, ?)")
    QueryExecutionResult insertUser(@Param("name") String name, @Param("email") String email);

    @Query("UPDATE users SET email = ? WHERE id = ?")
    QueryExecutionResult updateEmail(@Param("email") String newEmail, @Param("id") Long userId);

    @Query("DELETE FROM users WHERE fiscalCode = ?")
    QueryExecutionResult deleteUser(@Param("fiscalCode") String fiscalCode);

    @Query("UPDATE users SET username = ? WHERE fiscalCode = ?")
    QueryExecutionResult updateUser(@Param("username") String username, @Param("fiscalCode") String fiscalCode);
}
```

**Parameter Binding**:
- Use `?` for positional parameters
- Parameters are bound in method argument order
- Each parameter must be marked with `@Param("name")`
- *Node.js syntax sugars are not allowed!*

_(continua nel file...)_


## 4. Query Execution (`query-executor.js`) and Return Types

LightCore standardizes the communication between your Java application and a Node.js query execution layer.  
You don't have to worry about raw SQL parsing or manual DB response handling.

There are two types of return objects for your repository methods:

### A. `SelectQueryResult`
- Used exclusively for SELECT queries
- Returns a JSON-mapped list of objects
- Each row is automatically mapped based on the target table's field names

```json
[
    {"id": 1, "name": "John", ...},
    {"id": 2, "name": "Jane", ...}
]
```

### B. `QueryExecutionResult`
- Used for INSERT, UPDATE and DELETE operations
- Standardized response structure:

```json
{
    "affectedRows": 1,
    "insertId": 42,
    "message": "Operation successful",
    "error": null
}
```

**Java Class Fields**:
- `affectedRows` (int): Number of modified rows (0 if none)
- `insertId` (Long): Generated ID for INSERTs (null otherwise)
- `message` (String): Descriptive status message
- `error` (String): Error code if operation failed (null if success)

**Common Usage Patterns**:

```java
return new QueryExecutionResult(
    1,
    42,
    "User created successfully",
    null
);
```

```java
return new QueryExecutionResult(
    0,
    null,
    "Email already exists",
    "DUPLICATE_EMAIL"
);
```

```java
return new QueryExecutionResult(
    deletedCount,
    null,
    "Deleted " + deletedCount + " records",
    null
);
```

---

## 5. REST Controller Implementation

LightCore provides annotation-based REST controller functionality with automatic dependency injection and request handling.

### Example:

```java
@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    UserService userService;

    // Controller methods...
}
```

### Key Annotations:
- `@RestController`: Marks class as a REST controller
- `@RequestMapping`: Defines base path for endpoints
- `@Autowired`: Handles dependency injection

### Endpoint Examples:

```java
@GetMapping("/findById")
public List<Map<String, Object>> findById(@RequestParam("id") int id)
```

```java
@PostMapping("/insertUser")
public QueryExecutionResult insertUser(@RequestBody UserRequest request)
```

```java
@DeleteMapping("/deleteUsers")
public QueryExecutionResult deleteUsers(@RequestBody List<Map<String, Object>> request)
```

```java
@PutMapping("/updateUser")
public QueryExecutionResult updateUser(@RequestBody UserRequest request)
```

### Best Practices:
- Keep controllers focused on HTTP layer
- Delegate logic to services
- Use consistent naming
- Validate inputs and handle exceptions

---

## 6. Service and Component Pattern

Use `@Service` for business logic classes and `@Component` for utility classes.

### Example:

```java
@Service
public class UserService {
    @Autowired
    UserRepository userRepository;
    
    @Autowired
    PasswordHandler passwordHandler;

    // Business logic
}
```

```java
@Component
public class PasswordHandler {
    // Utility methods
}
```

Currently, there's no technical difference between `@Service` and `@Component` beyond convention.
