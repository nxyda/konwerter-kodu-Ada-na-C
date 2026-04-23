# Konwerter kodu Ada -> C
## Zespół
1. Natalia Poźniak – [npozniak@student.agh.edu.pl]
2. Małgorzata Poskróbek – [mposkrobek@student.agh.edu.pl]

## Założenia programu

### Ogólne cele programu
Program ma za zadanie przekonwertowanie kodu napisanego w języku Ada do kodu w języku C.

### Rodzaj translatora
Transpiler

### Planowany wynik działania programu
Kod w języku C

### Planowany język implementacji
Java

### Generator parsera
ANTLR4

## Tabela Tokenów
| Nazwa tokenu | Regex / Definicja        | Opis                           |
| ------------ | ------------------------ | ------------------------------ |
| PROCEDURE    | `"procedure"`            | deklaracja procedury           |
| FUNCTION     | `"function"`             | deklaracja funkcji             |
| IS           | `"is"`                   | początek sekcji deklaracji     |
| BEGIN        | `"begin"`                | początek bloku instrukcji      |
| END          | `"end"`                  | koniec bloku                   |
| IF           | `"if"`                   | instrukcja warunkowa           |
| THEN         | `"then"`                 | część warunku if               |
| ELSE         | `"else"`                 | alternatywa if                 |
| ELSIF        | `"elsif"`                | dodatkowy warunek if           |
| WHILE        | `"while"`                | pętla while                    |
| LOOP         | `"loop"`                 | blok pętli                     |
| FOR          | `"for"`                  | pętla for                      |
| IN           | `"in"`                   | operator zakresu w for         |
| RETURN       | `"return"`               | zwrot wartości                 |
| DECLARE      | `"declare"`              | sekcja deklaracji lokalnych    |
| ASSIGN       | `":="`                   | przypisanie wartości           |
| RANGE        | `"\.\."`                 | operator zakresu (np. 1 .. 10) |
| PLUS         | `"+"`                    | dodawanie                      |
| MINUS        | `"-"`                    | odejmowanie                    |
| MUL          | `"*"`                    | mnożenie                       |
| DIV          | `"/"`                    | dzielenie                      |
| EQ           | `"="`                    | równość                        |
| NEQ          | `"/="`                   | nierówność                     |
| LT           | `"<"`                    | mniejsze                       |
| GT           | `">"`                    | większe                        |
| LE           | `"<="`                   | mniejsze lub równe             |
| GE           | `">="`                   | większe lub równe              |
| SEMICOLON    | `";"`                    | koniec instrukcji              |
| COMMA        | `","`                    | separator                      |
| LPAREN       | `"("`                    | nawias otwierający             |
| RPAREN       | `")"`                    | nawias zamykający              |
| IDENTIFIER   | `[a-zA-Z_][a-zA-Z0-9_]*` | nazwa zmiennej lub procedury   |
| INTEGER      | `[0-9]+`                 | liczba całkowita               |
| FLOAT        | `[0-9]+\.[0-9]+`         | liczba zmiennoprzecinkowa      |
| COMMENT      | `"--" ~[\r\n]*`          | komentarz jednoliniowy         |
| WHITESPACE   | `[ \t\r\n]+`             | białe znaki                    |



## Gramatyka
```bnf
program ::= subprogram_list


subprogram_list ::=
    subprogram_decl
    | subprogram_list subprogram_decl


subprogram_decl ::=
    procedure_decl
    | function_decl

procedure_decl ::=
    "procedure" IDENTIFIER "is"
        declaration_part
    "begin"
        proc_statement_list
    "end" IDENTIFIER ";"


function_decl ::=
    "function" IDENTIFIER "return" IDENTIFIER "is"
        declaration_part
    "begin"
        func_statement_list
    "end" IDENTIFIER ";"


declaration_part ::=
    "declare" declaration_list "begin"
    | ε


declaration_list ::=
    declaration
    | declaration_list declaration


declaration ::=
    IDENTIFIER ":=" expression ";"


proc_statement_list ::=
    proc_statement
    | proc_statement_list proc_statement


proc_statement ::=
    assignment
    | if_statement_proc
    | while_statement_proc
    | for_statement_proc


func_statement_list ::=
    func_statement
    | func_statement_list func_statement


func_statement ::=
    assignment
    | if_statement_func
    | while_statement_func
    | for_statement_func
    | return_statement


assignment ::=
    lvalue ":=" expression ";"


lvalue ::=
    IDENTIFIER
    | array_access


array_access ::=
    IDENTIFIER "(" index_list ")"


index_list ::=
    expression
    | index_list "," expression


if_statement_proc ::=
    "if" condition "then"
        proc_statement_list
    else_part_proc
    "end" "if" ";"


else_part_proc ::=
    "else" proc_statement_list
    | "elsif" condition "then" proc_statement_list else_part_proc
    | ε


if_statement_func ::=
    "if" condition "then"
        func_statement_list
    else_part_func
    "end" "if" ";"


else_part_func ::=
    "else" func_statement_list
    | "elsif" condition "then" func_statement_list else_part_func
    | ε


while_statement_proc ::=
    "while" condition "loop"
        proc_statement_list
    "end" "loop" ";"


while_statement_func ::=
    "while" condition "loop"
        func_statement_list
    "end" "loop" ";"


for_statement_proc ::=
    "for" IDENTIFIER "in" expression RANGE expression "loop"
        proc_statement_list
    "end" "loop" ";"


for_statement_func ::=
    "for" IDENTIFIER "in" expression RANGE expression "loop"
        func_statement_list
    "end" "loop" ";"


return_statement ::=
    "return" expression ";"


condition ::=
    expression relational_op expression


relational_op ::=
    "=" | "/=" | "<" | ">" | "<=" | ">="


expression ::=
    term
    | expression "+" term
    | expression "-" term


term ::=
    factor
    | term "*" factor
    | term "/" factor


factor ::=
    "-" factor
    | IDENTIFIER
    | array_access
    | INTEGER
    | FLOAT
    | "(" expression ")"


RANGE ::= ".."

ε ::= (empty)