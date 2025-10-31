# CoA

Creator

[Sherwin Paul Sabandal, Chakinzo Sombito]

## Language Overview
CoA is an imperative, procedural programming language.

Keywords
lagay — declare a mutable variable (like var)

peg — declare an immutable variable (your “fixed peg”; like val)

kung — conditional (if)

kundi — else branch

habang — loop while condition is true

para — C-style for loop (init; cond; step)

ganap — function declaration

balik — return from a function (optionally with value)

yass — boolean literal true

noh — boolean literal false

wala — null literal

chika — built-in print/log to output values

Operators

Arithmetic: + - * / %
Comparison: < <= > >= == !=
Logical (symbolic): ! && ||
Logical (word aliases):

hindi (prefix) → logical NOT (e.g., hindi x)

tsaka (infix) → logical AND (e.g., a tsaka b)

or (infix) → logical OR (e.g., a or b)
Assignment: = (plus optional += -= *= /= %=)
Concatenate (optional): ++ for strings (e.g., "hi" ++ name)


Assignment: =

Delimiters: ( ) { } , ; .

Literals
Numbers: integers (42) and decimals (3.14, .5).

Strings: enclosed in double quotes "hello". Supports escape sequences \", \\, \n, \t, \r.

Booleans: true, false.

Null: null.

Identifiers

Can contain letters, digits, and underscores (_).

Must not start with a digit.

Case-sensitive (myVar and myvar are different identifiers)

Comments

Single-line: // until the end of the line.

Block: /* ... */, may span multiple lines.

Nested block comments are not supported.

Syntax Style
Whitespace is not significant except to separate tokens.

Statements are terminated by a semicolon (;).

Blocks are enclosed in braces { ... }.

Parentheses () are used for grouping expressions and control flow conditions.

Sample Code
```
ganap main() {
    chika("Hi, bes!");
    
    peg greeting = "As in hello, world!";
    lagay count = 0;

    chika(greeting);

    kung (count == 0 tsaka yass) {
        chika("fresh pa tayo");
    } kundi {
        chika("medyo pagod na");
    }

    para (lagay i = 0; i < 3; i = i + 1) {
        chika("count: " + i);
    }

    habang (hindi (count >= 5) or yass) {
        count = count + 1;
        chika("progress: " + count);
        kung (count > 6) {
            balik wala;
        }
    }

    balik 0;
}

```
Design Rationale


Dynamic typing: simplifies the language and accelerates prototyping.

Kotlin-inspired syntax: familiar and modern, but simplified for easier use.

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



