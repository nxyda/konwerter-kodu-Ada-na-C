package org.example;

import java.io.IOException;
import java.util.List;

import org.antlr.v4.runtime.misc.ParseCancellationException;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Error: No input file provided.");
            return;
        }

        Compiler compiler = new Compiler();
        boolean hadErrors = false;

    for (String filePath : args) {
        try {
            String output = compiler.compileFile(filePath);

            if (!compiler.getSemanticErrors().isEmpty()) {
                hadErrors = true;

                System.err.println("Semantic errors in file: " + filePath);
                for (String err : compiler.getSemanticErrors()) {
                    System.err.println("  - " + err);
                }

                continue;
            }

            System.out.println("// Source: " + filePath);
            System.out.println(output);

            String fileName = new java.io.File(filePath).getName();
            String outName = fileName.replace(".adb", ".c");

            java.nio.file.Path outPath = java.nio.file.Path.of("out", outName);
            java.nio.file.Files.createDirectories(outPath.getParent());

            java.nio.file.Files.writeString(
                    outPath,
                    "// Source: " + filePath + "\n" + output
            );

        } catch (IOException e) {
            hadErrors = true;
            System.err.println("IO error: " + filePath);
            e.printStackTrace();

        } catch (ParseCancellationException e) {
            hadErrors = true;
            System.err.println("Parse error: " + filePath);
            System.err.println(e.getMessage());
        }
    }

        if (hadErrors) {
            System.exit(2);
        }
    }

}
