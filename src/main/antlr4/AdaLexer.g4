lexer grammar AdaLexer;

PROCEDURE  : P R O C E D U R E ;
FUNCTION   : F U N C T I O N ;
IS         : I S ;
BEGIN      : B E G I N ;
END        : E N D ;
IF         : I F ;
THEN       : T H E N ;
ELSE       : E L S E ;
ELSIF      : E L S I F ;
WHILE      : W H I L E ;
LOOP       : L O O P ;
FOR        : F O R ;
IN         : I N ;
RETURN     : R E T U R N ;
DECLARE    : D E C L A R E ;

ASSIGN : ':=' ;
RANGE  : '..' ;

PLUS   : '+' ;
MINUS  : '-' ;
MUL    : '*' ;
DIV    : '/' ;

EQ     : '=' ;
NEQ    : '/=' ;
LE     : '<=' ;
GE     : '>=' ;
LT     : '<' ;
GT     : '>' ;

SEMICOLON : ';' ;
COMMA     : ',' ;
LPAREN    : '(' ;
RPAREN    : ')' ;


FLOAT   : [0-9]+ '.' [0-9]+ ;
INTEGER : [0-9]+ ;

IDENTIFIER : [a-zA-Z_] [a-zA-Z0-9_]* ;

COMMENT : '--' ~[\r\n]* -> skip ;

WS : [ \t\r\n]+ -> skip ;

fragment A:[aA]; fragment B:[bB]; fragment C:[cC]; fragment D:[dD];
fragment E:[eE]; fragment F:[fF]; fragment G:[gG]; fragment H:[hH];
fragment I:[iI]; fragment J:[jJ]; fragment K:[kK]; fragment L:[lL];
fragment M:[mM]; fragment N:[nN]; fragment O:[oO]; fragment P:[pP];
fragment Q:[qQ]; fragment R:[rR]; fragment S:[sS]; fragment T:[tT];
fragment U:[uU]; fragment V:[vV]; fragment W:[wW]; fragment X:[xX];
fragment Y:[yY]; fragment Z:[zZ];