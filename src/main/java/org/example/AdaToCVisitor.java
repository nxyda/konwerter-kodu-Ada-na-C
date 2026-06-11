package org.example;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.RuleNode;

public class AdaToCVisitor extends AdaParserBaseVisitor<String> {

    private enum CType {
        INT("int"),
        DOUBLE("double"),
        BOOL("bool");

        private final String cName;

        CType(String cName) {
            this.cName = cName;
        }
    }

    private int indentLevel = 0;

    private boolean hasSemanticError = false;
    private final List<String> semanticErrors = new ArrayList<>();

    private String firstProcedureName = null;

    private final Deque<Map<String, CType>> scopeTypes = new ArrayDeque<>();
    private final Deque<Set<String>> declaredVars = new ArrayDeque<>();

    private final Map<String, Integer> arraySizes = new HashMap<>();
    private final Map<String, Integer> arrayLowerBounds = new HashMap<>();

    private String indent() {
        return "    ".repeat(indentLevel);
    }

    private void semanticError(String message) {
        hasSemanticError = true;
        semanticErrors.add(message);
    }

    private void semanticError(String message, ParserRuleContext ctx) {
        hasSemanticError = true;
        int line = ctx.getStart().getLine();
        semanticErrors.add("Linia " + line + ": " + message);
    }

    public boolean hasSemanticErrors() {
        return hasSemanticError;
    }

    public List<String> getSemanticErrors() {
        return semanticErrors;
    }

    private void pushScope() {
        scopeTypes.push(new HashMap<>());
        declaredVars.push(new HashSet<>());
    }

    private void popScope() {
        scopeTypes.pop();
        declaredVars.pop();
    }

    private CType getType(String name) {
        for (Map<String, CType> scope : scopeTypes) {
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        return null;
    }

    private void setType(String name, CType type) {
        if (scopeTypes.isEmpty()) {
            return;
        }

        scopeTypes.peek().put(name, type);
    }

    private boolean isDeclared(String name) {
        if (declaredVars.isEmpty()) {
            return false;
        }

        return declaredVars.peek().contains(name);
    }

    private void markDeclared(String name) {
        if (!declaredVars.isEmpty()) {
            declaredVars.peek().add(name);
        }
    }

    private String mapAdaTypeToC(String adaType) {
        String t = adaType.toLowerCase();
        return switch (t) {
            case "integer", "natural", "positive" -> CType.INT.cName;
            case "float" -> CType.DOUBLE.cName;
            case "boolean" -> CType.BOOL.cName;
            default -> throw new RuntimeException("Unknown Ada type: '" + adaType + "'");
        };
    }

    private String mapRelOpToC(String adaOp) {
        return switch (adaOp) {
            case "=" -> "==";
            case "/=" -> "!=";
            default -> adaOp;
        };
    }

    private CType unifyNumeric(CType a, CType b) {
        if (a == CType.DOUBLE || b == CType.DOUBLE) {
            return CType.DOUBLE;
        }
        return CType.INT;
    }

    private CType inferType(AdaParser.ExpressionContext ctx) {
        if (ctx == null) return CType.INT;

        if (ctx.term() != null && ctx.expression() == null) {
            return inferType(ctx.term());
        }

        CType left = inferType(ctx.expression());
        CType right = inferType(ctx.term());

        String op = ctx.getChild(1).getText();

        return checkArithmetic(left, right, op, isZeroValue(ctx.term()), ctx);
    }

    private CType inferType(AdaParser.TermContext ctx) {
        if (ctx == null) return CType.INT;

        if (ctx.factor() != null && ctx.term() == null) {
            return inferType(ctx.factor());
        }

        CType left = inferType(ctx.term());
        CType right = inferType(ctx.factor());

        String op = ctx.getChild(1).getText();

        return checkArithmetic(left, right, op, isZeroValue(ctx.factor()), ctx);
    }

    private CType inferType(AdaParser.FactorContext ctx) {
        if (ctx == null) {
            return CType.INT;
        }

        if (ctx.FLOAT() != null) {
            return CType.DOUBLE;
        }

        if (ctx.INTEGER() != null) {
            return CType.INT;
        }

        if (ctx.IDENTIFIER() != null) {
            String name = ctx.IDENTIFIER().getText();

            if (name.equalsIgnoreCase("true") || name.equalsIgnoreCase("false")) {
                return CType.BOOL;
            }

            CType known = getType(name);

            if (known == null) {
                semanticError("Undeclared variable used: " + name, ctx);
                return CType.INT;
            }

            return known;
        }

        if (ctx.array_access() != null) {
            return CType.INT;
        }

        if (ctx.MINUS() != null) {
            return inferType(ctx.factor());
        }

        if (ctx.expression() != null) {
            return inferType(ctx.expression());
        }

        return CType.INT;
    }

    @Override
    public String visitProgram(AdaParser.ProgramContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("#include <stdio.h>\n");
        sb.append("#include <stdbool.h>\n");
        sb.append("#include <stdlib.h>\n\n");

        sb.append(visit(ctx.subprogram_decl()));

        if (firstProcedureName != null && !firstProcedureName.equalsIgnoreCase("Main")) {
            sb.append("\nint main() {\n");
            sb.append("    ").append(firstProcedureName).append("();\n");
            sb.append("    return 0;\n");
            sb.append("}\n");
        }

        return sb.toString();
    }

    @Override
    public String visitProcedure_decl(AdaParser.Procedure_declContext ctx) {
        String procedureName = ctx.IDENTIFIER(0).getText();
        String endName = ctx.IDENTIFIER(1).getText();
        if (!procedureName.equalsIgnoreCase(endName)) {
            semanticError("Nazwa procedury '" + procedureName + "' nie zgadza się z nazwą po 'end': '" + endName + "'", ctx);
        }

        if (firstProcedureName == null) {
            firstProcedureName = procedureName;
        }

        StringBuilder sb = new StringBuilder();

        if (procedureName.equalsIgnoreCase("main")) {
            sb.append("int main() {\n");
        } else {
            sb.append("void ").append(procedureName).append("() {\n");
        }

        pushScope();
        indentLevel++;
        sb.append(visit(ctx.var_decl_section()));
        if (ctx.proc_statement_list() != null) {
            sb.append(visit(ctx.proc_statement_list()));
        }
        if (procedureName.equalsIgnoreCase("main")) {
            sb.append(indent()).append("return 0;\n");
        }
        indentLevel--;
        popScope();
        sb.append("}\n");

        return sb.toString();
    }

    @Override
    public String visitFunction_decl(AdaParser.Function_declContext ctx) {
        String functionName = ctx.IDENTIFIER(0).getText();
        String returnType = mapAdaTypeToC(ctx.IDENTIFIER(1).getText());
        String endName = ctx.IDENTIFIER(2).getText();
        if (!functionName.equalsIgnoreCase(endName)) {
            semanticError("Nazwa funkcji '" + functionName + "' nie zgadza się z nazwą po 'end': '" + endName + "'", ctx);
        }
        StringBuilder sb = new StringBuilder();

        sb.append(returnType).append(" ").append(functionName).append("() {\n");

        pushScope();
        indentLevel++;
        sb.append(visit(ctx.var_decl_section()));

        if (ctx.func_statement_list() == null || 
            ctx.func_statement_list().func_statement() == null || 
            ctx.func_statement_list().func_statement().isEmpty()) {
            
            semanticError("Błąd semantyczny: Funkcja '" + functionName + "' nie może być pusta. Wymagany jest 'return'.", ctx);
        }

        if (ctx.func_statement_list() != null) {
            sb.append(visit(ctx.func_statement_list()));
        }
        
        indentLevel--;
        popScope();
        sb.append("}\n");

        return sb.toString();
    }

    @Override
    public String visitVar_decl_section(AdaParser.Var_decl_sectionContext ctx) {
        return ctx.var_decl().stream()
                .map(this::visit)
                .collect(Collectors.joining());
    }

    @Override
    public String visitVar_decl(AdaParser.Var_declContext ctx) {
        String varName = ctx.IDENTIFIER(0).getText();

        // array declaration: X : array (1..N) of Integer;
        if (ctx.ARRAY() != null) {
            String elemType = ctx.IDENTIFIER(1).getText();
            CType cType = mapAdaTypeToCType(elemType);
            String from = visit(ctx.expression(0));
            String to = visit(ctx.expression(1));
            int lower = 0, upper = 0;
            try {
                lower = Integer.parseInt(from);
                upper = Integer.parseInt(to);
                arraySizes.put(varName, upper);
                arrayLowerBounds.put(varName, lower);
            } catch (NumberFormatException ignored) {}
            setType(varName, cType);
            markDeclared(varName);
            // C array sized upper+1 so Ada 1-based indices fit without offset
            String sizeExpr = upper > 0 ? String.valueOf(upper + 1) : (to + " + 1");
            return indent() + cType.cName + " " + varName + "[" + sizeExpr + "];\n";
        }

        // regular declaration: X : Integer := E;
        String adaType = ctx.IDENTIFIER(1).getText();
        CType cType = mapAdaTypeToCType(adaType);
        setType(varName, cType);
        markDeclared(varName);
        if (!ctx.expression().isEmpty()) {
            return indent() + cType.cName + " " + varName + " = " + visit(ctx.expression(0)) + ";\n";
        }
        return indent() + cType.cName + " " + varName + ";\n";
    }

    private CType mapAdaTypeToCType(String adaType) {
        return switch (adaType.toLowerCase()) {
            case "float" -> CType.DOUBLE;
            case "boolean" -> CType.BOOL;
            case "integer", "natural", "positive" -> CType.INT;
            default -> throw new RuntimeException("Unknown Ada type: '" + adaType + "'");
        };
    }

    @Override
    public String visitProc_statement_list(AdaParser.Proc_statement_listContext ctx) {
        if (ctx == null || ctx.proc_statement() == null) return "";
        return ctx.proc_statement().stream()
                .map(this::visit)
                .filter(res -> res != null)
                .collect(Collectors.joining());
    }

    @Override
    public String visitFunc_statement_list(AdaParser.Func_statement_listContext ctx) {
        if (ctx == null || ctx.func_statement() == null) return "";
        return ctx.func_statement().stream()
                .map(this::visit)
                .filter(res -> res != null)
                .collect(Collectors.joining());
    }

    @Override
    public String visitAssignment(AdaParser.AssignmentContext ctx) {

        if (!isValidLvalue(ctx.lvalue())) {
            semanticError("Invalid lvalue in assignment: " + ctx.lvalue().getText(), ctx);
            return indent() + "/* invalid assignment */;\n";
        }

        String expression = visit(ctx.expression());
        CType rhsType = inferType(ctx.expression());

        if (ctx.lvalue().IDENTIFIER() != null) {
            String name = ctx.lvalue().IDENTIFIER().getText();

            CType lhsType = getType(name);

            if (lhsType != null && rhsType != null && lhsType != rhsType) {
                semanticError("Type mismatch in assignment: cannot assign "
                + rhsType + " to " + lhsType + " for variable " + name, ctx);
            }

            if (!isDeclared(name)) {
                CType inferred = inferType(ctx.expression());
                setType(name, inferred);
                markDeclared(name);
                return indent() + inferred.cName + " " + name + " = " + expression + ";\n";
            }
            return indent() + name + " = " + expression + ";\n";
        }

        String lvalue = visit(ctx.lvalue());
        return indent() + lvalue + " = " + expression + ";\n";
    }

    @Override
    public String visitLvalue(AdaParser.LvalueContext ctx) {
        if (ctx.IDENTIFIER() != null) {
            return ctx.IDENTIFIER().getText();
        }
        return visit(ctx.array_access());
    }

    @Override
    public String visitArray_access(AdaParser.Array_accessContext ctx) {
        String arrayName = ctx.IDENTIFIER().getText();

        for (AdaParser.ExpressionContext expr : ctx.index_list().expression()) {
            checkIndexType(ctx.index_list());
            checkIndexRange(arrayName, expr);
        }

        String indices = visit(ctx.index_list());
        return arrayName + "[" + indices + "]";
    }

    @Override
    public String visitIndex_list(AdaParser.Index_listContext ctx) {
        return ctx.expression().stream()
                .map(this::visit)
                .collect(Collectors.joining("]["));
    }

    @Override
    public String visitIf_statement_proc(AdaParser.If_statement_procContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent()).append("if (").append(visit(ctx.condition())).append(") {\n");
        indentLevel++;
        sb.append(visit(ctx.proc_statement_list()));
        indentLevel--;
        sb.append(indent()).append("}");

        if (ctx.else_part_proc() != null) {
            sb.append(visit(ctx.else_part_proc()));
        } else {
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public String visitElse_part_proc(AdaParser.Else_part_procContext ctx) {
        if (ctx.ELSE() != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(" else {\n");
            indentLevel++;
            sb.append(visit(ctx.proc_statement_list()));
            indentLevel--;
            sb.append(indent()).append("}\n");
            return sb.toString();
        }
        else if (ctx.ELSIF() != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(" else if (").append(visit(ctx.condition())).append(") {\n");
            indentLevel++;
            sb.append(visit(ctx.proc_statement_list()));
            indentLevel--;
            sb.append(indent()).append("}");
            if (ctx.else_part_proc() != null) {
                sb.append(visit(ctx.else_part_proc()));
            } else {
                sb.append("\n");
            }
            return sb.toString();
        }
        return "";
    }

    @Override
    public String visitWhile_statement_proc(AdaParser.While_statement_procContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent()).append("while (").append(visit(ctx.condition())).append(") {\n");
        indentLevel++;
        sb.append(visit(ctx.proc_statement_list()));
        indentLevel--;
        sb.append(indent()).append("}\n");
        return sb.toString();
    }

    @Override
    public String visitFor_statement_proc(AdaParser.For_statement_procContext ctx) {
        String loopVar = ctx.IDENTIFIER().getText();
        String start = visit(ctx.expression(0));
        String end = visit(ctx.expression(1));
        StringBuilder sb = new StringBuilder();

        setType(loopVar, CType.INT);
        markDeclared(loopVar);

        sb.append(indent()).append("for (int ").append(loopVar).append(" = ").append(start).append("; ").append(loopVar).append(" <= ").append(end).append("; ").append(loopVar).append("++) {\n");
        indentLevel++;
        sb.append(visit(ctx.proc_statement_list()));
        indentLevel--;
        sb.append(indent()).append("}\n");
        return sb.toString();
    }

    @Override
    public String visitIf_statement_func(AdaParser.If_statement_funcContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent()).append("if (").append(visit(ctx.condition())).append(") {\n");
        indentLevel++;
        sb.append(visit(ctx.func_statement_list()));
        indentLevel--;
        sb.append(indent()).append("}");

        if (ctx.else_part_func() != null) {
            sb.append(visit(ctx.else_part_func()));
        } else {
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public String visitElse_part_func(AdaParser.Else_part_funcContext ctx) {
        if (ctx.ELSE() != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(" else {\n");
            indentLevel++;
            sb.append(visit(ctx.func_statement_list()));
            indentLevel--;
            sb.append(indent()).append("}\n");
            return sb.toString();
        }
        else if (ctx.ELSIF() != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(" else if (").append(visit(ctx.condition())).append(") {\n");
            indentLevel++;
            sb.append(visit(ctx.func_statement_list()));
            indentLevel--;
            sb.append(indent()).append("}");
            if (ctx.else_part_func() != null) {
                sb.append(visit(ctx.else_part_func()));
            } else {
                sb.append("\n");
            }
            return sb.toString();
        }
        return "";
    }

    @Override
    public String visitWhile_statement_func(AdaParser.While_statement_funcContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent()).append("while (").append(visit(ctx.condition())).append(") {\n");
        indentLevel++;
        sb.append(visit(ctx.func_statement_list()));
        indentLevel--;
        sb.append(indent()).append("}\n");
        return sb.toString();
    }

    @Override
    public String visitFor_statement_func(AdaParser.For_statement_funcContext ctx) {
        String loopVar = ctx.IDENTIFIER().getText();
        String start = visit(ctx.expression(0));
        String end = visit(ctx.expression(1));
        StringBuilder sb = new StringBuilder();

        setType(loopVar, CType.INT);
        markDeclared(loopVar);

        sb.append(indent()).append("for (int ").append(loopVar).append(" = ").append(start).append("; ").append(loopVar).append(" <= ").append(end).append("; ").append(loopVar).append("++) {\n");
        indentLevel++;
        sb.append(visit(ctx.func_statement_list()));
        indentLevel--;
        sb.append(indent()).append("}\n");
        return sb.toString();
    }

    @Override
    public String visitReturn_statement(AdaParser.Return_statementContext ctx) {
        return indent() + "return " + visit(ctx.expression()) + ";\n";
    }

    @Override
    public String visitCondition(AdaParser.ConditionContext ctx) {
        String left = visit(ctx.expression(0));
        String op = mapRelOpToC(ctx.relational_op().getText());
        String right = visit(ctx.expression(1));
        return left + " " + op + " " + right;
    }

    @Override
    public String visitExpression(AdaParser.ExpressionContext ctx) {
        if (ctx.term() != null && ctx.expression() == null) {
            return visit(ctx.term());
        }

        String left = visit(ctx.expression());
        String op = ctx.getChild(1).getText();
        String right = visit(ctx.term());

        CType lType = inferType(ctx.expression());
        CType rType = inferType(ctx.term());


        return left + " " + op + " " + right;
    }

    @Override
    public String visitTerm(AdaParser.TermContext ctx) {
        if (ctx.factor() != null && ctx.term() == null) {
            return visit(ctx.factor());
        }

        String left = visit(ctx.term());
        String op = ctx.getChild(1).getText();
        String right = visit(ctx.factor());

        CType lType = inferType(ctx.term());
        CType rType = inferType(ctx.factor());

        return left + " " + op + " " + right;
    }

    @Override
    public String visitFactor(AdaParser.FactorContext ctx) {
        if (ctx.MINUS() != null) {
            return "-" + visit(ctx.factor());
        }
        if (ctx.LPAREN() != null) {
            return "(" + visit(ctx.expression()) + ")";
        }
        if (ctx.array_access() != null) {
            return visit(ctx.array_access());
        }
        String text = ctx.getText();
        if (text.equalsIgnoreCase("true")) return "true";
        if (text.equalsIgnoreCase("false")) return "false";
        if (ctx.IDENTIFIER() != null && !isDeclared(text)) {
            semanticError("Undeclared variable used: " + text, ctx);
        }
        return text;
    }

    @Override
    protected String defaultResult() {
        return "";
    }

    @Override
    protected String aggregateResult(String aggregate, String nextResult) {
        if (aggregate == null) return nextResult;
        if (nextResult == null) return aggregate;
        return aggregate + nextResult;
    }

    @Override
    public String visitChildren(RuleNode node) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < node.getChildCount(); i++) {
            result.append(visit(node.getChild(i)));
        }
        return result.toString();
    }

    private CType checkArithmetic(CType a, CType b, String op, boolean isRightZero, ParserRuleContext ctx) {
        if (a == null || b == null) return CType.INT;

        if (a == CType.BOOL || b == CType.BOOL) {
            semanticError("Invalid arithmetic operation: " + a + " " + op + " " + b, ctx);
            return CType.INT;
        }
        if ((op.equals("/") || op.equalsIgnoreCase("div") || op.equalsIgnoreCase("mod")) && isRightZero) {
            semanticError("Błąd semantyczny: Wykryto dzielenie przez zero za pomocą operatora '" + op + "'", ctx);
        }

        if (op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/")) {
            if (a == CType.BOOL || b == CType.BOOL) {
                semanticError("Cannot use arithmetic op on BOOL", ctx);
            }
        }

        if (a == CType.DOUBLE || b == CType.DOUBLE) {
            return CType.DOUBLE;
        }

        return CType.INT;
    }

    private boolean isValidLvalue(AdaParser.LvalueContext ctx) {
        if (ctx.IDENTIFIER() != null) {
            return true;
        }
        if (ctx.array_access() != null) {
            return true;
        }
        return false;
    }

    private void checkIndexType(AdaParser.Index_listContext ctx) {
        for (AdaParser.ExpressionContext expr : ctx.expression()) {
            CType type = inferType(expr);

            if (type != CType.INT) {
                semanticError("Niepoprawny typ indeksu tablicy: " + expr.getText()
                        + " (oczekiwano INTEGER, dostano " + type + ")", expr);
            }
        }
    }

    private void checkIndexRange(String arrayName, AdaParser.ExpressionContext expr) {
        Integer size = arraySizes.get(arrayName);
        if (size == null) return;

        CType type = inferType(expr);

        if (type != CType.INT) return;

        try {
            int value = Integer.parseInt(expr.getText());
            int lower = arrayLowerBounds.getOrDefault(arrayName, 0);

            if (value < lower || value > size) {
                semanticError("Indeks poza zakresem tablicy: " + value +
                        " (dozwolony zakres " + lower + ".." + size + ")", expr);
            }

        } catch (NumberFormatException e) {
        }
    }

    private boolean isZeroValue(AdaParser.FactorContext ctx) {
        if (ctx == null) return false;
        
        if (ctx.INTEGER() != null && ctx.INTEGER().getText().equals("0")) {
            return true;
        }
        
        if (ctx.FLOAT() != null) {
            try {
                return Double.parseDouble(ctx.FLOAT().getText()) == 0.0;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        if (ctx.expression() != null) {
            return isZeroValue(ctx.expression());
        }
        
        return false;
    }

    private boolean isZeroValue(AdaParser.ExpressionContext ctx) {
        if (ctx == null) return false;
        if (ctx.term() != null && ctx.expression() == null) {
            return isZeroValue(ctx.term());
        }
        return false;
    }

    private boolean isZeroValue(AdaParser.TermContext ctx) {
        if (ctx == null) return false;
        if (ctx.factor() != null && ctx.term() == null) {
            return isZeroValue(ctx.factor());
        }
        return false;
    }

    @Override
    public String visitPut_line_statement(AdaParser.Put_line_statementContext ctx) {
        String expr = visit(ctx.expression());
        CType type = inferType(ctx.expression());

        String format = switch (type) {
            case INT -> "%d";
            case DOUBLE -> "%f";
            case BOOL -> "%d";
        };

        return indent() + "printf(\"" + format + "\\n\", " + expr + ");\n";
    }
}
