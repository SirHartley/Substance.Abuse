# Repositories and branches

Where the code lives, which line is released, what is left of the move into one repository, and what the `mixology-update` line contains. Read it for any work on `mixology-update`, for porting a `mixology-update` feature, and before touching repository settings, default branches or version file URLs. Code ownership is in [ARCHITECTURE.md](ARCHITECTURE.md); the compatibility rules are in [MOD_API.md](MOD_API.md); version file mechanics are in [RELEASE.md](RELEASE.md).

## Current state

| Repository / branch | Content |
|---|---|
| `SirHartley/Substance.Abuse` `dev` | Next release of the released line. Branched from `master` at `aa5da44`. New work goes here by pull request. |
| `SirHartley/Substance.Abuse` `master` | Released line: 1.1.e, game `0.98a`, LunaLib, Java 17. Default branch; releases are cut from here. |
| `SirHartley/Substance.Abuse` `mixology-update` | Unfinished 2.0.a rework (custom alcohol, ingredients, brewery UI): game `0.96a`, no LunaLib. The old `master` (`1717edd`) plus a pointer `CLAUDE.md`. |
| `SirHartley/Substance.Abuse.Legacy` | Former home of the released line. Hosts the 1.1.d release and the version file 1.1.d installs poll. Kept until the hand-over below, then archived. |

`master` and `mixology-update` share no commit. `master` starts at `c532e31` (March 2025) and holds the 1.1.d code, moved here from `Substance.Abuse.Legacy` with its history. `mixology-update` starts at `24a085a` (July 2023, 1.0.k with custom alcohol work already started); `1717edd` is its last commit from before the move. The 1.1.a to 1.1.c changes (0.96a, 0.97a and 0.98a patches, brewery rework, price and consumption changes, LunaLib settings) exist only on `master`.

Never merge `master` into `mixology-update` or the reverse; see [Bringing mixology-update work to master](#bringing-mixology-update-work-to-master).

## Open steps

1. Version file hand-over, below, with the first release from this repository.
2. Archive `SirHartley/Substance.Abuse.Legacy` after that release. That GitHub keeps serving raw files from an archived repository is not verified; check before archiving.
3. Delete the leftover branch `legacy-master` (same commit as the moved `master`) in the GitHub UI.

### Version file hand-over

Installed copies poll the URL in their local `alcohol.version` (`masterVersionFile`):

- 1.1.d installs poll `SirHartley/Substance.Abuse.Legacy` `master`.
- Installs whose file points at `SirHartley/Substance.Abuse` `master` (the 1.0.j file on `mixology-update` does) now get the 1.1.d file and are told to update.

The first release from this repository changes `masterVersionFile`, `changelogURL` and `directDownloadURL` in `alcohol.version` to `SirHartley/Substance.Abuse`; the release workflow does this ([RELEASE.md](RELEASE.md#version-file)). After it, update Legacy's `alcohol.version` by hand to the same version and download URL so 1.1.d installs find it.

## The mixology-update line

State of `mixology-update` at `1717edd`, read from the source:

- Custom alcohol: player-made drinks from ingredients (`memory/CustomAlcohol`, `CustomAlcoholMemory`, `Ingredient`, `IndustrialAlcohol`, `BaseAlcohol`, `loading/Importer`), sold as one special item `CustomAlcohol.CUSTOM_ALCOHOL_ITEM_ID` with per-stack data, applied through three custom hullmod slots plus an overflow slot (`plugins/CustomAlcoholHullmodHandler`, `Custom_HullmodEffect`).
- Ingredients as commodities and market conditions (`data/campaign/ingredients.csv`, `procgen/condition_gen_data*.csv`, `IngredientResourceCondition`, sprites under `graphics/conditions/`).
- Brewery dialog and custom alcohol panel (`ui/BreweryOptionProvider`, `BreweryDialoguePlugin`, `CustomAlcoholPanel`, `ui/basepanel/*`). `ui/reference/*` imports Industrial Evolution classes and is excluded from compilation in `.idea/Substance.Abuse.iml`.
- `ModPlugin.onGameLoad` calls `devAddCustomAlcohol()` on every new game, outside dev mode.
- Settings come from `data/config/settings.json` through `static final` fields in `AddictionBrain`; LunaLib and `SettingsHandler` are gone. `CREW_PER_UNIT_PER_MONTH` is 10 there, 25 by default on `master`.
- Project files target Java 7 (`.idea/misc.xml`), `mod_info.json` says game `0.96a` and has no LunaLib dependency, `alcohol.version` says 1.0.j.
- `jars/production/` (54 IDE class files) is committed although `.gitignore` excludes it.
- `Changelog` opens with a todo list; the 2.0.a section lists only four entries.

### API breaks on mixology-update

Each break below makes a consumer from [MOD_API.md](MOD_API.md) fail at run time.

| Symbol | `master` | `mixology-update` | Breaks |
|---|---|---|---|
| `AlcoholRepo.get(String)` | returns `Alcohol` | returns `AlcoholAPI` | VIC `addBrewery` (`NoSuchMethodError`) |
| `BaseAlcoholHullmodEffect.getAlcohol()` | returns `Alcohol` | returns `AlcoholAPI` | VIC `vic_booze`, likely SVC `svc_booze` |
| `FactionAlcoholHandler.getFactionAlcoholTypes(String)` | exists | renamed `getFactionStaticAlcoholTypes`, returns `List<AlcoholAPI>` | SVC `giveCocktailToPirates` |
| `FactionAlcoholHandler.setFactionAlcoholTypes` | read by `AlcoholResourceCondition` | still callable, class `@Deprecated`, ignored; `AlcoholResourceCondition` matches the drink's own faction | giving a drink to a second faction, the use the README documents for modders |
| `AlcoholRepo.isAlcohol(String)` | exists | removed (`isIndustrialAlcohol`) | any caller |
| `AlcoholAPI.addStatusTooltip(TooltipMakerAPI)` | in the interface | replaced by `(TooltipMakerAPI, boolean)` | any implementer or caller through the interface |
| `AlcoholAPI` | 4 methods | 16 methods | any other implementation (`AbstractMethodError`) |
| `memory.SettingsHandler` | field-only save shell, no methods | removed | saves made before 1.1.e |

Save data: `AddictionMemory` moved from sector memory to `Global.getSector().getPersistentData()` under the same key `$AddictionMemory`. A 1.1.d save loaded on `mixology-update` starts with a fresh addiction state and keeps the old object in sector memory.

### Bringing mixology-update work to master

`mixology-update` cannot be merged into `master` as it stands: the histories are unrelated, `mixology-update` lacks every 1.1.x change, and it breaks the API above. Port features from `mixology-update` instead, one task branch per feature, off `master`, under the [MOD_API.md](MOD_API.md) rule. Keep the old entry points (for example `AlcoholRepo.get` returning `Alcohol`, with a new method for `AlcoholAPI`), keep LunaLib settings, and keep `$AddictionMemory` where it is. `mixology-update` stays as the reference copy until every feature is ported or dropped.

## Maintenance

Update this file when an open step is done, when the repositories or default branches change, when `mixology-update` gains or loses a feature, and when a break is fixed on `mixology-update` or ported to `master`. Remove the open steps section once it is empty.
