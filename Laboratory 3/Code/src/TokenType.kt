enum class TokenType {
    // punctuation & delimiters
    COMMA, SEMICOLON, PERIOD,
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,

    // arithmetic & operators (symbols)
    PLUS, MINUS, STAR, DIVIDE, MODULO,          // + - * / %
    CONCAT,                                      // ++  (string concat; optional)
    LESS, LESS_EQUAL, GREATER, GREATER_EQUAL,    // < <= > >=
    EQUAL, EQUAL_EQUAL,                          // = ==
    BANG, BANG_EQUAL,                            // ! !=
    AND_AND, OR_OR,                              // && ||

    // word logical operators (Conyo)
    NOT_WORD,    // "hindi"
    AND_WORD,    // "tsaka"
    OR_WORD,     // "or" (kept as word-op; distinct from ||)

    // keywords (Conyo + legacy mapped by KeywordClassifier)
    VAR, VAL,                          // lagay / peg
    IF_CONDITIONAL, ELSE_CONDITIONAL,  // kung / kundi
    WHILE_LOOP, FOR_LOOP,              // habang / para
    FUNCTION_DECLARATION, RETURN_CALL, // ganap / balik
    FUNCTION,                          // chika / print
    BOOLEAN, NULL,                     // yass/noh -> BOOLEAN; wala -> NULL
    LOOP_CONTROL,                      // break / continue

    // literals / identifiers
    STRING, NUMBER, IDENTIFIER,

    EOF
}

// Keep your pipeline; OperatorClassifier/KeywordClassifier should emit the new tokens above
val classifiers = listOf(
    OperatorClassifier(),   // update to handle MODULO, CONCAT(++), and compound assigns
    KeywordClassifier(),    // update to emit NOT_WORD/AND_WORD/OR_WORD for hindi/tsaka/or
    LiteralClassifier(),
    IdentifierClassifier()  // fallback
)

fun classifyLexeme(lexeme: String): TokenType {
    for (c in classifiers) {
        if (c.matches(lexeme)) return c.classify(lexeme)
    }
    return TokenType.IDENTIFIER
}
