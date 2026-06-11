package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CompilerController {

    @PostMapping("/compile")
    public Map<String, Object> compileAda(@RequestBody Map<String, String> request) {
        String adaCode = request.get("code");
        Map<String, Object> response = new HashMap<>();

        try {
            if (adaCode == null || adaCode.trim().isEmpty()) {
                response.put("success", false);
                response.put("errors", List.of("Kod źródłowy nie może być pusty."));
                return response;
            }

            List<String> syntaxErrors = new ArrayList<>();
            BaseErrorListener collectingListener = new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                        int line, int charPositionInLine,
                                        String msg, RecognitionException e) {
                    syntaxErrors.add("Błąd składni w linii " + line + ":" + charPositionInLine + " — " + msg);
                }
            };

            CodePointCharStream input = CharStreams.fromString(adaCode);

            AdaLexer lexer = new AdaLexer(input);
            lexer.removeErrorListeners();
            lexer.addErrorListener(collectingListener);

            CommonTokenStream tokens = new CommonTokenStream(lexer);

            AdaParser parser = new AdaParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(collectingListener);

            ParseTree tree = parser.program();

            if (!syntaxErrors.isEmpty()) {
                response.put("success", false);
                response.put("errors", syntaxErrors);
                return response;
            }

            AdaToCVisitor visitor = new AdaToCVisitor();
            String cCode = visitor.visit(tree);

            if (visitor.hasSemanticErrors()) {
                response.put("success", false);
                response.put("errors", visitor.getSemanticErrors());
            } else {
                response.put("success", true);
                response.put("cCode", cCode);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("errors", List.of("Błąd krytyczny kompilatora: " + e.getMessage()));
        }

        return response;
    }
}