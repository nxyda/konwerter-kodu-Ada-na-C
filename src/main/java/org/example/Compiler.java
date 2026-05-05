package org.example;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Compiler {

    public static String compile(String code) {

        CharStream input = CharStreams.fromString(code);

        AdaLexer lexer = new AdaLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        AdaParser parser = new AdaParser(tokens);

        ParseTree tree = parser.program();

        AdaToCVisitor visitor = new AdaToCVisitor();

        return visitor.visit(tree);
    }
}
