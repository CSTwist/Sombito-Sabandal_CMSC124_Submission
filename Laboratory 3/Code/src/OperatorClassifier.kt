class OperatorClassifier : LexemeClassifier {
    private val single = mapOf(
        "," to TokenType.COMMA,
        ";" to TokenType.SEMICOLON,
        "." to TokenType.PERIOD,
        "(" to TokenType.LEFT_PAREN,
        ")" to TokenType.RIGHT_PAREN,
        "{" to TokenType.LEFT_BRACE,
        "}" to TokenType.RIGHT_BRACE,
        "+" to TokenType.PLUS,
        "-" to TokenType.MINUS,
        "*" to TokenType.STAR,
        "/" to TokenType.DIVIDE,
        "<" to TokenType.LESS,
        ">" to TokenType.GREATER,
        "=" to TokenType.EQUAL,
        "!" to TokenType.BANG
    )
    private val double = mapOf(
        "<=" to TokenType.LESS_EQUAL,
        ">=" to TokenType.GREATER_EQUAL,
        "==" to TokenType.EQUAL_EQUAL,
        "!=" to TokenType.BANG_EQUAL,
        "&&" to TokenType.AND_AND,
        "||" to TokenType.OR_OR,
        "+=" to TokenType.PLUS_EQUAL,
        "-=" to TokenType.MINUS_EQUAL,
        "*=" to TokenType.STAR_EQUAL,
        "/=" to TokenType.DIVIDE_EQUAL,
        "++" to TokenType.PLUS_PLUS,
        "--" to TokenType.MINUS_MINUS
    )


    override fun matches(lexeme: String) =
        single.containsKey(lexeme) || double.containsKey(lexeme)

    override fun classify(lexeme: String) =
        single[lexeme] ?: double[lexeme]!!
}
