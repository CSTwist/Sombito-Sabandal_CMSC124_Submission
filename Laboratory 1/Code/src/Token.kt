class Token(
    var type: TokenType,
    var lexeme: String = "",
    var literal: String ?= "",
    var line: Int
)