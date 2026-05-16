package org.example;

import org.antlr.v4.runtime.tree.RuleNode;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    private final Deque<Map<String, CType>> scopeTypes = new ArrayDeque<>();
    private final Deque<Set<String>> declaredVars = new ArrayDeque<>();

    private String indent() {
        return "    ".repeat(indentLevel);
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
        if (scopeTypes.isEmpty()) {
            return null;
        }

        return scopeTypes.peek().get(name);
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
            default -> CType.INT.cName;
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
        if (ctx == null) {
            return CType.INT;
        }
        if (ctx.term() != null && ctx.expression() == null) {
            return inferType(ctx.term());
        }
        return unifyNumeric(inferType(ctx.expression()), inferType(ctx.term()));
    }

    private CType inferType(AdaParser.TermContext ctx) {
        if (ctx == null) {
            return CType.INT;
        }
        if (ctx.factor() != null && ctx.term() == null) {
            return inferType(ctx.factor());
        }
        return unifyNumeric(inferType(ctx.term()), inferType(ctx.factor()));
    }

    private CType inferType(AdaParser.FactorContext ctx) {
        if (ctx == null) {
            return CType.INT;
        }
        if (ctx.FLOAT() != null) return CType.DOUBLE;
        if (ctx.INTEGER() != null) return CType.INT;
        if (ctx.IDENTIFIER() != null) {
            CType known = getType(ctx.IDENTIFIER().getText());
            return known != null ? known : CType.INT;
        }
        if (ctx.array_access() != null) {
            return CType.INT;
        }
        if (ctx.MINUS() != null) return inferType(ctx.factor());
        if (ctx.expression() != null) return inferType(ctx.expression());
        return CType.INT;
    }

    @Override
    public String visitProgram(AdaParser.ProgramContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("#include <stdio.h>\n");
        sb.append("#include <stdbool.h>\n\n");

        if (ctx.subprogram_list() != null) {
            sb.append(visit(ctx.subprogram_list()));
        }

        return sb.toString();
    }

    @Override
    public String visitSubprogram_list(AdaParser.Subprogram_listContext ctx) {
        return ctx.subprogram_decl().stream()
                .map(this::visit)
                .collect(Collectors.joining("\n"));
    }

    @Override
    public String visitProcedure_decl(AdaParser.Procedure_declContext ctx) {
        String procedureName = ctx.IDENTIFIER(0).getText();
        StringBuilder sb = new StringBuilder();

        if (procedureName.equalsIgnoreCase("main")) {
            sb.append("int main() {\n");
        } else {
            sb.append("void ").append(procedureName).append("() {\n");
        }

        pushScope();
        indentLevel++;
        if (ctx.declaration_part() != null) {
            sb.append(visit(ctx.declaration_part()));
        }
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
        String returnType = mapAdaTypeToC(ctx.IDENTIFIER(1).getText());
        String functionName = ctx.IDENTIFIER(0).getText();
        StringBuilder sb = new StringBuilder();

        sb.append(returnType).append(" ").append(functionName).append("() {\n");

        pushScope();
        indentLevel++;
        if (ctx.declaration_part() != null) {
            sb.append(visit(ctx.declaration_part()));
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
    public String visitDeclaration_part(AdaParser.Declaration_partContext ctx) {
        if (ctx.declaration_list() != null) {
            return visit(ctx.declaration_list());
        }
        return "";
    }

    @Override
    public String visitDeclaration_list(AdaParser.Declaration_listContext ctx) {
        return ctx.declaration().stream()
                .map(this::visit)
                .collect(Collectors.joining());
    }

    @Override
    public String visitDeclaration(AdaParser.DeclarationContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        String expression = visit(ctx.expression());
        CType inferred = inferType(ctx.expression());
        
        setType(varName, inferred);
        markDeclared(varName);
        return indent() + inferred.cName + " " + varName + " = " + expression + ";\n";
    }

    @Override
    public String visitProc_statement_list(AdaParser.Proc_statement_listContext ctx) {
        return ctx.proc_statement().stream()
                .map(this::visit)
                .collect(Collectors.joining());
    }

    @Override
    public String visitFunc_statement_list(AdaParser.Func_statement_listContext ctx) {
        return ctx.func_statement().stream()
                .map(this::visit)
                .collect(Collectors.joining());
    }

    @Override
    public String visitAssignment(AdaParser.AssignmentContext ctx) {
        String expression = visit(ctx.expression());

        if (ctx.lvalue().IDENTIFIER() != null) {
            String name = ctx.lvalue().IDENTIFIER().getText();
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

        // Loop variable is declared by the for statement.
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
        return visit(ctx.expression()) + " " + ctx.getChild(1).getText() + " " + visit(ctx.term());
    }

    @Override
    public String visitTerm(AdaParser.TermContext ctx) {
        if (ctx.factor() != null && ctx.term() == null) {
            return visit(ctx.factor());
        }
        return visit(ctx.term()) + " " + ctx.getChild(1).getText() + " " + visit(ctx.factor());
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
        return ctx.getText();
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
}
