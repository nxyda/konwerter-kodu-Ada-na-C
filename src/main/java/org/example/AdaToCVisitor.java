package org.example;

public class AdaToCVisitor extends AdaParserBaseVisitor<String> {

    @Override
    public String visitProcedure_decl(AdaParser.Procedure_declContext ctx) {
        String name = ctx.IDENTIFIER(0).getText();

        StringBuilder body = new StringBuilder();
        if (ctx.proc_statement_list() != null) {
            for (AdaParser.Proc_statementContext st : ctx.proc_statement_list().proc_statement()) {
                body.append(visit(st)).append("\n");
            }
        }

        return "void " + name + "() {\n" + body + "}";
    }

    @Override
    public String visitAssignment(AdaParser.AssignmentContext ctx) {
        return ctx.IDENTIFIER().getText()
                + " = "
                + visit(ctx.expression())
                + ";";
    }

    @Override
    public String visitExpression(AdaParser.ExpressionContext ctx) {
        if (ctx.PLUS() != null)
            return visit(ctx.expression(0)) + " + " + visit(ctx.term());

        if (ctx.MINUS() != null)
            return visit(ctx.expression(0)) + " - " + visit(ctx.term());

        return visit(ctx.term());
    }

    @Override
    public String visitTerm(AdaParser.TermContext ctx) {
        if (ctx.MUL() != null)
            return visit(ctx.term()) + " * " + visit(ctx.factor());

        if (ctx.DIV() != null)
            return visit(ctx.term()) + " / " + visit(ctx.factor());

        return visit(ctx.factor());
    }

    @Override
    public String visitFactor(AdaParser.FactorContext ctx) {
        if (ctx.INTEGER() != null) return ctx.INTEGER().getText();
        if (ctx.FLOAT() != null) return ctx.FLOAT().getText();
        if (ctx.IDENTIFIER() != null) return ctx.IDENTIFIER().getText();

        return "(" + visit(ctx.expression()) + ")";
    }
}
