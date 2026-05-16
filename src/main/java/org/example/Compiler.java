package org.example;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.io.IOException;

public class Compiler {

    private static final BaseErrorListener THROWING_ERROR_LISTENER = new BaseErrorListener() {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
                                String msg, RecognitionException e) {
            throw new ParseCancellationException("line " + line + ":" + charPositionInLine + " " + msg);
        }
    };

    public String compileFile(String filePath) throws IOException {
        CharStream input = CharStreams.fromFileName(filePath);

        AdaLexer lexer = new AdaLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(THROWING_ERROR_LISTENER);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        AdaParser parser = new AdaParser(tokens);

        parser.removeErrorListeners();
        parser.addErrorListener(THROWING_ERROR_LISTENER);
        parser.setErrorHandler(new BailErrorStrategy());

        ParseTree tree = parser.program();

        AdaToCVisitor visitor = new AdaToCVisitor();
        return visitor.visit(tree);
    }
}
