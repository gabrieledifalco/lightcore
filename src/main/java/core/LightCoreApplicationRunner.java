// LightCore Framework
// (c) 2025 Gabriele Difalco
// Licensed under the LightCore License – Personal Use & Attribution
// SPDX-License-Identifier: LicenseRef-LightCore

package core;

import com.sun.net.httpserver.HttpServer;
import component.ComponentModule;
import controller.RestControllerModule;
import database_config.ConfigPropertiesGenerator;
import dependency_injector.DependencyInjector;
import node_executor.NodeScriptGenerator;
import repository.RepositoryModule;
import server.HTTPServerStarter;
import service.ServiceModule;
import utils.LightCoreCLI;
import utils.LightCoreDocumentationTemplate;

import java.lang.reflect.Method;
import java.util.Set;

public class LightCoreApplicationRunner {
    public static void run(Class<?> mainClass, String[] args) {
        try {
            //CLI MODE
            //LightCoreCLI.handle(args);

            System.out.println("""
              _      _ _       _____                  
             | |    (_) |     / ____|                 
             | |     _| | ___| |     ___  _ __  _ __  
             | |    | | |/ _ \\ |    / _ \\| '_ \\| '_ \\ 
             | |____| | |  __/ |___| (_) | | | | | | |
             |______|_|_|\\___|\\_____\\___/|_| |_|_| |_|
                                                      
                :: Powered by LightCore Framework ::
            """);

            String basePackage = mainClass.getPackage().getName();
            DependencyInjector.scanClasses(basePackage);

            DependencyInjector.register(mainClass);
            Object mainClassInstance = DependencyInjector.getInstance(mainClass);
            DependencyInjector.injectDependencies(mainClassInstance);

            RepositoryModule.registerRepositories(basePackage);
            ServiceModule.registerServices(basePackage);
            ComponentModule.registerComponent(basePackage);
            RestControllerModule.registerRoutes(basePackage);

            NodeScriptGenerator.generateIfMissing();
            ConfigPropertiesGenerator.generateIfMissing();

            HTTPServerStarter.start();

            System.out.println("Application started.");
        }
        catch (Exception e) {
            System.err.println("Error while startig the app");
            e.printStackTrace();
        }
    }
}
