grammar TADR;

WS
    : [ \t\n]+ -> skip
    ;

ParOpen
    : '('
    ;

ParClose
    : ')'
    ;

PS_Not
    : 'not'
    ;

PS_LT
    : '<'
    ;

PS_LE
    : '<='
    ;

PS_GT
    : '>'
    ;

PS_GE
    : '>='
    ;

PS_EQ
    : '='
    ;

PS_MUL
    : '*'
    ;

PS_ADD
    : '+'
    ;

PS_SUB
    : '-'
    ;

PS_DIV
    : 'div'
    ;

Numerial
    : '0'
    | [1-9] [0-9]*
    ;

Identifier
    : '$'? [a-z]+ [0-9]*
    ;

expr
    : ParOpen PS_Not expr ParClose        # Not
    | ParOpen PS_LT expr expr ParClose    # LessThan
    | ParOpen PS_GT expr expr ParClose    # GreaterThan
    | ParOpen PS_LE expr expr ParClose    # LessOrEqual
    | ParOpen PS_GE expr expr ParClose    # GreaterOrEqual
    | ParOpen PS_EQ expr expr ParClose    # Equal
    | ParOpen PS_MUL expr expr ParClose   # Multiplication
    | ParOpen PS_ADD expr expr ParClose   # Addition
    | ParOpen PS_SUB expr expr ParClose   # Subtraction
    | ParOpen PS_DIV expr expr ParClose   # Division
    | ParOpen PS_SUB expr ParClose        # Negation
    | Identifier                          # Identifier
    | Numerial                            # Value
    ;
