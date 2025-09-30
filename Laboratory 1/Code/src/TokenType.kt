enum class TokenType {
    // punctuation & operators
    COMMA, SEMICOLON, PERIOD,
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    PLUS, MINUS, STAR, DIVIDE,
    LESS, LESS_EQUAL, GREATER, GREATER_EQUAL,
    EQUAL, EQUAL_EQUAL, BANG, BANG_EQUAL,
    AND_AND, OR_OR,

    // keywords
    VAR, VAL, IF_CONDITIONAL, ELSE_CONDITIONAL,
    WHILE_LOOP, FOR_LOOP,
    FUNCTION_DECLARATION, RETURN_CALL,
    FUNCTION, BOOLEAN, NULL, LOOP_CONTROL,
    LOGICAL_OPERATORS,

    // literals / identifiers
    STRING, NUMBER, IDENTIFIER,

    EOF
}

val classifiers = listOf(
    OperatorClassifier(),
    KeywordClassifier(),
    LiteralClassifier(),
    IdentifierClassifier() // fallback
)

fun classifyLexeme(lexeme: String): TokenType {
    for (c in classifiers) {
        if (c.matches(lexeme)) return c.classify(lexeme)
    }
    return TokenType.IDENTIFIER
}


