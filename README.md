# CoA

## Creator

[Sherwin Paul Sabandal, Chakinzo Sombito]

## Language Overview
The CoA programming langauge is a language built for simplicity. It is influenced by the C programming language. As such, there are some similarities in syntax with C. However, it has several functionalities that are different from C. In particular, CoA is dynamic and strongly typed in contrast to C which is static and weakly typed. Another difference is CoA’s introduction of the Aray structured data type.

Aray Structured Data Type.
	The aray structured data type is a combination of the C array and struct. Additionally, it can also be heterogenous. Arays can also be declared within an aray. It can be declared with indices – which indicates the maximum number of variables it can store.


## Keywords
| Keywords | Description |
|----------|-------------|
| lagay	   |             declare a mutable variable|
| peg	   |             declare an immutable variable |
|enum	   |             Enumeration |
|kung	   |             conditional (if) |
|kungdi	   |             conditional (else if) |
|kundi	   |             else branch |
|baylo	   |             Switch. Executes one block of code among many options based on an expression’s value |
|kaso	   |             Defines a branch in a switch(baylo) statement. |
|habang	   |             loop while condition is true |
|para	   |             C-style for loop (init; cond; step) |
|padayon   |             Skips the rest of the loop body and moves to the next iteration. |
|guba	   |             break; Exits loops and the switch statement |
|ganap	   |             function declaration |
|balik	   |             return from a function (optionally with value) |
|kadto	   |             Jumps to a labeled statement in the same function |
|void	   |             Specifies that a function returns no value or that a pointer has no specific type. |
|yass(true), noh(false) | Boolean Literals |
|wala	   |             Null Literal |
|chika	   |             built-in print/log to output values |

## Operators
| Type | Operators |
|------|-----------|
|Arithmetic | + - * / % |
|Relational |   < <= > >= == != |
|Logical (symbolic) | ! && || |
|Logical (word aliases) | 1) hindi (prefix) → logical NOT (e.g., hindi x) <br>2) tsaka (infix) → logical AND (e.g., a tsaka b) <br>3) or (infix) → logical OR (e.g., a or b) |
|Assignment | = += -= *= /= %= |
|Increment | ++ |
|Decrement | -- |
|Bitwise | & ` ^ ~ << >> |
|Conditional Ternary | ?: |
|Special | sizeof, ., ->, &, * |
|Comma | , |

## Literals
| Type | Description |
|------|-------------|
| Numbers | integers (42), float (3.14, .5, 0.3333333), and double (3.14159265358979323, .14159265358979323) |
| Strings | enclosed in double quotes "hello". Supports escape sequences \", \\, \n, \t, \r. |
| Characters | enclosed in single quotes, 'c' |
| Booleans | yass, noh. |
| Null | wala.|

## Identifiers
1) Can contain letters, digits, and underscores (_).
2) Must not start with a digit.
3) Must start with OA.
4) Case-sensitive (OAmyVar and OAmyvar are different identifiers)

## Comments
1) Single-line: // until the end of the line.
2) Block: /* ... */, may span multiple lines.
3) Nested block comments are not supported.

## Syntax Style
1) Whitespace is not significant except to separate tokens.
2) Statements are terminated by a semicolon (;).
3) Blocks are enclosed in braces { ... }.
4) Parentheses () are used for grouping expressions and control flow conditions.

## Sample Code
```
ganap main() {
    chika("Hi, bes!");
    
    peg OAgreeting = "As in hello, world!";
    lagay OAcount = 0;

    chika(OAgreeting);

    kung (OAcount == 0 tsaka yass) {
        chika("fresh pa tayo");
    } kundi {
        chika("medyo pagod na");
    }

    para (lagay i = 0; OAi < 3; OAi = OAi + 1) {
        chika("count: " + OAi);
    }

    habang (hindi (OAcount >= 5) or yass) {
        OAcount = OAcount + 1;
        chika("progress: " + OAcount);
        kung (OAcount > 6) {
            balik wala;
        }
    }

    balik 0;
}

```
## Design Rationale


Dynamic typing: simplifies the language and accelerates prototyping.

C-inspired and Kotlin-inspired syntax: familiar and modern, but simplified for easier use.

Block-style languages, such as C, prepare the language for easy parsing in later stages.

Minimal features: chosen to be feasible in a 3-month course.

Error handling: built into the scanner to detect invalid tokens, unterminated strings, and comments.

Educational motivation: emphasizes learning how real languages tokenize, parse, and interpret code rather than creating a production-ready language.


PROGRAM STRUCTURE:

```
Program        ::= { TopLevelDecl } EOF ;

TopLevelDecl   ::= FunDecl
                 | VarDecl
                 | ConstDecl
                 | Statement ;

FunDecl        ::= "ganap" IDENTIFIER "(" [ ParamList ] ")" Block ;
ParamList      ::= IDENTIFIER { COMMA IDENTIFIER } ;

VarDecl        ::= "lagay" IDENTIFIER [ EQUAL Expression ] SEMICOLON ;
ConstDecl      ::= "peg"   IDENTIFIER [ EQUAL Expression ] SEMICOLON ;

```


```
Statement      ::= ExprStmt
                 | PrintStmt
                 | ReturnStmt
                 | IfStmt
                 | WhileStmt
                 | ForStmt
                 | Block ;

Block          ::= LEFT_BRACE { Statement } RIGHT_BRACE ;

ExprStmt       ::= Expression SEMICOLON ;

PrintStmt      ::= "chika" "(" [ ArgList ] ")" SEMICOLON ;

ReturnStmt     ::= "balik" [ Expression ] SEMICOLON ;

IfStmt         ::= "kung" "(" Expression ")" Statement [ "kundi" Statement ] ;

WhileStmt      ::= "habang" "(" Expression ")" Statement ;

ForStmt        ::= "para" "(" ForInit ForCond ForStep ")" Statement ;

ForInit        ::= VarDecl
                 | ConstDecl
                 | [ Expression ] SEMICOLON ;
ForCond        ::= [ Expression ] SEMICOLON ;
ForStep        ::= [ Expression ] ;

```

```
Expression     ::= Assignment ;

Assignment     ::= LogicOr
                 | LValue AssignOp Assignment ;

LValue         ::= Primary { Member } ;
Member         ::= PERIOD IDENTIFIER
                 | CallArgs ;

AssignOp       ::= EQUAL
                 | PLUS_EQUAL | MINUS_EQUAL | STAR_EQUAL | DIVIDE_EQUAL | MODULO_EQUAL ;

CallArgs       ::= "(" [ ArgList ] ")" ;
ArgList        ::= Expression { COMMA Expression } ;

```

```
LogicOr        ::= LogicAnd { (OR_OR | "or") LogicAnd } ;
```

```
LogicAnd       ::= Equality { (AND_AND | "tsaka") Equality } ;
```

```
Equality       ::= Comparison { (EQUAL_EQUAL | BANG_EQUAL) Comparison } ;

Comparison     ::= Concat { (LESS | LESS_EQUAL | GREATER | GREATER_EQUAL) Concat } ;

```

```
Concat         ::= Term { CONCAT Term } ;
```

```
Term           ::= Factor { (PLUS | MINUS) Factor } ;

Factor         ::= Unary { (STAR | DIVIDE | MODULO) Unary } ;

Unary          ::= (BANG | "hindi" | MINUS) Unary
                 | Postfix ;
```

```
Postfix        ::= Primary { Member } ;
```

```
Primary        ::= IDENTIFIER
                 | NUMBER
                 | STRING
                 | "yass"        // true
                 | "noh"         // false
                 | "wala"        // null
                 | "(" Expression ")" ;
```



