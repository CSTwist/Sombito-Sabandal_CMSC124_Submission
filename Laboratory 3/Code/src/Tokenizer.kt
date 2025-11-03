// Tokenizer.kt
object Tokenizer {

    fun tokenizeBlock(lines: List<String>) {
        var multiLineComment = false
        var lineNumber = 1

        for (line in lines) {
            val tokensInLine = scanLine(line, lineNumber)
            multiLineComment = classifyAndPrintTokens(tokensInLine, lineNumber, multiLineComment)
            lineNumber++
        }

        printEOF(lineNumber - 1)

        if (multiLineComment) {
            System.err.println("[line ${lineNumber - 1}] Error: Unterminated block comment (missing */)")
        }
    }

    fun tokenizeToTokens(lines: List<String>): List<Token> {
        val out = mutableListOf<Token>()
        var multiLineComment = false
        var lineNumber = 1

        for (line in lines) {
            val lexemes = scanLine(line, lineNumber)
            var i = 0
            while (i < lexemes.size) {
                val lex = lexemes[i]

                if (lex == "/*") { multiLineComment = true; i++; continue }
                if (lex == "*/") { multiLineComment = false; i++; continue }
                if (multiLineComment) { i++; continue }

                val type = classifyLexeme(lex)
                val literal = extractLiteral(type, lex)
                out.add(Token(type, lex, literal, lineNumber))
                i++
            }
            lineNumber++
        }

        out.add(Token(TokenType.EOF, "", null, (lineNumber - 1).coerceAtLeast(1)))

        if (multiLineComment) {
            System.err.println("[line ${lineNumber - 1}] Error: Unterminated block comment (missing */)")
        }

        return out
    }

    private fun scanLine(line: String, lineNumber: Int): MutableList<String> {
        val tokens = mutableListOf<String>()
        var index = 0

        while (index < line.length) {
            val c = line[index]
            when {
                c.isWhitespace() -> index++

                c == '"' -> {
                    val (token, newIndex) = scanString(line, index, lineNumber)
                    if (token != null) tokens.add(token)
                    index = newIndex
                }

                isTwoCharOperator(line, index) -> {
                    val (token, newIndex) = scanTwoCharOperator(line, index)
                    if (token != null) tokens.add(token)
                    index = newIndex
                }

                isSingleCharOperator(c) -> {
                    tokens.add(c.toString())
                    index++
                }

                c.isDigit() || (c == '.' && index + 1 < line.length && line[index + 1].isDigit()) -> {
                    val (token, newIndex) = scanNumber(line, index)
                    tokens.add(token)
                    index = newIndex
                }

                c.isLetter() || c == '_' -> {
                    val (token, newIndex) = scanIdentifier(line, index)
                    tokens.add(token)
                    index = newIndex
                }

                else -> {
                    System.err.println("[line $lineNumber] Error: Unexpected character '$c'")
                    index++
                }
            }
        }
        return tokens
    }

    private fun classifyAndPrintTokens(
        tokensInLine: MutableList<String>,
        lineNumber: Int,
        startingCommentState: Boolean
    ): Boolean {
        var multiLineComment = startingCommentState
        var i = 0
        while (i < tokensInLine.size) {
            val lexeme = tokensInLine[i]

            if (lexeme == "/*") { multiLineComment = true; i++; continue }
            if (lexeme == "*/") { multiLineComment = false; i++; continue }

            val type = classifyLexeme(lexeme)
            val literal = extractLiteral(type, lexeme)

            if (!multiLineComment) {
                println("Token(Type=$type, Lexeme=$lexeme, Literal=$literal, Line=$lineNumber)")
            }
            i++
        }
        return multiLineComment
    }

    private fun extractLiteral(type: TokenType, lexeme: String): String? = when (type) {
        TokenType.STRING -> lexeme.removePrefix("\"").removeSuffix("\"")
        TokenType.NUMBER -> lexeme
        else -> null
    }

    private fun printEOF(lastLine: Int) {
        println("Token(Type=EOF, Lexeme=NULL, Literal=NULL, Line=$lastLine)")
    }

    private fun isTwoCharOperator(line: String, index: Int): Boolean {
        if (index + 1 >= line.length) return false
        val two = line.substring(index, index + 2)
        return two in listOf(
            "<=", ">=", "==", "!=", "&&", "||",
            "//", "/*", "*/",
            "++", "+=", "-=", "*=", "/=", "%="
        )
    }

    private fun scanTwoCharOperator(line: String, start: Int): Pair<String?, Int> {
        val two = line.substring(start, start + 2)
        return if (two == "//") Pair(null, line.length) else Pair(two, start + 2)
    }

    private fun isSingleCharOperator(c: Char): Boolean {
        return c in listOf(
            ',', ';', '.', '(', ')', '{', '}',
            '+', '-', '*', '/', '%',
            '<', '>', '=', '!'
        )
    }

    private fun scanNumber(line: String, start: Int): Pair<String, Int> {
        var index = start
        var hasDot = false
        if (line[index] == '.') { hasDot = true; index++ }
        while (index < line.length && line[index].isDigit()) index++
        if (index < line.length && line[index] == '.' && !hasDot) {
            if (index + 1 < line.length && line[index + 1].isDigit()) {
                hasDot = true; index++
                while (index < line.length && line[index].isDigit()) index++
            }
        }
        return Pair(line.substring(start, index), index)
    }

    private fun scanIdentifier(line: String, start: Int): Pair<String, Int> {
        var index = start + 1
        while (index < line.length && (line[index].isLetterOrDigit() || line[index] == '_')) index++
        return Pair(line.substring(start, index), index)
    }

    private fun scanString(line: String, start: Int, lineNumber: Int): Pair<String?, Int> {
        var index = start + 1
        while (index < line.length && line[index] != '"') {
            if (line[index] == '\\' && index + 1 < line.length) { index += 2; continue }
            index++
        }
        return if (index >= line.length) {
            System.err.println("[line $lineNumber] Error: Unterminated string")
            Pair(null, line.length)
        } else {
            Pair(line.substring(start, index + 1), index + 1)
        }
    }
}