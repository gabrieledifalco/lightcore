// LightCore Framework
// (c) 2025 Gabriele Difalco
// Licensed under the LightCore License – Personal Use & Attribution
// SPDX-License-Identifier: LicenseRef-LightCore

package router;

import com.sun.net.httpserver.HttpExchange;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class Router {
    public static class Route {
        public final Object controllerInstace;
        public final Method method;

        public Route(Object controllerInstace, Method method) {
            this.controllerInstace = controllerInstace;
            this.method = method;
        }

        public Object getControllerInstance() {
            return controllerInstace;
        }

        public Method getMethod() {
            return method;
        }
    }

    public static final Map<String, Route> routes = new HashMap<>();

    public static void registerRoute(String httpMethod, String path, Object instance, Method method) {
        String routerKey = httpMethod + ":" + path;
        routes.put(routerKey, new Route(instance, method));
        System.out.println("Route registered: " + routerKey);
    }

    public static Route getHandler(String httpMethod, String path) {
        String routerKey = httpMethod + ":" + path;
        return routes.get(routerKey);
    }

    public static void executeRoute(String httpMethod, String path, HttpExchange exchange) throws Exception {
        Route route = getHandler(httpMethod, path);

        if(route != null) {
            Method method = route.getMethod();
            Object controller = route.getControllerInstance();
            method.setAccessible(true);
            RouteExecutor.handle(exchange, controller, method);
        }
        else {
            String notFound = "404 Not Found";
            exchange.sendResponseHeaders(404, notFound.length());
            try(OutputStream os = exchange.getResponseBody()) {
                os.write(notFound.getBytes());
            }
        }
    }
}