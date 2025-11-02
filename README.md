# CoA

## Creator

[Sherwin Paul Sabandal, Chakinzo Sombito]

## Language Overview
The CoA programming langauge is a language built for simplicity. It is influenced by the C programming language. As such, there are some similarities in syntax with C. However, it has several functionalities that are different from C. In particular, CoA is dynamic and strongly typed in contrast to C which is static and weakly typed. Another difference is CoA’s introduction of the Aray structured data type.

Aray Structured Data Type.
	The aray structured data type is a combination of the C array and struct. Additionally, it can also be heterogenous. Arays can also be declared within an aray. It can be declared with indices – which indicates the maximum number of variables it can store.

DECLARATION:
lagay arayName[max_variables] {variable declerations} 

EXAMPLES
1)
aray Student {
    lagay variableName;
//or
    lagay arayName[max_indices]{};
//or
    lagay arayName{};
    …
};

2)
aray Student {
    lagay name[50]{};
    lagay details{
        lagay age;
        lagay grade;
    }    
};

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
3) Must start with OA_.
4) Case-sensitive (OA_myVar and OA_myvar are different identifiers)

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
    
    peg OA_greeting = "As in hello, world!";
    lagay OA_count = 0;

    chika(OA_greeting);

    kung (OA_count == 0 tsaka yass) {
        chika("fresh pa tayo");
    } kundi {
        chika("medyo pagod na");
    }

    para (lagay i = 0; OA_i < 3; OA_i = OA_i + 1) {
        chika("count: " + OA_i);
    }

    habang (hindi (OA_count >= 5) or yass) {
        OA_count = OA_count + 1;
        chika("progress: " + OA_count);
        kung (OA_count > 6) {
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


## Grammar

```
Program        ::= { TopLevelDecl } EOF
TopLevelDecl   ::= FunDecl
                 | VarDecl
                 | ConstDecl
                 | ArayDecl
                 | EnumDecl
                 | Statement

FunDecl        ::= "ganap" IDENTIFIER "(" [ ParamList ] ")" Block
ParamList      ::= IDENTIFIER { "," IDENTIFIER }

VarDecl        ::= "lagay" IDENTIFIER [ "=" Expression ] ";"
ConstDecl      ::= "peg" IDENTIFIER [ "=" Expression ] ";"

ArayDecl       ::= "aray" IDENTIFIER "{" { ArayMember } "}" ";"
ArayMember     ::= DataType IDENTIFIER [ "[" NUMBER "]" ] ";"
                 | "aray" IDENTIFIER [ "[" NUMBER "]" ] ";"

EnumDecl       ::= "enum" IDENTIFIER "{" EnumList "}" ";"
EnumList       ::= IDENTIFIER { "," IDENTIFIER }

```


```
Statement      ::= ExprStmt
                 | PrintStmt
                 | ReturnStmt
                 | IfStmt
                 | WhileStmt
                 | ForStmt
                 | SwitchStmt
                 | BreakStmt
                 | ContinueStmt
                 | Block

Block          ::= "{" { Statement } "}"

ExprStmt       ::= Expression ";"
PrintStmt      ::= "chika" "(" [ ArgList ] ")" ";"
ReturnStmt     ::= "balik" [ Expression ] ";"

IfStmt         ::= "kung" "(" Expression ")" Statement
                   { "kungdi" "(" Expression ")" Statement }
                   [ "kundi" Statement ]

SwitchStmt     ::= "baylo" "(" Expression ")" "{"
                     { "kaso" Constant ":" { Statement } }
                     [ "kundi" ":" { Statement } ]
                   "}"

WhileStmt      ::= "habang" "(" Expression ")" Statement

ForStmt        ::= "para" "(" ForInit ForCond ForStep ")" Statement
ForInit        ::= VarDecl | ConstDecl | [ Expression ] ";"
ForCond        ::= [ Expression ] ";"
ForStep        ::= [ Expression ]

BreakStmt      ::= "guba" ";"
ContinueStmt   ::= "padayon" ";"
GotoStmt       ::= "kadto" IDENTIFIER ";"

```

```
Expression     ::= Assignment
Assignment     ::= LogicOr | LValue AssignOp Assignment
AssignOp       ::= "=" | "+=" | "-=" | "*=" | "/=" | "%="

CallArgs       ::= "(" [ ArgList ] ")" ;
ArgList        ::= Expression { COMMA Expression } ;

```

```
LogicOr        ::= LogicAnd { ("||" | "or") LogicAnd }
LogicAnd       ::= Equality { ("&&" | "tsaka") Equality }
Equality       ::= Comparison { ("==" | "!=") Comparison }
Comparison     ::= Term { ("<" | "<=" | ">" | ">=") Term }
Term           ::= Factor { ("+" | "-") Factor }
Factor         ::= Unary { ("*" | "/" | "%") Unary }
Unary          ::= ("!" | "hindi" | "-") Unary | Postfix

```

```
Postfix        ::= Primary { Member }
Member         ::= "." IDENTIFIER | CallArgs
CallArgs       ::= "(" [ ArgList ] ")"
ArgList        ::= Expression { "," Expression }

Primary        ::= IDENTIFIER
                 | NUMBER
                 | STRING
                 | "yass"        // true
                 | "noh"         // false
                 | "wala"        // null
                 | "(" Expression ")"
```



