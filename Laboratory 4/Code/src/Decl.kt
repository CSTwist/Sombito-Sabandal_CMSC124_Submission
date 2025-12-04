// Decl.kt
sealed interface Decl {

    /** import IDENTIFIER ; */
    data class ImportDecl(val name: Token) : Decl

    /** const type IDENTIFIER = expression */
    data class ConstDecl(val type: Token?, val name: Token, val value: Expr) : Decl

    /** set IDENTIFIER = expression */
    data class VarDecl(val name: Token, val value: Expr) : Decl

    /** hero IDENTIFIER { hero_body } */
    data class HeroDecl(
        val name: Token,
        val statements: List<HeroStatement>
    ) : Decl

    /** turret IDENTIFIER { stat_entries } */
    data class TurretDecl(
        val name: Token,
        val stats: List<StatEntry>
    ) : Decl

    /** core IDENTIDENTIFIER { stat_entries } */
    data class CoreDecl(
        val name: Token,
        val stats: List<StatEntry>
    ) : Decl

    /** team IDENTIFIER { [core ref] [turrets block] } */
    data class TeamDecl(
        val name: Token,
        val coreRef: Token?,
        val turrets: List<TurretDecl>
    ) : Decl

    /** statusEffect IDENTIFIER { status_effect_body } */
    data class StatusEffectDecl(
        val name: Token,
        val fields: List<StatusEffectField>
    ) : Decl

    /** item IDENTIFIER { item_body } */
    data class ItemDecl(
        val name: Token,
        val fields: List<ItemField>
    ) : Decl

    /** creep IDENTIFIER { stat_entries } */
    data class CreepDecl(
        val name: Token,
        val stats: List<StatEntry>
    ) : Decl

    /** function IDENTIFIER (params) : type { body } */
    data class FunctionDecl(
        val name: Token,
        val params: List<Param>,
        val returnType: Token?, // e.g. Number, Boolean
        val body: BlockStmt
    ) : Decl
}

data class Param(val type: Token, val name: Token)

// ========================= HERO STATEMENTS =========================

sealed interface HeroStatement {

    data class SetStmt(val name: Token, val value: Expr) : HeroStatement

    data class HeroStatBlock(val stats: List<StatEntry>) : HeroStatement

    data class AbilitiesBlock(val abilities: List<AbilityDecl>) : HeroStatement
}


// ========================= ABILITIES =========================

data class AbilityDecl(
    val name: Token,
    val fields: List<AbilityField>
)

sealed interface AbilityField {

    data class TypeField(val value: Token) : AbilityField

    data class CooldownField(val value: Expr) : AbilityField

    data class ManaCostField(val value: Expr) : AbilityField

    data class RangeField(val value: Expr) : AbilityField

    data class DamageTypeField(val value: Token) : AbilityField

    /**
     * Behavior: Can be a block or a pipeline expression
     */
    data class BehaviorField(
        val body: BlockStmt?, // If it's a script block
        val expression: Expr? // If it's a pipeline expression
    ) : AbilityField
}


// ========================= STATUS EFFECT =========================

sealed interface StatusEffectField {

    data class TypeField(val value: Token) : StatusEffectField

    data class DurationField(val value: Expr) : StatusEffectField

    data class OnApplyField(val block: BlockStmt) : StatusEffectField

    data class OnTickField(val block: BlockStmt) : StatusEffectField

    data class OnExpireField(val block: BlockStmt) : StatusEffectField
}


// ========================= ITEM =========================

sealed interface ItemField {

    data class PropertyField(val name: Token, val value: Expr) : ItemField

    /**
     * passive: { behavior: ... }
     * Uses a pipeline expression
     */
    data class PassiveField(val behavior: Expr) : ItemField
}


// ========================= STATEMENTS =========================

sealed interface Stmt {

    data class ApplyStmt(val call: FunctionCall, val target: TargetExpr) : Stmt

    data class StatEntryStmt(val entry: StatEntry) : Stmt

    data class SetStmt(val name: Token, val value: Expr) : Stmt

    data class ConstDeclStmt(val type: Token, val name: Token, val value: Expr) : Stmt

    data class FunctionCallStmt(val call: FunctionCall) : Stmt

    data class ExprStmt(val expr: Expr) : Stmt

    // Control flow
    data class IfStmt(val condition: Expr, val thenBranch: BlockStmt, val elseBranch: BlockStmt?) : Stmt

    data class WhileStmt(val condition: Expr, val body: BlockStmt) : Stmt

    data class ForStmt(val variable: Token, val collection: Expr, val body: BlockStmt) : Stmt

    data class ReturnStmt(val keyword: Token, val value: Expr?) : Stmt
}


// A block contains List<Stmt>
data class BlockStmt(val statements: List<Stmt>)


// ========================= FUNCTION CALLS =========================

data class FunctionCall(
    val name: Token,
    val arguments: List<Argument>
)

sealed interface Argument {

    data class NamedArg(val name: Token, val value: Expr) : Argument

    data class PositionalArg(val value: Expr) : Argument
}


// ========================= TARGET EXPRESSIONS =========================

sealed interface TargetExpr {
    object Self : TargetExpr
    object Target : TargetExpr
    object Caster : TargetExpr
    data class Named(val name: Token) : TargetExpr
}


// ========================= MISC =========================

data class StatEntry(val name: Token, val value: Expr)