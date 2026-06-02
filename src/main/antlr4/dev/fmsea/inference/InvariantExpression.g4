grammar InvariantExpression;

WS : [ \t\n]+ -> skip ;

RELOP
    : '=='
    | '<='
    | '>='
    | '<'
    | '>'
    ;

SUMOP : '+' | '-' ;

Numeral
    : '0'
    | '-'? [1-9] [0-9]*
    ;

Identifier
    : '-'? '$'? [a-z]+ [0-9]*
    ;

invariant
    : expr RELOP expr # RelationalConstraint
    ;

expr
    : expr SUMOP expr          # BinarySum
    | Identifier               # Identifier
    | Numeral                  # Numeral
    ;
