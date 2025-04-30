// LightCore Framework
// (c) 2025 Gabriele Difalco
// Licensed under the LightCore License – Limited Source Use
// SPDX-License-Identifier: LicenseRef-LightCore

package component;

import annotations.service.Service;
import dependency_injector.DependencyInjector;

import java.util.Set;

public class ComponentModule {

    public static void registerComponent(String basePackage) throws Exception {
        Set<Class<?>> scannedClasses = DependencyInjector.scanClasses(basePackage);

        for(Class<?> clazz : scannedClasses) {
            if(clazz.isAnnotationPresent(Service.class)) {
                DependencyInjector.register(clazz);
                System.out.println("Component registered: " + clazz.getName());
            }
        }
    }
}
