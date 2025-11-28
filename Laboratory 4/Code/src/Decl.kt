// Decl.kt
sealed interface Decl {

    /** import IDENTIFIER ; */
    data class ImportDecl(val name: Token) : Decl

    /** const type IDENTIFIER = expression */
    data class ConstDecl(val type: Token?, val name: Token, val value: Expr) : Decl

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

    /** core IDENTIFIER { stat_entries } */
    data class CoreDecl(
        val name: Token,
        val stats: List<StatEntry>
    ) : Decl

    /** const team IDENTIFIER */
    data class TeamDecl(val name: Token) : Decl

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
}

// Hero-specific statements
sealed interface HeroStatement {
    data class SetStmt(val name: Token, val value: Expr) : HeroStatement
    data class HeroStatBlock(val stats: List<StatEntry>) : HeroStatement
    data class ScalingCall(val param1: Token, val param2: Token) : HeroStatement
    data class AbilitiesBlock(val abilities: List<AbilityDecl>) : HeroStatement
}

// Ability declaration
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
    data class BehaviorField(val pipeline: PipelineExpr) : AbilityField
}

// Behavior pipeline
data class PipelineExpr(val calls: List<FunctionCall>)

data class FunctionCall(
    val name: Token,
    val arguments: List<Argument>
)

sealed interface Argument {
    data class NamedArg(val name: Token, val value: Expr) : Argument
    data class PositionalArg(val value: Expr) : Argument
}

// Status effect fields
sealed interface StatusEffectField {
    data class TypeField(val value: Token) : StatusEffectField
    data class DurationField(val value: Expr) : StatusEffectField
    data class OnApplyField(val block: BlockStmt) : StatusEffectField
    data class OnTickField(val block: BlockStmt) : StatusEffectField
}

// Item fields
sealed interface ItemField {
    data class PropertyField(val name: Token, val value: Expr) : ItemField
    data class EffectField(val pipeline: PipelineExpr) : ItemField
}

// Statements
sealed interface Stmt {
    data class ApplyStmt(val call: FunctionCall, val target: TargetExpr) : Stmt
    data class PipelineStmt(val pipeline: PipelineExpr) : Stmt
    data class SetStmt(val name: Token, val value: Expr) : Stmt
    data class StatEntryStmt(val entry: StatEntry) : Stmt
    data class FunctionCallStmt(val call: FunctionCall) : Stmt
}

data class BlockStmt(val statements: List<Stmt>)

sealed interface TargetExpr {
    object Self : TargetExpr
    object Target : TargetExpr
    object Caster : TargetExpr
    data class Named(val name: Token) : TargetExpr
}

// Stat entry: IDENTIFIER : expression
data class StatEntry(val name: Token, val value: Expr)