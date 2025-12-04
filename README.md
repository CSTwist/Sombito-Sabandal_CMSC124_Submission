# MOBA Unity Engine DSL 

## Creator

[Sherwin Paul Sabandal (itsShiii16), Chakinzo Sombito (CSTwist)]

## Language Overview
MOBA Unity Engine DSL (often shortened to MOBA DSL) is a domain-specific language for defining complete MOBA-style games on top of the Unity C# engine.

It is designed so non-programmers (e.g., designers, game balancers) can describe heroes, abilities, items, creeps, arenas, and status effects in a declarative, designer-friendly way while the underlying engine handles execution.

<h1 align="center"> WHAT'S UNIQUE</h1>

Abilities are described as pipelines of effects using the |> operator. Each stage is a function-like step (e.g., single_target, deal, apply_status), making complex behaviors easy to read and compose.



**EXAMPLE:**
```
ability Fireball {
    type: SINGLE_TARGET
    cooldown: 8s
    mana_cost: 60
    damage_type: MAGIC
    range: 800

    behavior:
        single_target(Affects: ENEMY)
        |> deal(base: 70, scaling: 60% AP)
        |> apply_status(Burn)
}
```

**SECTION-BASED GAME DEFINITION**<br>
The game is organized into semantic sections inside a single GAME block. Each section focuses on one domain: heroes, arena, status effects, items, and creeps.

```
GAME MyFirstMoba {
    Heroes { ... }
    Arena { ... }
    StatusEffects { ... }
    Items { ... }
    Creeps { ... }
}

```

## Keywords
| Keyword          | Description                                                 |
| ---------------- | ----------------------------------------------------------- |
| `GAME`           | Declares a full game configuration block                    |
| `Heroes`         | Section for defining all heroes                             |
| `Arena`          | Section for defining teams, cores, turrets, and map setup   |
| `StatusEffects`  | Section for defining buffs and debuffs                      |
| `Items`          | Section for defining all items                              |
| `Creeps`         | Section for defining lane/neutral creeps                    |
| `set`            | Declare a mutable variable                                  |
| `const`          | Declare an immutable variable                               |
| `Number`         | Primitive numeric type                                      |
| `Boolean`        | Primitive logical type                                      |
| `String`         | Primitive text type                                         |
| `Duration`       | Primitive time type                                         |
| `Percentage`     | Primitive percentage type                                   |
| `Vector`         | Primitive vector type (position/direction)                  |
| `Entity`         | Base type for all in-game entities                          |
| `hero`           | Declare a hero inside `Heroes`                              |
| `creep`          | Declare a creep inside `Creeps`                             |
| `turret`         | Declare a turret inside `Arena`                             |
| `core`           | Declare a core/base inside `Arena`                          |
| `team`           | Declare a team inside `Arena`                               |
| `item`           | Declare an item inside `Items`                              |
| `ability`        | Declare an ability inside a hero or item                    |
| `statusEffect`   | Declare a status effect inside `StatusEffects`              |
| `type`           | Ability/status field for type classification                |
| `cooldown`       | Ability field for cooldown duration                         |
| `mana_cost`      | Ability field for mana/resource cost                        |
| `damage_type`    | Ability field for damage type (`PHYSICAL`, `MAGIC`, `PURE`) |
| `range`          | Ability field for casting/target range                      |
| `behavior`       | Declares the behavior pipeline of an ability or passive     |
| `duration`       | Duration of a status effect or timed behavior               |
| `on_cast`        | Hook: triggers when an ability is cast                      |
| `on_hit`         | Hook: triggers when an attack/ability hits                  |
| `on_tick`        | Hook: triggers periodically while an effect is active       |
| `on_expire`      | Hook: triggers when a status effect ends                    |
| `apply_status`   | Apply one or more status effects to a target                |
| `remove_status`  | Remove one or more status effects from a target             |
| `knockback`      | Apply a knockback displacement                              |
| `stun`           | Stun the target                                             |
| `slow`           | Slow the target                                             |
| `silence`        | Silence the target                                          |
| `root`           | Root the target (no movement)                               |
| `shield`         | Apply a shield that absorbs damage                          |
| `projectile`     | Spawn/configure a projectile                                |
| `dash`           | Move the caster in some direction or to a target            |
| `teleport`       | Instantly move an entity to a position                      |
| `spawn`          | Spawn an entity (e.g., creep, minion, object)               |
| `summon`         | Summon a controllable or timed entity                       |
| `play_vfx`       | Play a visual effect                                        |
| `play_sfx`       | Play a sound effect                                         |
| `target`         | Contextual reference to the current target                  |
| `caster`         | Contextual reference to the entity casting an ability       |
| `self`           | Contextual reference to the effect owner                    |
| `ally` / `enemy` | Target designators in behavior calls                        |
| `ALLY`           | Target category: allied units                               |
| `ENEMY`          | Target category: enemy units                                |
| `NEUTRAL`        | Target category: neutral units                              |
| `PHYSICAL`       | Damage type: physical damage                                |
| `MAGIC`          | Damage type: magic damage                                   |
| `PURE`           | Damage type: pure/true damage                               |
| `BUFF`           | Status type: beneficial effect                              |
| `DEBUFF`         | Status type: harmful effect                                 |
| `SINGLE_TARGET`  | Ability type: single target                                 |
| `AOE`            | Ability type: area of effect                                |
| `PASSIVE`        | Ability type: passive/always-on                             |
| `scaling`        | DSL helper for describing scaling formulas                  |


## Operators
| Type                  | Operators / Usage       |   
| --------------------- | ----------------------- |
| Arithmetic            | `+ - * / %`             |  
| Relational            | `< <= > >= == !=`       |  
| Logical (symbolic)    | `! &&                   | 
| Assignment            | `=  +=  -=  *=  /=  %=` | 
| Increment & Decrement | `++  --`                |  
| Conditional Ternary   | `?:`                    |
| Pipeline              | `                       | 
| Comma                 | `,`                     |                        


## Literals
| Type                | Description                                                                     |
| ------------------- | ------------------------------------------------------------------------------- |
| Numbers             | **Integer:** `0`, `1`, `25`, `300`   /   **Float:** `0.5`, `3.14`, `10.0`       |
| Strings             | Enclosed in double quotes, e.g., `"Pyromancer"`, `"Basic Attack"`, `"Fireball"` |
| Booleans            | `True`, `False`                                                                 |
| Duration literals   | `<number>s` — e.g., `3s`, `10s`, `60s`                                          |
| Percentage literals | `<number>%` — e.g., `10%`, `30%`, `100%`                                        |
| Null                | Represents “no value”. (Implementation typically uses `null` in code.)          |

## Identifiers
1) Can contain letters, digits, and underscores (_).
2) Must not start with a digit.
3) Must not be any reserved keyword.
4) Identifiers are case-sensitive (Pyromancer, pyromancer, and PYROMANCER are all different).
x
Examples:

Pyromancer, Fireball, LightMeleeCreep

attackDamage, moveSpeed, heroLevel

## Comments

Single-line:
// This is a single-line comment

Block:
/* This is a
   multi-line comment */
   
Nested block comments are not supported.

## Syntax Style
Whitespace is not significant except to separate tokens.

Sections and game structure are defined inside a single GAME block.

Blocks are enclosed in braces { ... }.

Object-like data (e.g., stats) use key: value pairs inside { ... }.

Behavior pipelines are written top-to-bottom using |>.
## Sample Code
```
import Maps;

GAME MyFirstMoba {

  set initialHeroLevel = 1

  Heroes {

    hero Pyromancer {

      heroStat: {
        level: initialHeroLevel
        hp: 500
        mana: 300
        attackDamage: 55
        abilityPower: 80
        armor: 20
        magicResist: 30
        moveSpeed: 335
      }

      abilities: {

        ability Fireball {
          type: SINGLE_TARGET
          cooldown: 8s
          mana_cost: 60
          damage_type: MAGIC
          range: 800

          behavior:
            single_target(Affects: ENEMY)
            |> deal(base: 70, scaling: 60% AP)
            |> apply_status(Burn)
        }

        ability Recover {
          type: SINGLE_TARGET
          cooldown: 12s
          mana_cost: 50
          damage_type: MAGIC
          range: 600

          behavior:
            single_target(Affects: ALLY, IncludeSelf: true)
            |> deal(base: 0, scaling: 0% AP)  // no damage, just consistency
            |> apply_status(Regeneration)
        }

      }

    }

  }

  Arena {

    team Light {
      core CoreOfLight

      turrets: {
        turret LightOuterTop {
          hp: 2000
          attackDamage: 160
          attackRange: 750
        }
      }
    }

    team Dark {
      core CoreOfDark

      turrets: {
        turret DarkOuterTop {
          hp: 2000
          attackDamage: 160
          attackRange: 750
        }
      }
    }

  }

  StatusEffects {

    statusEffect Burn {
      type: DEBUFF
      duration: 3s

      on_apply: {
        apply DealDamage(10) to target
      }

      on_tick: {
        apply DealDamage(10) to target
      }
    }

    statusEffect Regeneration {
      type: BUFF
      duration: 5s

      on_apply: {
        apply Heal(10) to target
      }

      on_tick: {
        apply Heal(15) to target
      }
    }

  }

  Creeps {

    creep LightMeleeCreep {
      team: Light
      hp: 400
      attackDamage: 30
      moveSpeed: 325
    }

    creep DarkMeleeCreep {
      team: Dark
      hp: 400
      attackDamage: 30
      moveSpeed: 325
    }

  }

  Items {

    item SorcerersTorch {
      cost: 900

      stats: {
        abilityPower: 60
      }

      passive: {
        behavior:
          on_hit(target: ENEMY)
          |> apply_status(Burn)
      }
    }

  }

}

```
## Design Rationale

Domain-specific focus:
The language is built around MOBA concepts (heroes, abilities, items, status effects, creeps, arena). This keeps the mental model close to how designers think.

Pipeline-based behaviors:
The |> operator encourages composing complex behaviors from small, reusable steps (single_target, deal, apply_status, etc.).

Sectioned layout:
Separating Heroes, Arena, StatusEffects, Items, and Creeps improves readability and makes it easier to find and modify specific parts of the game.

Data-first, logic-light:
Most of the DSL is declarative — numbers, flags, and simple pipelines — leaving heavy logic and performance concerns to the Unity/C# backend.

Safety and validation:
A fixed structure and reserved keywords make it easier to validate DSL files and catch configuration errors early before they reach the engine.

Fast iteration:
Designers can tweak balance values, add new effects, or create heroes without touching C# scripts, drastically speeding up iteration.


## Grammar

```
game_file      ::= [ import_stmt { import_stmt } ] "GAME" IDENT block ;

import_stmt    ::= "import" IDENT ";" ;

block          ::= "{" { declaration | section } "}" ;

section        ::= heroes_section
                 | arena_section
                 | status_effects_section
                 | items_section
                 | creeps_section ;


heroes_section ::= "Heroes" "{" { hero_def } "}" ;

hero_def       ::= "hero" IDENT "{" hero_body "}" ;

hero_body      ::= hero_stat_block
                 | hero_stat_block abilities_block
                 | abilities_block ;

hero_stat_block::= "heroStat" ":" "{" { stat_entry } "}" ;

stat_entry     ::= IDENT ":" expression ;

abilities_block::= "abilities" ":" "{" { ability_def } "}" ;

ability_def    ::= "ability" IDENT "{" ability_body "}" ;

ability_body   ::= { ability_field | behavior_field } ;

ability_field  ::= "type" ":" IDENT
                 | "cooldown" ":" expression
                 | "mana_cost" ":" expression
                 | "range" ":" expression
                 | "damage_type" ":" IDENT ;

behavior_field ::= "behavior" ":" ( behavior_expr | script_block ) ;

behavior_expr  ::= pipeline_expr ;

pipeline_expr  ::= function_call { "|>" function_call } ;

function_call  ::= IDENT "(" [ argument_list ] ")" ;

argument_list  ::= argument { "," argument } ;

argument       ::= IDENT ":" expression ;

arena_section  ::= "Arena" "{" arena_body "}" ;

arena_body     ::= { team_def | core_def | turret_def } ;

team_def       ::= "team" IDENT "{" team_body "}" ;

team_body      ::= [ core_ref ] [ turret_block ] ;

core_ref       ::= "core" IDENT ;

turret_block   ::= "turrets" ":" "{" { turret_def } "}" ;

turret_def     ::= "turret" IDENT "{" turret_body "}" ;

turret_body    ::= { IDENT ":" expression } ;


status_effects_section
               ::= "StatusEffects" "{" { status_effect_def } "}" ;

status_effect_def
               ::= "statusEffect" IDENT "{" status_effect_body "}" ;

status_effect_body
               ::= { status_effect_field } ;

status_effect_field
               ::= "type" ":" IDENT
                 | "duration" ":" expression
                 | "on_apply" ":" script_block 
                 | "on_tick" ":" script_block
                 | "on_expire" ":" script_block ;


items_section  ::= "Items" "{" { item_def } "}" ;

item_def       ::= "item" IDENT "{" item_body "}" ;

item_body      ::= { item_field | passive_block } ;

item_field     ::= IDENT ":" expression ;

passive_block  ::= "passive" ":" "{" passive_body "}" ;

passive_body   ::= "behavior" ":" behavior_expr ;

creeps_section ::= "Creeps" "{" { creep_def } "}" ;

creep_def      ::= "creep" IDENT "{" creep_body "}" ;

creep_body     ::= { IDENT ":" expression } ;

const_decl     ::= "const" type_expr IDENT "=" expression ;
set_stmt       ::= "set" IDENT "=" expression ";" ;

type_expr      ::= "Number"
                 | "Boolean"
                 | "String"
                 | "Duration"
                 | "Percentage"
                 | "Hero"
                 | "Creep"
                 | "Turret"
                 | "Core"
                 | "Team"
                 | "Ability"
                 | "StatusEffect"
                 | "Item" ;

expression     ::= ...   (* standard expression grammar: literals, identifiers, operators *)

script_block   ::= "{" { statement } "}" ;

statement      ::= if_stmt
                 | loop_stmt
                 | set_stmt
                 | const_decl
                 | return_stmt
                 | expression_stmt ;

if_stmt        ::= "if" "(" expression ")" script_block [ "else" ( script_block | if_stmt ) ] ;

loop_stmt      ::= while_stmt
                 | for_stmt ;

while_stmt     ::= "while" "(" expression ")" script_block ;

for_stmt       ::= "for" "(" IDENT "in" expression ")" script_block ;

expression_stmt::= expression ";" ;

return_stmt    ::= "return" [ expression ] ";" ;

```





