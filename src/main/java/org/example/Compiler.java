package org.example;

import java.io.IOException;
import java.util.List;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;

public class Compiler {

    private AdaToCVisitor visitor;

    private static final BaseErrorListener THROWING_ERROR_LISTENER = new BaseErrorListener() {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                int line, int charPositionInLine,
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

        visitor = new AdaToCVisitor(); 
        return visitor.visit(tree);
    }


    public List<String> getSemanticErrors() {
        return visitor == null ? List.of() : visitor.getSemanticErrors();
    }

    public boolean hasSemanticErrors() {
        return visitor != null && visitor.hasSemanticErrors();
    }
}