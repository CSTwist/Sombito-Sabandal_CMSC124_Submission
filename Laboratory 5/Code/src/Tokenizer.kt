// Tokenizer.kt
//
// Clean, consistent tokenizer that matches Parser.kt and TokenType.kt.
// Handles keywords, numbers, percentages, time literals, strings,
// operators, identifiers, and comments correctly.

object Tokenizer {

    // ---------------------------------------------------------------
    // PUBLIC: Convert source lines → List<Token>
    // ---------------------------------------------------------------
    fun tokenizeToTokens(lines: List<String>): List<Token> {
        val tokens = mutableListOf<Token>()
        var multiLineComment = false
        var lineNumber = 1

        for (line in lines) {
            val lexemes = scanLine(line, lineNumber)
            var i = 0

            while (i < lexemes.size) {
                val lex = lexemes[i]

                // Comment handling
                if (lex == "/*") { multiLineComment = true; i++; continue }
                if (lex == "*/") { multiLineComment = false; i++; continue }
                if (multiLineComment) { i++; continue }

                if (lex == "//") break // ignore rest of line

                val type = classifyLexeme(lex)
                val literal = extractLiteral(type, lex)
                tokens.add(Token(type, lex, literal, lineNumber))

                i++
            }

            lineNumber++
        }

        tokens.add(Token(TokenType.EOF, "", null, lineNumber - 1))
        return tokens
    }

    // ---------------------------------------------------------------
    // Scan a SINGLE line into raw lexemes (strings)
    // ---------------------------------------------------------------
    private fun scanLine(line: String, lineNumber: Int): MutableList<String> {
        val tokens = mutableListOf<String>()
        var index = 0

        fun atEnd() = index >= line.length

        while (!atEnd()) {
            val c = line[index]

            when {
                c.isWhitespace() -> index++

                // --------------------------
                // String literal
                // --------------------------
                c == '"' -> {
                    val (token, newIdx) = scanString(line, index, lineNumber)
                    token?.let { tokens.add(it) }
                    index = newIdx
                }

                // --------------------------
                // Two-character operators
                // --------------------------
                isTwoCharOperator(line, index) -> {
                    val (op, newIdx) = scanTwoCharOperator(line, index)
                    op?.let { tokens.add(it) }
                    index = newIdx
                }

                // --------------------------
                // Single-char operator
                // --------------------------
                isSingleCharOperator(c) -> {
                    tokens.add(c.toString())
                    index++
                }

                // --------------------------
                // Number / %, s
                // --------------------------
                c.isDigit() -> {
                    val (num, newIdx) = scanNumberOrSpecial(line, index)
                    tokens.add(num)
                    index = newIdx
                }

                // --------------------------
                // Identifier or Keyword
                // --------------------------
                c.isLetter() || c == '_' -> {
                    val (id, newIdx) = scanIdentifier(line, index)
                    tokens.add(id)
                    index = newIdx
                }

                else -> {
                    System.err.println("[line $lineNumber] Error: Unexpected character '$c'")
                    index++
                }
            }
        }

        return tokens
    }

    // ---------------------------------------------------------------
    // STRING handling
    // ---------------------------------------------------------------
    private fun scanString(line: String, start: Int, lineNumber: Int): Pair<String?, Int> {
        var index = start + 1

        while (index < line.length && line[index] != '"') {
            if (line[index] == '\\' && index + 1 < line.length) {
                index += 2
                continue
            }
            index++
        }

        return if (index >= line.length) {
            System.err.println("[line $lineNumber] Error: Unterminated string.")
            Pair(null, line.length)
        } else {
            Pair(line.substring(start, index + 1), index + 1)
        }
    }

    // ---------------------------------------------------------------
    // NUMBER, PERCENTAGE, TIME
    // ---------------------------------------------------------------
    private fun scanNumberOrSpecial(line: String, start: Int): Pair<String, Int> {
        var index = start

        // digits
        while (index < line.length && line[index].isDigit()) index++

        // decimal part
        if (index < line.length && line[index] == '.' &&
            index + 1 < line.length && line[index + 1].isDigit()
        ) {
            index++
            while (index < line.length && line[index].isDigit()) index++
        }

        // suffix
        if (index < line.length) {
            if (line[index] == '%') return Pair(line.substring(start, index + 1), index + 1)
            if (line[index] == 's') return Pair(line.substring(start, index + 1), index + 1)
        }

        return Pair(line.substring(start, index), index)
    }

    // ---------------------------------------------------------------
    // Identifier or Keyword
    // ---------------------------------------------------------------
    private fun scanIdentifier(line: String, start: Int): Pair<String, Int> {
        var index = start + 1

        while (index < line.length &&
            (line[index].isLetterOrDigit() || line[index] == '_')
        ) index++

        return Pair(line.substring(start, index), index)
    }

    // ---------------------------------------------------------------
    // Operators
    // ---------------------------------------------------------------
    private fun isTwoCharOperator(line: String, index: Int): Boolean {
        if (index + 1 >= line.length) return false
        return line.substring(index, index + 2) in listOf(
            "<=", ">=", "==", "!=", "|>",
            "//", "/*", "*/"
        )
    }

    private fun scanTwoCharOperator(line: String, index: Int): Pair<String?, Int> {
        val op = line.substring(index, index + 2)
        if (op == "//") return Pair(op, line.length) // ignore rest
        return Pair(op, index + 2)
    }

    private fun isSingleCharOperator(c: Char): Boolean {
        return c in listOf(
            ',', ';', ':', '(', ')', '{', '}',
            '+', '-', '*', '/', '=', '<', '>', '!'
        )
    }

    // ---------------------------------------------------------------
    // Literal extraction
    // ---------------------------------------------------------------
    private fun extractLiteral(type: TokenType, lexeme: String): Any? {
        return when (type) {
            TokenType.STRING -> lexeme.removePrefix("\"").removeSuffix("\"")
            TokenType.NUMBER -> lexeme.toDouble()
            TokenType.PERCENTAGE -> lexeme.removeSuffix("%").toDouble()
            TokenType.TIME -> lexeme.removeSuffix("s").toInt()
            else -> null
        }
    }
}
