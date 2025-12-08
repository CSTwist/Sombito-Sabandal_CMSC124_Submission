// TokenType.kt
// TokenType.kt
//
// Complete list of tokens used by the DSL, consistent with Parser, Evaluator, Tokenizer, and AST.
//

enum class TokenType {

    // ---------------------------------------------------------
    // Single-character symbols
    // ---------------------------------------------------------
    LEFT_PAREN, RIGHT_PAREN,
    LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, COLON, SEMICOLON,
    PLUS, MINUS, STAR, DIVIDE,
    BANG, QUESTION,
    EQUAL, GREATER, LESS,

    // ---------------------------------------------------------
    // Multi-character operators
    // ---------------------------------------------------------
    EQUAL_EQUAL, BANG_EQUAL,
    GREATER_EQUAL, LESS_EQUAL,
    PIPE_GREATER,          // |>

    // ---------------------------------------------------------
    // Literals
    // ---------------------------------------------------------
    IDENTIFIER,
    STRING,                 // "text"
    NUMBER,                 // 10, 20.5
    PERCENTAGE,             // 50%
    TIME,                   // 5s

    // ---------------------------------------------------------
    // Keywords
    // ---------------------------------------------------------
    GAME,
    IMPORT,

    // Variables
    SET,
    CONST,

    // Logic
    AND,
    OR,
    IF,
    ELSE,
    WHILE,
    FOR,
    IN,
    RETURN,

    // Function-related
    FUNCTION,
    FUNCTIONS,
    APPLY,
    TO,

    // Hero system
    HEROES,
    HERO,
    HERO_STAT,
    ABILITIES,
    ABILITY,

    // Arena system
    ARENA,
    TEAM,
    TURRET,
    TURRETS,
    CORE,

    // Status effects
    STATUS_EFFECTS,
    STATUS_EFFECT,
    TYPE,
    DURATION,
    ON_APPLY,
    ON_TICK,
    ON_EXPIRE,

    // Items
    ITEMS,
    ITEM,
    PASSIVE,
    BEHAVIOR,
    RANGE,
    MANA_COST,
    COOLDOWN,
    DAMAGE_TYPE,

    // Creeps
    CREEPS,
    CREEP,

    // Targets
    SELF,
    TARGET,
    CASTER,

    // End of file
    EOF
}

fun classifyLexeme(lexeme: String): TokenType {
    return when (lexeme) {
        "," -> TokenType.COMMA
        ";" -> TokenType.SEMICOLON
        ":" -> TokenType.COLON
        "(" -> TokenType.LEFT_PAREN
        ")" -> TokenType.RIGHT_PAREN
        "{" -> TokenType.LEFT_BRACE
        "}" -> TokenType.RIGHT_BRACE

        "+" -> TokenType.PLUS
        "-" -> TokenType.MINUS
        "*" -> TokenType.STAR
        "/" -> TokenType.DIVIDE
        "=" -> TokenType.EQUAL
        "==" -> TokenType.EQUAL_EQUAL
        "!" -> TokenType.BANG
        "!=" -> TokenType.BANG_EQUAL
        "<" -> TokenType.LESS
        "<=" -> TokenType.LESS_EQUAL
        ">" -> TokenType.GREATER
        ">=" -> TokenType.GREATER_EQUAL
        "|>" -> TokenType.PIPE_GREATER

        "GAME" -> TokenType.GAME
        "import" -> TokenType.IMPORT

        "set" -> TokenType.SET
        "const" -> TokenType.CONST

        "if" -> TokenType.IF
        "else" -> TokenType.ELSE
        "while" -> TokenType.WHILE
        "for" -> TokenType.FOR
        "in" -> TokenType.IN
        "return" -> TokenType.RETURN

        "and" -> TokenType.AND
        "or" -> TokenType.OR

        "apply" -> TokenType.APPLY
        "to" -> TokenType.TO

        // Game blocks
        "Heroes" -> TokenType.HEROES
        "Arena" -> TokenType.ARENA
        "StatusEffects" -> TokenType.STATUS_EFFECTS
        "Items" -> TokenType.ITEMS
        "Creeps" -> TokenType.CREEPS
        "Functions" -> TokenType.FUNCTIONS

        // Hero system
        "hero" -> TokenType.HERO
        "heroStat" -> TokenType.HERO_STAT
        "abilities" -> TokenType.ABILITIES
        "ability" -> TokenType.ABILITY

        "type" -> TokenType.TYPE
        "cooldown" -> TokenType.COOLDOWN
        "mana_cost" -> TokenType.MANA_COST
        "range" -> TokenType.RANGE
        "damage_type" -> TokenType.DAMAGE_TYPE
        "behavior" -> TokenType.BEHAVIOR

        // Arena system
        "team" -> TokenType.TEAM
        "turret" -> TokenType.TURRET
        "turrets" -> TokenType.TURRETS
        "core" -> TokenType.CORE

        // Status effects
        "statusEffect" -> TokenType.STATUS_EFFECT
        "duration" -> TokenType.DURATION
        "on_apply" -> TokenType.ON_APPLY
        "on_tick" -> TokenType.ON_TICK
        "on_expire" -> TokenType.ON_EXPIRE

        // Items
        "item" -> TokenType.ITEM
        "passive" -> TokenType.PASSIVE

        // Creeps
        "creep" -> TokenType.CREEP

        // Targets
        "self" -> TokenType.SELF
        "target" -> TokenType.TARGET
        "caster" -> TokenType.CASTER

        // Functions
        "function" -> TokenType.FUNCTION

        else -> when {
            lexeme.matches(Regex("^[0-9]+%$")) -> TokenType.PERCENTAGE
            lexeme.matches(Regex("^[0-9]+s$")) -> TokenType.TIME
            lexeme.matches(Regex("^[0-9]+(\\.[0-9]+)?$")) -> TokenType.NUMBER
            lexeme.matches(Regex("^\".*\"$")) -> TokenType.STRING
            lexeme.matches(Regex("^[a-zA-Z_][a-zA-Z0-9_]*$")) -> TokenType.IDENTIFIER
            else -> TokenType.IDENTIFIER // no INVALID token type
        }
    }
}
