package org.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
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

            CodePointCharStream input = CharStreams.fromString(adaCode);
            AdaLexer lexer = new AdaLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            AdaParser parser = new AdaParser(tokens);

            parser.removeErrorListeners();
            
            ParseTree tree = parser.program();

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