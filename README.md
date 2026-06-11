# Konwerter kodu Ada -> C

## Zespół

1. Natalia Poźniak – [npozniak@student.agh.edu.pl]
2. Małgorzata Poskróbek – [mposkrobek@student.agh.edu.pl]

## Założenia programu

### Ogólne cele programu

Program ma za zadanie przekonwertowanie kodu napisanego w języku Ada do kodu w języku C.
Zgodnie z zasadami języka Ada każdy plik `.adb` zawiera dokładnie jedną jednostkę kompilacji – jedną procedurę lub jedną funkcję.

### Rodzaj translatora

Konwerter

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
| ARRAY        | `"array"`                | deklaracja tablicy             |
| OF           | `"of"`                   | typ elementów tablicy          |
| ASSIGN       | `":="`                   | przypisanie wartości           |
| COLON        | `":"`                    | separator nazwy i typu         |
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
| PUT_LINE     | `"Put_Line"`             | wypisanie wartości na wyjście  |
| IDENTIFIER   | `[a-zA-Z_][a-zA-Z0-9_]*` | nazwa zmiennej lub procedury   |
| INTEGER      | `[0-9]+`                 | liczba całkowita               |
| FLOAT        | `[0-9]+\.[0-9]+`         | liczba zmiennoprzecinkowa      |
| COMMENT      | `"--" ~[\r\n]*`          | komentarz jednoliniowy         |
| WHITESPACE   | `[ \t\r\n]+`             | białe znaki                    |

## Gramatyka

```bnf
program ::= subprogram_decl


subprogram_decl ::=
    procedure_decl
    | function_decl

procedure_decl ::=
    "procedure" IDENTIFIER "is"
        var_decl_section
    "begin"
        proc_statement_list
    "end" IDENTIFIER ";"


function_decl ::=
    "function" IDENTIFIER "return" IDENTIFIER "is"
        var_decl_section
    "begin"
        func_statement_list
    "end" IDENTIFIER ";"


var_decl_section ::=
    var_decl*

var_decl ::=
    IDENTIFIER ":" IDENTIFIER [ ":=" expression ] ";"
    | IDENTIFIER ":" "array" "(" expression ".." expression ")" "of" IDENTIFIER ";"


proc_statement_list ::=
    proc_statement
    | proc_statement_list proc_statement


proc_statement ::=
    assignment
    | if_statement_proc
    | while_statement_proc
    | for_statement_proc
    | put_line_statement


func_statement_list ::=
    func_statement
    | func_statement_list func_statement


func_statement ::=
    assignment
    | if_statement_func
    | while_statement_func
    | for_statement_func
    | return_statement


put_line_statement ::=
    "Put_Line" "(" expression ")" ";"


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
```

## Technologie

| Warstwa             | Technologia                           | Wersja |
| ------------------- | ------------------------------------- | ------ |
| Język implementacji | Java                                  | 17     |
| Framework webowy    | Spring Boot (spring-boot-starter-web) | 3.2.3  |
| Generator parsera   | ANTLR4                                | 4.13.1 |
| System budowania    | Apache Maven                          | ---    |
| Frontend            | HTML + Bootstrap 5.3 + vanilla JS     | ---    |

## Architektura systemu

Pipeline przetwarzania kodu przebiega przez cztery etapy:

1. **Leksykalna analiza** – `AdaLexer` (generowany z `AdaLexer.g4`)  
   zamienia strumień znaków na sekwencję tokenów.

2. **Analiza składniowa** – `AdaParser` (generowany z `AdaParser.g4`)  
   buduje drzewo rozbioru (_ParseTree_).

3. **Analiza semantyczna i generowanie kodu** – `AdaToCVisitor`  
   przechodzi drzewo metodą _Visitor_ i produkuje kod C.

4. **Wyjście** – tekst kodu C zwracany do wywołującego  
   (plik `.c` w trybie CLI, odpowiedź JSON w trybie web).

---

# Ada → C Konwerter

Projekt to transpiler języka **Ada do C** z dwoma trybami działania:

- aplikacja webowa (Spring Boot REST API),
- tryb CLI do kompilacji plików `.adb`.

---

## Struktura projektu

### Application

Punkt startowy aplikacji webowej opartej na Spring Boot. Uruchamia serwer na porcie `8080`.

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### Main

Punkt wejściowy trybu CLI.
Odpowiada za:

- przyjęcie listy plików .adb
- kompilację każdego pliku przez Compiler.compileFile()
- zapis wyników do katalogu out/
- wypisanie błędów na stderr
- zakończenie programu kodem 2 w przypadku błędów

### Compiler

Klasa odpowiedzialna za przeprowadzenie procesu konwersji kodu Ada na C z użyciem ANTLR4.
Zakres odpowiedzialności:

- wczytywanie plików (CharStreams.fromFileName)
- konfiguracja leksera i parsera
- obsługa błędów składniowych (ErrorListener)
- obsługa ParseCancellationException
- uruchomienie AdaToCVisitor
- udostępnienie błędów semantycznych

### CompilerController

REST API w Spring Boot (`@RestController`)

Endpoint: `POST /api/compile`

Wejście:
```json
{
  "code": "..."
}
```

Wyjście — sukces:
```json
{
  "success": true,
  "cCode": "..."
}
```

Wyjście — błąd:
```json
{
  "success": false,
  "errors": []
}
```

### AdaToCVisitor

Główna klasa konwertera. Dziedziczy po `AdaParserBaseVisitor<String>`.
Każda metoda `visitXxx()`:

- odpowiada regule gramatycznej
- zwraca fragment kodu C jako String

#### Zarządzanie zakresami

Struktury:

- `scopeTypes`: `Map<String, CType>` – mapowanie zmiennych na typy
- `declaredVars`: `Set<String>` – zmienne w bieżącym scope

Operacje:

- `pushScope()` – wejście do bloku
- `popScope()` – wyjście z bloku

#### System typów

Wewnętrzny enum `CType` definiuje podstawowe typy używane w systemie:

```java
private enum CType {
    INT("int"),
    DOUBLE("double"),
    BOOL("bool");
}
```

#### Mapowanie typów Ada → C

| Typ Ada  | Typ C  |
| -------- | ------ |
| Integer  | int    |
| Natural  | int    |
| Positive | int    |
| Float    | double |
| Boolean  | bool   |

#### Generowanie kodu

Wcięcia zarządzane są zmienną indentLevel.
Metoda indent() generuje odpowiedni łańcuch białych znaków.

## Obsługiwane konstrukcje języka Ada

| Konstrukcja Ada                              | Odpowiednik C                                     |
| -------------------------------------------- | ------------------------------------------------- |
| `procedure P is ... end P;`                  | `void P() { ... }`                                |
| `procedure Main is ...`                      | `int main() { ... return 0; }`                    |
| `function F return T is ...`                 | `T F() { ... }`                                   |
| `X : Integer := E;`                          | `int X = E;` (deklaracja ze typem)                |
| `X : Float;`                                 | `double X;` (deklaracja bez wartości początkowej) |
| `X : array (A..B) of Integer;`               | `int X[B+1];` (tablica 1-indeksowana)             |
| `X := E;`                                    | `X = E;`                                          |
| `if ... then ... elsif ... else ... end if;` | `if (...) { ... } else if { ... } else { ... }`   |
| `while ... loop ... end loop;`               | `while (...) { ... }`                             |
| `for I in A .. B loop ...`                   | `for (int I = A; I <= B; I++) { }`                |
| `return E;`                                  | `return E;`                                       |
| `Put_Line(E);`                               | `printf("%d\n", E)` / `%f`                        |
| `A(I)`, `A(I,J)`                             | `A[I]`, `A[I][J]`                                 |
| `=`, `/=`                                    | `==`, `!=`                                        |

## Analiza semantyczna

Klasa `AdaToCVisitor` przeprowadza analizę semantyczną podczas przechodzenia drzewa składniowego. Wykryte błędy są zbierane w liście `semanticErrors` i zwracane po zakończeniu procesu kompilacji. Każdy komunikat o błędzie zawiera numer linii w oryginalnym kodzie Ada (np. `Linia 5: Undeclared variable used: x`).

## Wykrywane błędy semantyczne

| Rodzaj błędu                      | Przykład wyzwalający                        |
| --------------------------------- | ------------------------------------------- |
| Użycie niezadeklarowanej zmiennej         | `x := y + 1` (y nieznane)                              |
| Przypisanie do niezadeklarowanej zmiennej | `z := 5;` (z nie zadeklarowane w sekcji deklaracji)    |
| Duplikat deklaracji zmiennej              | `x : Integer; x : Float;` w tym samym zakresie         |
| Niezgodność typów w przypisaniu           | przypisanie `double` do `int`                           |
| Niezgodność typu return                   | funkcja `return Integer` zwraca `Float`                 |
| Operacja arytmetyczna na `bool`           | `True + 1`                                              |
| Dzielenie przez zero (statyczne)          | `x := a / 0;`                                          |
| Nieprawidłowy typ indeksu tablicy         | `A(1.5)`                                               |
| Indeks tablicy poza zakresem              | `A(10)` przy rozmiarze 5                               |
| Pusta funkcja (brak `return`)             | `function F return Integer is begin end F;`            |
| Nieznany typ Ada                          | `x : Integre := 1;`                                    |
| Niezgodna nazwa po `end`                  | `procedure Foo is ... end Bar;`                        |

## Interfejs webowy

Frontend jest serwowany jako statyczny plik znajdujący się w:

`src/main/resources/static/index.html`

### Elementy interfejsu

- **Lewy panel:** pole tekstowe z kodem Ada (czcionka monospace, ciemne tło)
- **Przycisk _Konwertuj do C_:** wywołuje `POST /api/compile` przez Fetch API
- **Prawy panel:** wyświetla wygenerowany kod C (kolor niebieski) lub komunikat o błędzie
- **Sekcja błędów semantycznych:** lista błędów wyświetlana na czerwono poniżej paneli

## Uruchomienie

### Aplikacja webowa

Aby uruchomić wersję webową projektu, należy użyć Maven:

```bash
mvn spring-boot:run
```

Po uruchomieniu aplikacja będzie dostępna pod adresem:

http://localhost:8080

### Tryb CLI

Projekt można również uruchomić w trybie konsolowym:

```bash
mvn package -DskipTests
java -jar target/ada-to-c-1.0.jar examples/factorial.adb
```

Wynik działania programu zostanie zapisany np. do pliku:

out/factorial.c

W przypadku błędów program wypisuje komunikaty na stderr i kończy działanie z kodem.

## Przykłady

### Silnia (Ada)

```ada
-- examples/factorial.adb
procedure Factorial is
    n    : Integer := 5;
    fact : Integer := 1;
begin
    for i in 1 .. n loop
        fact := fact * i;
    end loop;
end Factorial;
```

### Wygenerowany kod C

```c
// out/factorial.c
#include <stdio.h>
#include <stdbool.h>
#include <stdlib.h>

void Factorial() {
    int n = 5;
    int fact = 1;
    for (int i = 1; i <= n; i++) {
        fact = fact * i;
    }
}

int main() {
    Factorial();
    return 0;
}
```

### Przykład z błędem semantycznym

```ada
procedure DivByZero is
    x : Integer := 0;
    y : Integer := 0;
begin
    y := 10 / x;
end DivByZero;
```

Konwerter wykrywa dzielenie przez zero statycznie i zwraca błąd zamiast generować kod C:

```json
{
  "success": false,
  "errors": [
    "Linia 5: Błąd semantyczny: Wykryto dzielenie przez zero za pomocą operatora '/'"
  ]
}
```
