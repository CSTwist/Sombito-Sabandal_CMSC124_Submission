// TokenType.kt
enum class TokenType {
    // Punctuation
    COMMA, SEMICOLON, COLON,
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,

    // Operators
    PLUS, MINUS, STAR, DIVIDE,
    EQUAL, EQUAL_EQUAL,
    BANG_EQUAL,
    LESS, LESS_EQUAL, GREATER, GREATER_EQUAL,
    PIPE_GREATER, // |>

    // Keywords - Top Level
    GAME, HEROES, ARENA, STATUS_EFFECTS, ITEMS, CREEPS,
    IMPORT, CONST,

    // Keywords - Hero
    HERO, HERO_STAT, SCALING, ABILITIES, SET,

    // Keywords - Ability
    ABILITY, TYPE, COOLDOWN, MANA_COST, RANGE, DAMAGE_TYPE, BEHAVIOR,

    // Keywords - Arena
    TEAM, TURRET, CORE,

    // Keywords - Status Effect
    STATUS_EFFECT, DURATION, ON_APPLY, ON_TICK,

    // Keywords - Item
    ITEM, EFFECT,

    // Keywords - Creep
    CREEP,

    // Keywords - Behavior/Targets
    APPLY, TO, SELF, TARGET, CASTER,

    // Literals
    STRING, NUMBER, IDENTIFIER,
    PERCENTAGE, TIME,

    // Special
    EOF, INVALID
}

fun classifyLexeme(lexeme: String): TokenType {
    return when (lexeme) {
        // Punctuation
        "," -> TokenType.COMMA
        ";" -> TokenType.SEMICOLON
        ":" -> TokenType.COLON
        "(" -> TokenType.LEFT_PAREN
        ")" -> TokenType.RIGHT_PAREN
        "{" -> TokenType.LEFT_BRACE
        "}" -> TokenType.RIGHT_BRACE

        // Operators
        "+" -> TokenType.PLUS
        "-" -> TokenType.MINUS
        "*" -> TokenType.STAR
        "/" -> TokenType.DIVIDE
        "=" -> TokenType.EQUAL
        "==" -> TokenType.EQUAL_EQUAL
        "!=" -> TokenType.BANG_EQUAL
        "<" -> TokenType.LESS
        "<=" -> TokenType.LESS_EQUAL
        ">" -> TokenType.GREATER
        ">=" -> TokenType.GREATER_EQUAL
        "|>" -> TokenType.PIPE_GREATER

        // Keywords - Top Level
        "GAME" -> TokenType.GAME
        "Heroes" -> TokenType.HEROES
        "Arena" -> TokenType.ARENA
        "StatusEffects" -> TokenType.STATUS_EFFECTS
        "Items" -> TokenType.ITEMS
        "Creeps" -> TokenType.CREEPS
        "import" -> TokenType.IMPORT
        "const" -> TokenType.CONST

        // Keywords - Hero
        "hero" -> TokenType.HERO
        "heroStat" -> TokenType.HERO_STAT
        "scaling" -> TokenType.SCALING
        "abilities" -> TokenType.ABILITIES
        "set" -> TokenType.SET

        // Keywords - Ability
        "ability" -> TokenType.ABILITY
        "type" -> TokenType.TYPE
        "cooldown" -> TokenType.COOLDOWN
        "mana_cost" -> TokenType.MANA_COST
        "range" -> TokenType.RANGE
        "damage_type" -> TokenType.DAMAGE_TYPE
        "behavior" -> TokenType.BEHAVIOR

        // Keywords - Arena
        "team" -> TokenType.TEAM
        "turret" -> TokenType.TURRET
        "core" -> TokenType.CORE

        // Keywords - Status Effect
        "statusEffect" -> TokenType.STATUS_EFFECT
        "duration" -> TokenType.DURATION
        "on_apply" -> TokenType.ON_APPLY
        "on_tick" -> TokenType.ON_TICK

        // Keywords - Item
        "item" -> TokenType.ITEM
        "effect" -> TokenType.EFFECT

        // Keywords - Creep
        "creep" -> TokenType.CREEP

        // Keywords - Behavior/Targets
        "apply" -> TokenType.APPLY
        "to" -> TokenType.TO
        "self" -> TokenType.SELF
        "target" -> TokenType.TARGET
        "caster" -> TokenType.CASTER

        else -> when {
            // Percentage (e.g., "50%")
            lexeme.matches(Regex("^[0-9]+%$")) -> TokenType.PERCENTAGE

            // Time (e.g., "5s")
            lexeme.matches(Regex("^[0-9]+s$")) -> TokenType.TIME

            // Numeric literals
            lexeme.matches(Regex("^[0-9]+(\\.[0-9]+)?$")) -> TokenType.NUMBER

            // String literals (double-quoted)
            lexeme.matches(Regex("^\".*\"$")) -> TokenType.STRING

            // Valid identifiers
            lexeme.matches(Regex("^[a-zA-Z_][a-zA-Z0-9_]*$")) -> TokenType.IDENTIFIER

            else -> TokenType.INVALID
        }
    }
}