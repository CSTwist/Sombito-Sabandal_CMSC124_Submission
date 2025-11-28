// Program.kt
data class Program(
    val imports: List<Decl.ImportDecl>,
    val heroes: List<Decl.HeroDecl>,
    val arenaItems: List<Decl>, // TeamDecl, TurretDecl, CoreDecl
    val statusEffects: List<Decl.StatusEffectDecl>,
    val items: List<Decl.ItemDecl>,
    val creeps: List<Decl.CreepDecl>
)