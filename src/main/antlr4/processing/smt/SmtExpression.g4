grammar SmtExpression;

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

PS_And
    : 'and'
    ;

PS_Or
    : 'or'
    ;

PS_True
    : 'true'
    ;

PS_False
    : 'false'
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

PS_MOD
    : 'mod'
    ;

Numerial
    : '0'
    | [1-9] [0-9]*
    ;

Identifier
    : '$'? [a-z]+ [0-9]*
    ;

expr
    : ParOpen PS_And expr+ ParClose       # And
    | ParOpen PS_Or  expr+ ParClose       # Or
    | ParOpen PS_Not expr ParClose        # Not
    | ParOpen PS_LT expr expr ParClose    # LessThan
    | ParOpen PS_GT expr expr ParClose    # GreaterThan
    | ParOpen PS_LE expr expr ParClose    # LessOrEqual
    | ParOpen PS_GE expr expr ParClose    # GreaterOrEqual
    | ParOpen PS_EQ expr expr ParClose    # Equal
    | ParOpen PS_MUL expr expr ParClose   # Multiplication
    | ParOpen PS_ADD expr expr ParClose   # Addition
    | ParOpen PS_SUB expr expr ParClose   # Subtraction
    | ParOpen PS_DIV expr expr ParClose   # Division
    | ParOpen PS_MOD expr expr ParClose   # Modulus
    | ParOpen PS_SUB expr ParClose        # Negation
    | PS_True                             # True
    | PS_False                            # False
    | Identifier                          # Identifier
    | Numerial                            # Number
    ;
