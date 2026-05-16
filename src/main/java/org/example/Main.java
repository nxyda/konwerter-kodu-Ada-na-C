package org.example;

import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.io.IOException;

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
                System.out.println("// Source: " + filePath);
                System.out.println(output);
            } catch (IOException e) {
                hadErrors = true;
                System.err.println("Error reading or parsing file: " + filePath);
                e.printStackTrace();
            } catch (ParseCancellationException e) {
                hadErrors = true;
                System.err.println("Parse error in file: " + filePath);
                System.err.println(e.getMessage());
            }
        }

        if (hadErrors) {
            System.exit(2);
        }
    }
}
