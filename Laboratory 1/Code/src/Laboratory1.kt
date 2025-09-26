import java.util.Scanner

class Token(
    var type: String = "",
    var lexeme: String = "",
    var literal: String = "",
    var line: String = ""
)

fun main() {
    val scanner = Scanner(System.`in`)
    val myList = mutableListOf<Token>()

    println("Enter line(s) of code (press Enter on an empty line to stop):")

    var lineNumber = 1
    var multiLineComment = false
    while (true) {
        val line = scanner.nextLine()
        if (line.isBlank()) break

        var index = 0
        val tokensInLine = mutableListOf<String>()

        while (index < line.length) {
            val c = line[index]

            when {
                // whitespace
                c.isWhitespace() -> index++

                // string literal
                c == '"' -> {
                    val start = index
                    index++
                    while (index < line.length && line[index] != '"') index++
                    if (index < line.length) index++ // consume closing "
                    tokensInLine.add(line.substring(start, index))
                }

                // two-character operators
                index + 1 < line.length && (line.substring(index, index + 2) in listOf("<=", ">=", "==", "//", "/*", "*/")) -> {
                    tokensInLine.add(line.substring(index, index + 2))
                    index += 2
                }

                // single-character operators and delimiters
                c in listOf(',', ';', '.', '(', ')', '+', '-', '*', '/', '<', '>','=') -> {
                    tokensInLine.add(c.toString())
                    index++
                }

                // number (integer or decimal)
                c.isDigit() || (c == '.' && index + 1 < line.length && line[index + 1].isDigit()) -> {
                    val start = index
                    var hasDot = false
                    if (c == '.') {
                        hasDot = true
                        index++
                    }
                    while (index < line.length && line[index].isDigit()) index++
                    if (index < line.length && line[index] == '.' && !hasDot) {
                        hasDot = true
                        index++
                        while (index < line.length && line[index].isDigit()) index++
                    }
                    tokensInLine.add(line.substring(start, index))
                }

                // identifier/keyword
                c.isLetter() -> {
                    val start = index
                    while (index < line.length && line[index].isLetterOrDigit()) index++
                    tokensInLine.add(line.substring(start, index))
                }

                else -> {
                    // unknown character
                    tokensInLine.add(c.toString())
                    index++
                }
            }
        }

        for (lexeme in tokensInLine) {
            val type = when (lexeme) {
                "var" -> "VAR"
                "val" -> "VAL"
                "," -> "COMMA"
                ";" -> "SEMICOLON"
                "." -> "PERIOD"
                "=" -> "EQUAL"
                "==" -> "RELATIONAL_OPERATOR"
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
                "//", "/*", "*/" -> "COMMENT"
                "if" -> "IF_CONDITIONAL"
                "else" -> "ELSE_CONDITIONAL"
                "while" -> "WHILE_LOOP"
                "for" ->  "FOR_LOOP"
                "fun" -> "FUNCTION_DECLARATION"
                "return" -> "RETURN_CALL"
                "nil", "null" -> "NULL"
                "break", "continue" -> "LOOP_CONTROL"
                "print" -> "FUNCTION"
                "and", "or", "not", "!", "&&", "||" -> "LOGICAL_OPERATORS"
                "true", "false" -> "BOOLEAN"
                else -> when {
                    lexeme.startsWith("\"") && lexeme.endsWith("\"") -> "STRING"
                    isNumber(lexeme) -> "NUMBER"
                    else -> "IDENTIFIER"
                }
            }

            val literal = when (type) {
                "NUMBER" -> lexeme
                "STRING" -> lexeme
                else -> "null"
            }

            if (lexeme == "/*") {
                multiLineComment = true
            } else if (lexeme == "*/") {
                multiLineComment = false
            } else if (lexeme == "//") {
                break
            }

            if (type != "COMMENT" && !multiLineComment) {
                myList.add(
                    Token(
                        type = type,
                        lexeme = lexeme,
                        literal = literal,
                        line = lineNumber.toString()
                    )
                )
            }
        }

        lineNumber++
    }

    println("\nAll tokens read:")
    for (t in myList) {
        println("Token(Type=${t.type}, Lexeme=${t.lexeme}, Literal=${t.literal}, Line=${t.line})")
    }
    println("Token(Type=EOF, Lexeme=, Literal=null, Line=${lineNumber - 1})")
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
    return digitSeen
}