import java.util.Scanner

class Token(
    var type: String = "",
    var lexeme: String = "",
    var literal: String = "",
    var line: String = ""
)

fun main() {
    val scanner = Scanner(System.`in`)

    val buffer = mutableListOf<String>()          // holds multi-line user input until submit
    var lineNumber = 1
    var multiLineComment = false

    println("Enter your code. Press ENTER for a new line. Submit with an empty line. Quit with :q / :quit.")

    while (true) {
        if (!scanner.hasNextLine()) break
        val raw = scanner.nextLine()
        val cmd = raw.trim()

        // quit immediately
        if (cmd == ":q" || cmd == ":quit") break

        // end + evaluate block, then exit program
        if (raw.isBlank()) {
            if (buffer.isNotEmpty()) {
                multiLineComment = tokenizeBlock(buffer, multiLineComment)
                buffer.clear()
            }
            break   // exit after evaluation
        }

        buffer.add(raw)
        lineNumber++
    }
}

fun tokenizeBlock(lines: List<String>, startingCommentState: Boolean): Boolean {
    var multiLineComment = startingCommentState
    var lineNumber = 1

    for (line in lines) {
        var index = 0
        val tokensInLine = mutableListOf<String>()

        while (index < line.length) {
            val c = line[index]
            when {
                c.isWhitespace() -> index++
                c == '"' -> {
                    val start = index
                    index++
                    while (index < line.length && line[index] != '"') index++
                    if (index >= line.length) {
                        System.err.println("[line $lineNumber] Error: Unterminated string")
                        break
                    } else {
                        index++
                        tokensInLine.add(line.substring(start, index))
                    }
                }
                index + 1 < line.length && (line.substring(index, index + 2) in listOf(
                    "<=", ">=", "==", "!=", "&&", "||", "//", "/*", "*/"
                )) -> {
                    val op = line.substring(index, index + 2)
                    if (op == "//") break
                    tokensInLine.add(op)
                    index += 2
                }
                c in listOf(',', ';', '.', '(', ')', '+', '-', '*', '/', '<', '>', '=', '!') -> {
                    tokensInLine.add(c.toString()); index++
                }
                c.isDigit() || (c == '.' && index + 1 < line.length && line[index + 1].isDigit()) -> {
                    val start = index
                    var hasDot = false
                    if (c == '.') { hasDot = true; index++ }
                    while (index < line.length && line[index].isDigit()) index++
                    if (index < line.length && line[index] == '.' && !hasDot) {
                        if (index + 1 < line.length && line[index + 1].isDigit()) {
                            hasDot = true; index++
                            while (index < line.length && line[index].isDigit()) index++
                        }
                    }
                    tokensInLine.add(line.substring(start, index))
                }
                c.isLetter() || c == '_' -> {
                    val start = index; index++
                    while (index < line.length && (line[index].isLetterOrDigit() || line[index] == '_')) index++
                    tokensInLine.add(line.substring(start, index))
                }
                else -> { System.err.println("[line $lineNumber] Error: Unexpected character '$c'"); index++ }
            }
        }

        var i = 0
        while (i < tokensInLine.size) {
            val lexeme = tokensInLine[i]
            if (lexeme == "/*") { multiLineComment = true; i++; continue }
            else if (lexeme == "*/") { multiLineComment = false; i++; continue }

            val type = when (lexeme) {
                "var" -> "VAR"
                "val" -> "VAL"
                "," -> "COMMA"
                ";" -> "SEMICOLON"
                "." -> "PERIOD"
                "=" -> "EQUAL"
                "==" -> "EQUAL_EQUAL"
                "!" -> "BANG"
                "!=" -> "BANG_EQUAL"
                "-" -> "MINUS"
                "+" -> "PLUS"
                "/" -> "DIVIDE"
                "*" -> "STAR"
                ")" -> "RIGHT_PAREN"
                "(" -> "LEFT_PAREN"
                "<=" -> "LESS_EQUAL"
                ">=" -> "GREATER_EQUAL"
                "<" -> "LESS"
                ">" -> "GREATER"
                "&&" -> "AND_AND"
                "||" -> "OR_OR"
                "if" -> "IF_CONDITIONAL"
                "else" -> "ELSE_CONDITIONAL"
                "while" -> "WHILE_LOOP"
                "for" -> "FOR_LOOP"
                "fun" -> "FUNCTION_DECLARATION"
                "return" -> "RETURN_CALL"
                "nil", "null" -> "NULL"
                "break", "continue" -> "LOOP_CONTROL"
                "print" -> "FUNCTION"
                "and", "or", "not" -> "LOGICAL_OPERATORS"
                "true", "false" -> "BOOLEAN"
                else -> when {
                    lexeme.startsWith("\"") && lexeme.endsWith("\"") -> "STRING"
                    isNumber(lexeme) -> "NUMBER"
                    else -> "IDENTIFIER"
                }
            }

            val literal = when (type) {
                "NUMBER" -> lexeme
                "STRING" -> lexeme.removePrefix("\"").removeSuffix("\"")
                else -> "null"
            }

            if (!multiLineComment) {
                println("Token(type=$type , lexeme=$lexeme , literal=$literal , line =$lineNumber)")
            }
            i++
        }
        lineNumber++
    }

    // Single EOF once, after the whole block
    println("Token(type=EOF , lexeme= , literal=null , line =${lineNumber - 1})")

    if (multiLineComment) {
        System.err.println("[line ${lineNumber - 1}] Error: Unterminated block comment (missing */)")
    }
    return multiLineComment
}

fun isNumber(s: String): Boolean {
    if (s.isEmpty()) return false
    var dotSeen = false
    var digitSeen = false
    for (i in s.indices) {
        val c = s[i]
        when {
            c.isDigit() -> digitSeen = true
            c == '.' -> {
                if (dotSeen) return false
                dotSeen = true
            }
            else -> return false
        }
    }
    return digitSeen && (s != ".")
}
