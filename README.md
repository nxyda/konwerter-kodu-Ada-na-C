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

## Tabela Tokenów
| Nazwa tokenu | Regex / Definicja        | Opis                           |
| ------------ | ------------------------ | ------------------------------ |
| PROCEDURE    | `"procedure"`            | deklaracja procedury           |
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
| COMMENT      | `"--".*`                 | komentarz jednoliniowy         |
| WHITESPACE   | `[ \t\r\n]+`             | białe znaki                    |

## Gramatyka
```bnf
program ::= procedure_decl


procedure_decl ::=
    "procedure" IDENTIFIER "is"
        declaration_part
    "begin"
        statement_list
    "end" IDENTIFIER ";"


declaration_part ::=
    "declare" declaration_list "begin"
    | ε


declaration_list ::=
    declaration
    | declaration_list declaration


declaration ::=
    IDENTIFIER ":=" expression ";"


statement_list ::=
    statement
    | statement_list statement


statement ::=
    assignment
    | if_statement
    | while_statement
    | for_statement
    | return_statement


assignment ::=
    IDENTIFIER ":=" expression ";"


if_statement ::=
    "if" condition "then"
        statement_list
    else_part
    "end" "if" ";"


else_part ::=
    "else" statement_list
    | "elsif" condition "then" statement_list else_part
    | ε


while_statement ::=
    "while" condition "loop"
        statement_list
    "end" "loop" ";"


for_statement ::=
    "for" IDENTIFIER "in" expression RANGE expression "loop"
        statement_list
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
    IDENTIFIER
    | INTEGER
    | FLOAT
    | "(" expression ")"

RANGE ::= ".."

ε ::= (empty)
```

