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

### Tabela Tokenów
| Nazwa tokenu | Regex / Definicja        | Opis                           |
| ------------ | ------------------------ | ------------------------------ |
| PROCEDURE    | `"procedure"`            | deklaracja procedury           |
| IS           | `"is"`                   | początek sekcji deklaracji     |
| BEGIN        | `"begin"`                | początek bloku instrukcji      |
| END          | `"end"`                  | koniec bloku                   |
| IF           | `"if"`                   | instrukcja warunkowa           |
| THEN         | `"then"`                 | część warunku IF               |
| ELSE         | `"else"`                 | alternatywa IF                 |
| ELSIF        | `"elsif"`                | dodatkowy warunek IF           |
| WHILE        | `"while"`                | pętla while                    |
| LOOP         | `"loop"`                 | blok pętli                     |
| FOR          | `"for"`                  | pętla for                      |
| IN           | `"in"`                   | operator zakresu w FOR         |
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
| WHITESPACE   | `[ \t\r\n]+`             | białe znaki (ignorowane)       |

