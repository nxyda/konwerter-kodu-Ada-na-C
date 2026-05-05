package org.example;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Main {

    public static void main(String[] args) {

        String code =
                "procedure Test is\n" +
                        "begin\n" +
                        "   x := 10;\n" +
                        "end Test;";

        CharStream input = CharStreams.fromString(code);

        AdaLexer lexer = new AdaLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        AdaParser parser = new AdaParser(tokens);

        ParseTree tree = parser.program();

        System.out.println(tree.toStringTree(parser));
    }
}