// TokenType.kt
enum class TokenType {
    // Punctuation
    COMMA, SEMICOLON, COLON,
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,

    // Operators
    PLUS, MINUS, STAR, DIVIDE,
    EQUAL, EQUAL_EQUAL,
    BANG, BANG_EQUAL,
    LESS, LESS_EQUAL, GREATER, GREATER_EQUAL,
    PIPE_GREATER, // |>
    AND, OR,

    // Keywords - Top Level
    GAME, HEROES, ARENA, STATUS_EFFECTS, ITEMS, CREEPS, FUNCTIONS,
    IMPORT, CONST, SET,

    // Keywords - Hero
    HERO, HERO_STAT, SCALING, ABILITIES,

    // Keywords - Ability
    ABILITY, TYPE, COOLDOWN, MANA_COST, RANGE, DAMAGE_TYPE, BEHAVIOR,

    // Keywords - Arena
    TEAM, TURRET, TURRETS, CORE,

    // Keywords - Status Effect
    STATUS_EFFECT, DURATION, ON_APPLY, ON_TICK, ON_EXPIRE,

    // Keywords - Item
    ITEM, EFFECT, PASSIVE,

    // Keywords - Creep
    CREEP,

    // Keywords - Behavior/Targets
    APPLY, TO, SELF, TARGET, CASTER, FUNCTION,

    // Keywords - Control Flow
    IF, ELSE,
    WHILE, FOR, IN,
    RETURN,

    // Literals
    STRING, NUMBER, IDENTIFIER,
    PERCENTAGE, TIME,

    // Special
    EOF, INVALID
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
        "Heroes" -> TokenType.HEROES
        "Arena" -> TokenType.ARENA
        "StatusEffects" -> TokenType.STATUS_EFFECTS
        "Items" -> TokenType.ITEMS
        "Creeps" -> TokenType.CREEPS
        "Functions" -> TokenType.FUNCTIONS
        "import" -> TokenType.IMPORT
        "const" -> TokenType.CONST
        "set" -> TokenType.SET

        "hero" -> TokenType.HERO
        "heroStat" -> TokenType.HERO_STAT
        "scaling" -> TokenType.SCALING
        "abilities" -> TokenType.ABILITIES

        "ability" -> TokenType.ABILITY
        "type" -> TokenType.TYPE
        "cooldown" -> TokenType.COOLDOWN
        "mana_cost" -> TokenType.MANA_COST
        "range" -> TokenType.RANGE
        "damage_type" -> TokenType.DAMAGE_TYPE
        "behavior" -> TokenType.BEHAVIOR

        "team" -> TokenType.TEAM
        "turret" -> TokenType.TURRET
        "turrets" -> TokenType.TURRETS
        "core" -> TokenType.CORE

        "statusEffect" -> TokenType.STATUS_EFFECT
        "duration" -> TokenType.DURATION
        "on_apply" -> TokenType.ON_APPLY
        "on_tick" -> TokenType.ON_TICK
        "on_expire" -> TokenType.ON_EXPIRE

        "item" -> TokenType.ITEM
        "effect" -> TokenType.EFFECT
        "passive" -> TokenType.PASSIVE

        "creep" -> TokenType.CREEP

        "apply" -> TokenType.APPLY
        "to" -> TokenType.TO
        "self" -> TokenType.SELF
        "target" -> TokenType.TARGET
        "caster" -> TokenType.CASTER
        "function" -> TokenType.FUNCTION

        "if" -> TokenType.IF
        "else" -> TokenType.ELSE
        "while" -> TokenType.WHILE
        "for" -> TokenType.FOR
        "in" -> TokenType.IN
        "return" -> TokenType.RETURN

        "and" -> TokenType.AND
        "or" -> TokenType.OR

        else -> when {
            lexeme.matches(Regex("^[0-9]+%$")) -> TokenType.PERCENTAGE
            lexeme.matches(Regex("^[0-9]+s$")) -> TokenType.TIME
            lexeme.matches(Regex("^[0-9]+(\\.[0-9]+)?$")) -> TokenType.NUMBER
            lexeme.matches(Regex("^\".*\"$")) -> TokenType.STRING
            lexeme.matches(Regex("^[a-zA-Z_][a-zA-Z0-9_]*$")) -> TokenType.IDENTIFIER
            else -> TokenType.INVALID
        }
    }
}