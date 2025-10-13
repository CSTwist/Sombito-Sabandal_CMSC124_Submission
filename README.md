# Cobra

Creator

[Sherwin Paul Sabandal, Chakinzo Sombito]

## Language Overview
Cobra is a simple, dynamically typed programming language inspired by Kotlin and the C family of languages.
It is designed for learning and prototyping language implementation concepts (scanner → parser → interpreter).
The language emphasizes simplicity, readability, and feasibility for a semester-long project.

Keywords
var — Declare a mutable variable

val — Declare an immutable variable

if — Conditional branching

else — Alternative branch for if

while — Loop construct

for — Loop construct with initialization, condition, and increment

fun — Function declaration

return — Exit from a function and optionally return a value

true — Boolean literal (true)

false — Boolean literal (false)

null — Null literal

print — Built-in function to output values

Operators
Arithmetic: + - * /

Comparison: < <= > >= == !=

Logical: ! && ||

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
var x = 10;  
val pi = 3.14;  

if (x > 5) {  
    print "x is greater than 5";  
} else {  
    print "x is 5 or less";  
}  

for (var i = 0; i < 3; i = i + 1) {  
    print i;  
}  

fun greet(name) {  
    print "Hello, " + name;  
}

greet("World");
```
Design Rationale


Dynamic typing: simplifies the language and accelerates prototyping.

Kotlin-inspired syntax: familiar and modern, but simplified for easier use.

Block-style languages, such as C, prepare the language for easy parsing in later stages.

Minimal features: chosen to be feasible in a 3-month course.

Error handling: built into the scanner to detect invalid tokens, unterminated strings, and comments.

Educational motivation: emphasizes learning how real languages tokenize, parse, and interpret code rather than creating a production-ready language.
