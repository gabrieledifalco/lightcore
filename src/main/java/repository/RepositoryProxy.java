// LightCore Framework
// (c) 2025 Gabriele Difalco
// Licensed under the LightCore License – Limited Source Use
// SPDX-License-Identifier: LicenseRef-LightCore

package repository;

import annotations.nodejs.Param;
import annotations.nodejs.file.FileHandling;
import annotations.nodejs.mysql.Query;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import node_executor.FileRequest;
import node_executor.NodeExecutor;
import node_executor.QueryRequest;

import java.lang.reflect.*;
import java.util.*;
import java.util.logging.Logger;

public class RepositoryProxy {
    private static final Logger logger = Logger.getLogger(RepositoryProxy.class.getName());

    private static ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule())
            .registerModule(new ParameterNamesModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> repositoryInterface) {
        return (T) Proxy.newProxyInstance(
                repositoryInterface.getClassLoader(),
                new Class<?>[]{repositoryInterface},
                (proxy, method, args) -> {

                    Query query = method.getAnnotation(Query.class);
                    if (query != null) {
                        String sql = query.value();

                        logger.info("Executing query: " + sql);
                        logger.info("With parameters: " + Arrays.toString(args));

                        List<Object> queryParams = new ArrayList<>();
                        String finalQuery = replaceQueryParams(method, sql, args, queryParams);

                        QueryRequest queryRequest = new QueryRequest(finalQuery, queryParams);
                        Object rowResult = NodeExecutor.executeQuery(List.of(queryRequest));

                        return mapResult(method.getGenericReturnType(), rowResult);
                    }

                    FileHandling fileHandling = method.getAnnotation(FileHandling.class);
                    if(fileHandling != null) {
                        logger.info("Executing file operation: " + method.getName());
                        logger.info("With parameters: " + Arrays.toString(args));

                        List<FileRequest> fileRequests = convertToFileRequest(method, args);
                        Object result = NodeExecutor.executeFileHandler(fileRequests);

                        return mapFileResult(method.getGenericReturnType(), result);
                    }

                    throw new UnsupportedOperationException("Method not supported: " + method.getName());
                }
        );
    }

    private static String replaceQueryParams(Method method, String sql, Object[] args, List<Object> queryParams) {
        Parameter[] parameters = method.getParameters();

        for(int i = 0 ; i < parameters.length ; i++) {
            if(parameters[i].isAnnotationPresent(Param.class)) {
                Param param = parameters[i].getAnnotation(Param.class);
                String paramName = param.value();
                sql = sql.replaceFirst("\\?" + paramName, "?");
                queryParams.add(args[i]);
            }
            else {
                sql = sql.replaceFirst("\\?", "?");
                queryParams.add(args[i]);
            }
        }

        return sql;
    }

    private static Object mapResult(Type returnType, Object rowResult) {
        if(rowResult == null) {
            return null;
        }

        /*if(rowResult instanceof List<?>) {
            List<?> resultList = (List<?>) rowResult;
            if(!resultList.isEmpty() && resultList.get(0) instanceof Map<?, ?>) {
                Map<?, ?> firstResult = (Map<?, ?>) resultList.get(0);

                if(firstResult.containsKey("error") && firstResult.get("error") != null) {
                    throw new RuntimeException("Query error: " + firstResult.get("error"));
                }
            }
        }*/

        if(returnType == void.class || returnType == Void.class) {
            return null;
        }

        /*if(returnType == int.class || returnType == Integer.class){
            return extractAffectedRows(rowResult, 0);
        }

        if(returnType == boolean.class || returnType == Boolean.class) {
            int affectedRows = extractAffectedRows(rowResult, 0);
            return affectedRows > 0;
        }*/

        if (returnType == QueryExecutionResult.class) {
            return mapToQueryExecutionResult(rowResult);
        }

        /*if(returnType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)  returnType;
            if(pt.getRawType().equals(List.class)) {
                Type itemType = pt.getActualTypeArguments()[0];

                if(rowResult instanceof List<?>) {
                    List<?> resultList = (List<?>) rowResult;

                    return resultList.stream().map(row -> {
                        if(row instanceof Map<?, ?>) {
                            Map<String, Object> processedRow = preprocessRow((Map<?, ?>) row);
                            return objectMapper.convertValue(processedRow, objectMapper.constructType(itemType));
                        }

                        return objectMapper.convertValue(row, objectMapper.constructType(itemType));
                    }).collect(Collectors.toList());
                }

                return new ArrayList<>();
            }
        }*/

        if(rowResult instanceof List<?>) {
            List<?> resultList = (List<?>) rowResult;
            if(!resultList.isEmpty()) {
                Object firstRow = resultList.get(0);

                if(firstRow instanceof Map<?, ?>) {
                    Map<String, Object> processedRow = preprocessRow((Map<?, ?>) firstRow);
                    return objectMapper.convertValue(processedRow, objectMapper.constructType(returnType));
                }
                return objectMapper.convertValue(firstRow, objectMapper.constructType(returnType));
            }

            return null;
        }

        return null;
    }

    /*private static int extractAffectedRows(Object rowResult, int defaultValue) {
        if(rowResult instanceof List<?>) {
            List<?> resultList = (List<?>) rowResult;

            if(!resultList.isEmpty()) {
                Object first = resultList.get(0);

                if(first instanceof Map<?, ?>) {
                    Map<?, ?> map = (Map<?, ?>) first;

                    if(map.containsKey("affectedRows")) {
                        Object value = map.get("affectedRows");

                        if(value instanceof Number) {
                            return ((Number) value).intValue();
                        }
                    }
                }
            }
        }

        return defaultValue;
    }*/

    private static QueryExecutionResult mapToQueryExecutionResult(Object rowResult) {
        if(rowResult instanceof List<?> && !((List<?>) rowResult).isEmpty()){
            Object first = ((List<?>) rowResult).get(0);

            if(first instanceof Map<?, ?>) {
                Map<?, ?> map = (Map<?, ?>) first;

                int affectedRows = map.containsKey("affectedRows") ? ((Number) map.get("affectedRows")).intValue() : 0;
                Long insertId = null;

                if(map.containsKey("insertId") && map.get("insertId") != null) {
                    Object idVal = map.get("insertId");

                    if(idVal instanceof Number){
                        insertId = ((Number) idVal).longValue();
                    }
                    else if(idVal instanceof String) {
                        try {
                            insertId = Long.parseLong((String) idVal);
                        }
                        catch (NumberFormatException e) {
                            insertId = null;
                        }
                    }
                }

                // Safe handling of potentially null values
                String message = map.containsKey("message") && map.get("message") != null ?
                        map.get("message").toString() : null;
                String error = map.containsKey("error") && map.get("error") != null ?
                        map.get("error").toString() : null;
                String type = map.containsKey("type") && map.get("type") != null ?
                        map.get("type").toString() : null;

                if("SELECT".equals(type) && map.containsKey("rows")) {
                    List<Map<String, Object>> rows = new ArrayList<>();
                    Object rowsObj = map.get("rows");

                    if (rowsObj instanceof List<?>) {
                        List<?> rowsList = (List<?>) rowsObj;
                        for (Object row : rowsList) {
                            if (row instanceof Map<?, ?>) {
                                Map<?, ?> rowMap = (Map<?, ?>) row;
                                Map<String, Object> processedRow = new HashMap<>();

                                for (Map.Entry<?, ?> entry : rowMap.entrySet()) {
                                    String key = entry.getKey().toString();
                                    Object value = entry.getValue();
                                    processedRow.put(key, value);
                                }

                                rows.add(processedRow);
                            }
                        }
                    }
                }

                return new QueryExecutionResult(affectedRows, insertId, message, error);
            }
        }

        return new QueryExecutionResult(0, null, "No result", null);
    }

    private static Map<String, Object> preprocessRow(Map<?, ?> row) {
        Map<String, Object> processedRow = new java.util.HashMap<>();

        for (Map.Entry<?, ?> entry : row.entrySet()) {
            String key = entry.getKey().toString();
            Object value = entry.getValue();

            if (value == null) {
                processedRow.put(key, null);
                continue;
            }

            if (value instanceof List<?> && ((List<?>) value).size() > 0 && ((List<?>) value).get(0) instanceof Number) {
                List<?> byteList = (List<?>) value;
                byte[] byteArray = new byte[byteList.size()];
                for (int i = 0; i < byteList.size(); i++) {
                    byteArray[i] = ((Number) byteList.get(i)).byteValue();
                }
                processedRow.put(key, byteArray);
            }
            else if (value instanceof String) {
                String strValue = (String) value;
                if (strValue.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z")) {
                    try {
                        processedRow.put(key, new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                .parse(strValue));
                    } catch (java.text.ParseException e) {
                        processedRow.put(key, strValue);
                    }
                }
                else if (strValue.matches("-?\\d+(\\.\\d+)?")) {
                    if (strValue.contains(".")) {
                        try {
                            processedRow.put(key, new java.math.BigDecimal(strValue));
                        } catch (NumberFormatException e) {
                            processedRow.put(key, strValue);
                        }
                    } else {
                        try {
                            long longValue = Long.parseLong(strValue);
                            processedRow.put(key, longValue);
                        } catch (NumberFormatException e) {
                            try {
                                processedRow.put(key, new java.math.BigInteger(strValue));
                            } catch (NumberFormatException ex) {
                                processedRow.put(key, strValue);
                            }
                        }
                    }
                } else {
                    processedRow.put(key, strValue);
                }
            }
            else {
                processedRow.put(key, value);
            }
        }

        return processedRow;
    }

    public static List<FileRequest> convertToFileRequest(Method method, Object[] args) {
        List<FileRequest> requests = new ArrayList<>();

        if(args != null && args.length > 0) {
            for(Object arg : args) {
                if(arg instanceof  FileRequest) {
                    requests.add((FileRequest) arg);
                }
            }
        }

        return requests;
    }

    public static Object mapFileResult(Type returnType, Object result) {
        if (result == null) {
            return null;
        }

        Map<String, Object> resultMap = objectMapper.convertValue(result, new TypeReference<Map<String, Object>>() {});
        FileExecutionResult fileExecutionResult = new FileExecutionResult();
        fileExecutionResult.setSuccess(Boolean.TRUE.equals(resultMap.get("success")));

        if (resultMap.containsKey("results")) {
            List<Map<String, Object>> rawResults = (List<Map<String, Object>>) resultMap.get("results");
            List<FileExecutionResult.FileOperationResult> operationResults = new ArrayList<>();

            for (Map<String, Object> rawResult : rawResults) {
                FileExecutionResult.FileOperationResult operationResult = new FileExecutionResult.FileOperationResult();
                operationResult.setAction((String) rawResult.get("action"));
                operationResult.setPath((String) rawResult.get("path"));
                operationResult.setResult((Map<String, Object>) rawResult.get("result"));
                operationResult.setError((String) rawResult.get("error"));
                operationResults.add(operationResult);
            }

            fileExecutionResult.setResults(operationResults);
        }

        if (!fileExecutionResult.isSuccess()) {
            String errorMsg = (String) resultMap.getOrDefault("error", "File operation failed");
            throw new RuntimeException(errorMsg);  // Potresti personalizzare questa eccezione
        }

        if (returnType == FileExecutionResult.class) {
            return fileExecutionResult;
        }

        if (returnType == FileExecutionResult.FileOperationResult.class) {
            if (!fileExecutionResult.getResults().isEmpty()) {
                return fileExecutionResult.getResults().get(0);
            }
            return null;
        }

        /*if (returnType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) returnType;
            if (pt.getRawType() == List.class && pt.getActualTypeArguments()[0] == FileExecutionResult.FileOperationResult.class) {
                return fileExecutionResult.getResults();
            }
        }*/

        return objectMapper.convertValue(result, objectMapper.constructType(returnType));
    }
}
