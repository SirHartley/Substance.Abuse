# Mod integration API

The classes, members and ids other mods compile against or reference from their data. Read this before changing any public class, method, field, constructor, return type or id, on any branch. Where these symbols live and what they do is in [ARCHITECTURE.md](ARCHITECTURE.md); how the `mixology-update` line breaks them is in [BRANCHES.md](BRANCHES.md#api-breaks-on-dev).

Other mods ship jars compiled against a released `alcoholism.jar` and are not rebuilt when this mod changes. A change that compiles here can still throw `NoSuchMethodError`, `NoSuchFieldError` or `AbstractMethodError` in their code at run time.

## Rule

- The symbols below are frozen. Do not rename, remove, move to another package, change a parameter list, change a return type, change a field type, or make them less visible.
- Adding is safe: new classes, new overloads, new methods on classes. Adding an abstract method to an interface or abstract class that other mods implement or extend is not safe (`BaseAlcoholHullmodEffect`, `AlcoholHullmodEffectAPI`).
- Replacing a mechanism is allowed only behind the old entry points: keep the old method working and route it to the new owner. `@Deprecated` is fine; removal is not.
- Ids in the table at the end are referenced from other mods' CSV and code; treat them like the Java symbols.
- A change to anything listed here needs the user's approval and a line in the PR under Changes.

## Known consumers

Found with GitHub code search for `com.fs.starfarer.api.alcoholism` in November 2026. Other consumers may exist outside GitHub.

| Mod | Files |
|---|---|
| VIC (`AstaratStarsector/VIC`) | `jars/src/data/scripts/plugins/vic_addBooze.java`, `jars/src/data/hullmods/vic_booze.java`, `jars/src/data/scripts/plugins/SubAb/vic_AlcoholItemPlugin.java`, `vic_makeRecipePlugin.java`, `vic_RecipeItemPlugin.java` |
| Symbiotic Void Creatures (`DesperatePeter/starsector-symbiotic-void-creatures`) | `src/tecrys/svc/plugins/substanceabuse/loadSubstanceAbuse.kt`, `makeRecipeItem.kt`, `svc_AlcoholItemPlugin.java`, `src/tecrys/svc/hullmods/svc_booze.kt`, `data/campaign/special_items.csv` |

Both guard their integration with `isModEnabled("alcoholism")`.

## Frozen symbols

Package prefix `com.fs.starfarer.api.alcoholism.` omitted.

| Symbol | Used as | By |
|---|---|---|
| `memory.AlcoholRepo.ALCOHOL_MAP` | static `Map`, `put(id, new Alcohol(...))` | VIC, SVC |
| `memory.AlcoholRepo.get(String)` returning `Alcohol` | `.getIndustryItemId()` on the result | VIC |
| `memory.Alcohol(String id, float mult, String factionId, int, int, String... demands)` | constructor | VIC, SVC |
| `memory.Alcohol.getId()`, `getAddictionStatus()`, `getIndustryItemId()` | calls | VIC, SVC |
| `memory.AddictionMemory.getInstanceOrRegister()`, `refresh()` | calls after adding a drink | VIC, SVC |
| `memory.AddictionStatus.getAddictionValue()`, `isConsuming()` | calls | VIC |
| `memory.FactionAlcoholHandler.setFactionAlcoholTypes(String, String...)` | call | VIC, SVC |
| `memory.FactionAlcoholHandler.getFactionAlcoholTypes(String)` returning `List<Alcohol>` | call, `.id` on elements | SVC |
| `industry.Brewery.INDUSTRY_ID` | `market.addIndustry(...)`, `getIndustry(...).setSpecialItem(...)` | VIC, SVC |
| `itemPlugins.RecipeInstallableItemEffect(String)` | constructor, put into `ItemEffectsRepo.ITEM_EFFECTS` | VIC, SVC |
| `itemPlugins.AlcoholItemPlugin()` | constructor, `init(stack)`, `setId(id)`, wrapped by their item plugin | VIC, SVC |
| `itemPlugins.RecipeItemPlugin()` | constructor, wrapped by their item plugin | VIC, SVC |
| `hullmods.BaseAlcoholHullmodEffect` | superclass of their effect hullmod; `getAlcohol()` returning `Alcohol`, `isWithdrawal()` | VIC, SVC |

The table lists what the search showed. Members a consumer calls through a superclass (the `BaseAlcoholHullmodEffect` and `BaseSpecialItemPlugin` methods they override or call) are only partly visible in search snippets; treat every public and protected member of the listed classes as frozen.

## Frozen ids

| Id | Referenced by |
|---|---|
| `alcoholism` (mod id) | `isModEnabled` checks in both consumers |
| `alcohol_brewery` | SVC `special_items.csv` (recipe `params` column), both consumers via `Brewery.INDUSTRY_ID` |
| `alcoholism_*` drink ids | `AlcoholRepo` constants; the README tells modders to use them with `FactionAlcoholHandler.setFactionAlcoholTypes` |

## Maintenance

Update the consumer and symbol tables when a new consumer is found or a consumer's usage changes; record the search and date. Add a symbol before shipping a change that a consumer would see. Never remove a row because the symbol was changed; restore the symbol instead.
