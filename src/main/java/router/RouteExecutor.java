// LightCore Framework
// (c) 2025 Gabriele Difalco
// Licensed under the LightCore License – Personal Use & Attribution
// SPDX-License-Identifier: LicenseRef-LightCore

package router;

import annotations.web.RequestBody;
import annotations.web.RequestParam;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class RouteExecutor {
    public static void handle(HttpExchange exchange, Object controller, Method method) throws Exception {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        Map<String, String> queryParams = parseQueryParms(exchange);
        String body = new BufferedReader(new InputStreamReader(exchange.getRequestBody())).lines().collect(Collectors.joining("\n"));

        for(int i = 0 ; i < parameters.length ; i++) {
            Parameter param = parameters[i];

            if(param.isAnnotationPresent(RequestParam.class)) {
                String key = param.getAnnotation(RequestParam.class).value();
                String value = queryParams.get(key);
                args[i] = convert(value, param.getType());
            }

            if(param.isAnnotationPresent(RequestBody.class)) {
                Object parsed = new ObjectMapper().readValue(body, param.getType());
                args[i] = parsed;
            }
        }

        Object result = method.invoke(controller, args);

        if(result != null) {
            String json = new ObjectMapper().writeValueAsString(result);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, json.getBytes().length);
            exchange.getResponseBody().write(json.getBytes());
        }
        else {
            exchange.sendResponseHeaders(204, -1);
        }

        exchange.close();
    }

    private static Map<String, String> parseQueryParms(HttpExchange exchange) {
        Map<String, String> queryParams = new HashMap<>();
        String query = exchange.getRequestURI().getQuery();

        if(query != null) {
            String[] pairs = query.split("&");
            for(String pair : pairs) {
                String[] keyVal = pair.split("=");

                if(keyVal.length == 2) {
                    String key = URLDecoder.decode(keyVal[0], StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(keyVal[1], StandardCharsets.UTF_8);
                    queryParams.put(key, value);
                }
            }
        }

        return queryParams;
    }

    private  static Object convert(String value, Class<?> type) {
        if(value == null) return null;
        if(type == String.class) return value;
        if(type == int.class || type == Integer.class) return Integer.parseInt(value);
        if(type == long.class || type == Long.class) return Long.parseLong(value);
        if(type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(value);
        if(type == double.class || type == Double.class) return Double.parseDouble(value);

        return value;
    }
}