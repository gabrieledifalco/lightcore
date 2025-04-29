// LightCore Framework
// (c) 2025 Gabriele Difalco
// Licensed under the LightCore License – Personal Use & Attribution
// SPDX-License-Identifier: LicenseRef-LightCore

package utils;

import node_executor.NodeScriptGenerator;

public class LightCoreCLI {
    // WORK IN PROGRESS

    public static boolean handle(String[] args) {
        if(args.length == 0) return false;

        String cmd = args[0].toLowerCase();

        switch (cmd) {
            case "--doc" -> {
                System.out.println(LightCoreDocumentationTemplate.getTemplate());
                return true;
            }

            case "--help" -> {
                System.out.println("""
                        LightCore CLI – Available Commands:
                        -----------------------------------
                        --doc                     Print framework documentation
                        --help                    Show available commands
                        --version                 Show LightCore version
                        --generate-config         Regenerate config.properties
                        --generate-node-scripts   Regenerate Node.js scripts
                        --reset-config            Delete config.properties
                        --reset-node-scripts      Regenerate node-scripts folder
                        """);
                return true;
            }

            case "--version" -> {
                System.out.println("LightCore version 1.0.0");
                return true;
            }

            case "--generate-config" -> {
                try {
                    PropertiesLoader.generateIfMissing();
                    return true;
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            case "--generate-node-scripts" -> {
                try {
                    System.out.println("Generating Node scripts...");
                    NodeScriptGenerator.generateIfMissing();
                    return true;
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            case "--reset-config" -> {
                System.out.println("Resetting config.properties...");
                PropertiesLoader.resetAll();
                System.out.println("File restored");
                return true;
            }

            case "--reset-node-scripts" -> {
                System.out.println("Resetting Node scripts...");
                NodeScriptGenerator.resetAll();
                System.out.println("Node scripts reset completed");
                return true;
            }

            default -> {
                System.out.println("Unknown command. Use --help for a list of available commands");
                return true;
            }
        }
    }
}
