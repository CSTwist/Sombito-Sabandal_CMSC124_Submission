// TokenType.kt
enum class TokenType {
    // punctuation
    COMMA, SEMICOLON, PERIOD,
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    LEFT_BRACKET, RIGHT_BRACKET,

    // arithmetic & operators
    PLUS, MINUS, STAR, DIVIDE, MODULO,
    CONCAT,
    LESS, LESS_EQUAL, GREATER, GREATER_EQUAL,
    EQUAL, EQUAL_EQUAL,
    BANG, BANG_EQUAL,
    AND_AND, OR_OR,

    // compound assignment
    PLUS_EQUAL, MINUS_EQUAL, STAR_EQUAL, DIVIDE_EQUAL, MODULO_EQUAL,

    // increment/decrement
    PLUS_PLUS, MINUS_MINUS,

    // word logical operators
    NOT_WORD, AND_WORD, OR_WORD,

    // keywords (CoA language)
    VAR, CONST, ARAY, ENUM,
    IF_CONDITIONAL, ELSE_IF_CONDITIONAL, ELSE_CONDITIONAL,
    WHILE_LOOP, FOR_LOOP,
    FUNCTION_DECLARATION, FUNCTION, OTHER, RETURN_CALL,
    PRINT,

    // literals
    STRING, NUMBER, IDENTIFIER,
    TRUE, FALSE, NULL,

    // control flow
    BREAK, CONTINUE,

    // special
    EOF, INVALID
}

// Lexeme classifier hook
fun classifyLexeme(lexeme: String): TokenType {
    return when (lexeme) {
        // punctuation
        "," -> TokenType.COMMA
        ";" -> TokenType.SEMICOLON
        "." -> TokenType.PERIOD
        "(" -> TokenType.LEFT_PAREN
        ")" -> TokenType.RIGHT_PAREN
        "{" -> TokenType.LEFT_BRACE
        "}" -> TokenType.RIGHT_BRACE
        "[" -> TokenType.LEFT_BRACKET
        "]" -> TokenType.RIGHT_BRACKET

        // arithmetic & operators
        "+" -> TokenType.PLUS
        "-" -> TokenType.MINUS
        "*" -> TokenType.STAR
        "/" -> TokenType.DIVIDE
        "%" -> TokenType.MODULO
        "<" -> TokenType.LESS
        "<=" -> TokenType.LESS_EQUAL
        ">" -> TokenType.GREATER
        ">=" -> TokenType.GREATER_EQUAL
        "=" -> TokenType.EQUAL
        "==" -> TokenType.EQUAL_EQUAL
        "!=" -> TokenType.BANG_EQUAL
        "!" -> TokenType.BANG
        "&&" -> TokenType.AND_AND
        "||" -> TokenType.OR_OR

        // compound assignment
        "+=" -> TokenType.PLUS_EQUAL
        "-=" -> TokenType.MINUS_EQUAL
        "*=" -> TokenType.STAR_EQUAL
        "/=" -> TokenType.DIVIDE_EQUAL
        "%=" -> TokenType.MODULO_EQUAL

        // increment/decrement
        "++" -> TokenType.PLUS_PLUS
        "--" -> TokenType.MINUS_MINUS

        // keywords (CoA)
        "lagay" -> TokenType.VAR
        "peg" -> TokenType.CONST
        "aray" -> TokenType.ARAY
        "enum" -> TokenType.ENUM
        "kung" -> TokenType.IF_CONDITIONAL
        "kungdi" -> TokenType.ELSE_IF_CONDITIONAL
        "kundi" -> TokenType.ELSE_CONDITIONAL
        "habang" -> TokenType.WHILE_LOOP
        "para" -> TokenType.FOR_LOOP
        "ganap" -> TokenType.FUNCTION_DECLARATION
        "balik" -> TokenType.RETURN_CALL
        "chika" -> TokenType.PRINT
        "other" -> TokenType.OTHER
        "yass" -> TokenType.TRUE
        "noh" -> TokenType.FALSE
        "wala" -> TokenType.NULL
        "guba" -> TokenType.BREAK
        "padayon" -> TokenType.CONTINUE
        "hindi" -> TokenType.NOT_WORD
        "tsaka" -> TokenType.AND_WORD
        "or" -> TokenType.OR_WORD

        else -> when {
            // numeric literals (int or float)
            lexeme.matches(Regex("^[0-9]+(\\.[0-9]+)?$")) -> TokenType.NUMBER

            // string literals (double-quoted)
            lexeme.matches(Regex("^\".*\"$")) -> TokenType.STRING

            // valid identifiers
            lexeme.matches(Regex("^[a-zA-Z_][a-zA-Z0-9_]*$")) -> TokenType.IDENTIFIER

            // otherwise invalid
            else -> TokenType.INVALID
        }
    }
}