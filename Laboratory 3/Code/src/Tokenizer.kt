// Tokenizer.kt
object Tokenizer {

    // ENTRY POINT
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

    // --- NEW: return tokens for the parser (no printing) ---
    fun tokenizeToTokens(lines: List<String>): List<Token> {
        val out = mutableListOf<Token>()
        var multiLineComment = false
        var lineNumber = 1

        for (line in lines) {
            val lexemes = scanLine(line, lineNumber)
            var i = 0
            while (i < lexemes.size) {
                val lex = lexemes[i]

                // block comment state
                if (lex == "/*") { multiLineComment = true; i++; continue }
                if (lex == "*/") { multiLineComment = false; i++; continue }
                if (multiLineComment) { i++; continue }

                // Delegate classification to global classifyLexeme (TokenType.kt)
                val type = classifyLexeme(lex)
                val literal = extractLiteral(type, lex)
                out.add(Token(type, lex, literal, lineNumber))
                i++
            }
            lineNumber++
        }

        // Always end with EOF on the last processed line
        out.add(Token(TokenType.EOF, "", null, (lineNumber - 1).coerceAtLeast(1)))

        if (multiLineComment) {
            System.err.println("[line ${lineNumber - 1}] Error: Unterminated block comment (missing */)")
        }

        return out
    }

    // Scan one line into raw lexemes
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

    // Classify lexemes → tokens and print
    private fun classifyAndPrintTokens(
        tokensInLine: MutableList<String>,
        lineNumber: Int,
        startingCommentState: Boolean
    ): Boolean {
        var multiLineComment = startingCommentState
        var i = 0
        while (i < tokensInLine.size) {
            val lexeme = tokensInLine[i]

            // comment state machine
            if (lexeme == "/*") { multiLineComment = true; i++; continue }
            if (lexeme == "*/") { multiLineComment = false; i++; continue }

            val type = classifyLexeme(lexeme) // global function
            val literal = extractLiteral(type, lexeme)

            if (!multiLineComment) {
                println("Token(Type=$type, Lexeme=$lexeme, Literal=$literal, Line Number=$lineNumber)")
            }
            i++
        }
        return multiLineComment
    }

    // Extract literal value (if any)
    private fun extractLiteral(type: TokenType, lexeme: String): String? = when (type) {
        TokenType.STRING -> lexeme.removePrefix("\"").removeSuffix("\"")
        TokenType.NUMBER -> lexeme
        else -> null
    }

    // Print EOF once at end of block
    private fun printEOF(lastLine: Int) {
        val eof = Token(TokenType.EOF, "", null, lastLine)
        println("Token(type=EOF, lexeme= NULL, literal= NULL, Line Number=$lastLine)")
    }

    private fun isNumber(s: String): Boolean {
        if (s.isEmpty()) return false
        var dotSeen = false
        var digitSeen = false
        for (i in s.indices) {
            val c = s[i]
            when {
                c.isDigit() -> digitSeen = true
                c == '.' -> { if (dotSeen) return false; dotSeen = true }
                else -> return false
            }
        }
        return digitSeen && (s != ".")
    }

    // --- String literal ---
    private fun scanString(line: String, start: Int, lineNumber: Int): Pair<String?, Int> {
        var index = start + 1
        while (index < line.length && line[index] != '"') {
            // naive: skip escapes by jumping two chars when backslash is seen
            if (line[index] == '\\' && index + 1 < line.length) { index += 2; continue }
            index++
        }
        return if (index >= line.length) {
            System.err.println("[line $lineNumber] Error: Unterminated string")
            Pair(null, line.length) // skip rest of line
        } else {
            index++ // consume closing "
            Pair(line.substring(start, index), index)
        }
    }

    // --- Two-character (and more) operators ---
    private fun isTwoCharOperator(line: String, index: Int): Boolean {
        if (index + 1 >= line.length) return false
        val two = line.substring(index, index + 2)
        if (two in listOf(
                "<=", ">=", "==", "!=", "&&", "||",
                "//", "/*", "*/",
                "++", "+=", "-=", "*=", "/=", "%="
            )) return true
        return false
    }

    private fun scanTwoCharOperator(line: String, start: Int): Pair<String?, Int> {
        val two = line.substring(start, start + 2)
        return when (two) {
            "//" -> Pair(null, line.length) // single-line comment: ignore rest of the line
            else -> Pair(two, start + 2)
        }
    }

    // --- Single-character operators ---
    private fun isSingleCharOperator(c: Char): Boolean {
        return c in listOf(
            ',', ';', '.', '(', ')', '{', '}', // delimiters
            '+', '-', '*', '/', '%',           // arithmetic (added %)
            '<', '>', '=', '!'                 // comparison/assignment/bang
        )
    }

    // --- Numbers ---
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

    // --- Identifiers / keywords ---
    private fun scanIdentifier(line: String, start: Int): Pair<String, Int> {
        var index = start + 1
        while (index < line.length && (line[index].isLetterOrDigit() || line[index] == '_')) index++
        return Pair(line.substring(start, index), index)
    }
}
