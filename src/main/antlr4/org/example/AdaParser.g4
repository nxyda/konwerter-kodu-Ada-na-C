parser grammar AdaParser;

options { tokenVocab=AdaLexer; }

program
    : subprogram_list
    ;

subprogram_list
    : subprogram_decl+
    ;

subprogram_decl
    : procedure_decl
    | function_decl
    ;


procedure_decl
    : PROCEDURE IDENTIFIER IS
        declaration_part?
        BEGIN
            proc_statement_list?
        END IDENTIFIER SEMICOLON
    ;



function_decl
    : FUNCTION IDENTIFIER RETURN IDENTIFIER IS
        declaration_part?
        BEGIN
            func_statement_list?
        END IDENTIFIER SEMICOLON
    ;



declaration_part
    : DECLARE declaration_list BEGIN
    |
    ;

declaration_list
    : declaration+
    ;

declaration
    : IDENTIFIER ASSIGN expression SEMICOLON
    ;


proc_statement_list
    : proc_statement+
    ;

proc_statement
    : assignment
    | if_statement_proc
    | while_statement_proc
    | for_statement_proc
    ;

func_statement_list
    : func_statement+
    ;

func_statement
    : assignment
    | if_statement_func
    | while_statement_func
    | for_statement_func
    | return_statement
    ;

assignment
    : lvalue ASSIGN expression SEMICOLON
    ;

lvalue
    : IDENTIFIER
    | array_access
    ;

array_access
    : IDENTIFIER LPAREN index_list RPAREN
    ;

index_list
    : expression (COMMA expression)*
    ;


if_statement_proc
    : IF condition THEN
        proc_statement_list
        else_part_proc?
    END IF SEMICOLON
    ;

else_part_proc
    : ELSE proc_statement_list
    | ELSIF condition THEN proc_statement_list else_part_proc
    ;


while_statement_proc
    : WHILE condition LOOP
        proc_statement_list
    END LOOP SEMICOLON
    ;

for_statement_proc
    : FOR IDENTIFIER IN expression RANGE expression LOOP
        proc_statement_list
    END LOOP SEMICOLON
    ;

if_statement_func
    : IF condition THEN
        func_statement_list
        else_part_func?
    END IF SEMICOLON
    ;

else_part_func
    : ELSE func_statement_list
    | ELSIF condition THEN
    func_statement_list
    else_part_func
    ;

while_statement_func
    : WHILE condition LOOP
        func_statement_list
    END LOOP SEMICOLON
    ;

for_statement_func
    : FOR IDENTIFIER IN expression RANGE expression LOOP
        func_statement_list
    END LOOP SEMICOLON ;

return_statement
    : RETURN expression
    SEMICOLON
    ;

condition
    : expression relational_op expression
    ;

relational_op
    : EQ | NEQ | LT | GT | LE | GE
    ;

expression
    : expression PLUS term
    | expression MINUS term
    | term
    ;

term
    : term MUL factor
    | term DIV factor
    | factor
    ;

factor
    : MINUS factor
    | IDENTIFIER
    | array_access
    | INTEGER
    | FLOAT
    | LPAREN expression RPAREN
    ;