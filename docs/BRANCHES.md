# Repositories and branches

Where the code lives, which line is released, the plan for moving both lines into one repository, and what the `dev` line contains. Read it for any work on `dev`, for porting a `dev` feature, and before touching repository settings, default branches or version file URLs. Code ownership is in [ARCHITECTURE.md](ARCHITECTURE.md); the compatibility rules are in [MOD_API.md](MOD_API.md); version file mechanics are in [RELEASE.md](RELEASE.md).

## Current state

| Repository | `master` | Line |
|---|---|---|
| `SirHartley/Substance.Abuse.Legacy` | `b7642ae`, 1.1.d, game `0.98a`, LunaLib, Java 17 | Released. Hosts the 1.1.d release and the version file current installs poll. |
| `SirHartley/Substance.Abuse` | `1717edd`, 2.0.a unreleased, game `0.96a`, no LunaLib | Unfinished rework (custom alcohol, ingredients, brewery UI). Last commit January 2024. |

The two histories share no commit. `Substance.Abuse` starts at `24a085a` (July 2023, 1.0.k with custom alcohol work already started); `Substance.Abuse.Legacy` starts at `c532e31` (March 2025) and holds the 1.1.d code. The 1.1.a to 1.1.c changes (0.96a, 0.97a and 0.98a patches, brewery rework, price and consumption changes, LunaLib settings) exist only in the Legacy line.

## Target

| Repository / branch | Content |
|---|---|
| `SirHartley/Substance.Abuse` `master` | The Legacy line, with its history. Default branch. Releases are cut from here. |
| `SirHartley/Substance.Abuse` `dev` | The current `Substance.Abuse` `master` (`1717edd`), unchanged. |
| `SirHartley/Substance.Abuse.Legacy` | Kept, not deleted, until the version file hand-over below is done; then archived. |

## Migration plan

Not done yet. Every step changes a public repository and needs the user's explicit go-ahead.

1. In `Substance.Abuse`, check open pull requests, branch protection and the default branch setting.
2. Push `1717edd` to a new `dev` branch and a tag `pre-legacy-master`, so the rework stays reachable.
3. Replace `Substance.Abuse` `master` with Legacy `master`: `git push --force-with-lease=master:1717edd <substance-abuse-remote> <legacy-master>:master`. This is a force push and rewrites `master`; steps 1 and 2 come first.
4. Confirm `master` and `dev` on the remote, and that the default branch is `master`.
5. Change the repository names in [CLAUDE.md](../CLAUDE.md), this file and [RELEASE.md](RELEASE.md) to `Substance.Abuse` in the same docs commit.
6. Point the Legacy README at `Substance.Abuse`. Leave Legacy's `alcohol.version` in place.

Why a force push and not a merge: a merge commit with the old `master` as a parent would mark every `dev` commit as merged, and a later merge of `dev` would bring nothing. Replacing `master` keeps `dev` mergeable on its own terms.

### Version file hand-over

Installed copies poll the URL in their local `alcohol.version` (`masterVersionFile`):

- 1.1.d installs poll `SirHartley/Substance.Abuse.Legacy` `master`.
- 1.0.x installs poll `SirHartley/Substance.Abuse` `master`, which serves 1.0.j today. After step 3 it serves 1.1.d, so those players are told to update.

For the first release from `Substance.Abuse`, change `masterVersionFile`, `changelogURL` and `directDownloadURL` to `Substance.Abuse`, and update Legacy's `alcohol.version` to the same version and download URL so 1.1.d installs find it. Archive Legacy only after that release. That GitHub keeps serving raw files from an archived repository is not verified here; check before archiving.

## The dev line

State of `Substance.Abuse` `master` at `1717edd`, read from the source:

- Custom alcohol: player-made drinks from ingredients (`memory/CustomAlcohol`, `CustomAlcoholMemory`, `Ingredient`, `IndustrialAlcohol`, `BaseAlcohol`, `loading/Importer`), sold as one special item `CustomAlcohol.CUSTOM_ALCOHOL_ITEM_ID` with per-stack data, applied through three custom hullmod slots plus an overflow slot (`plugins/CustomAlcoholHullmodHandler`, `Custom_HullmodEffect`).
- Ingredients as commodities and market conditions (`data/campaign/ingredients.csv`, `procgen/condition_gen_data*.csv`, `IngredientResourceCondition`, sprites under `graphics/conditions/`).
- Brewery dialog and custom alcohol panel (`ui/BreweryOptionProvider`, `BreweryDialoguePlugin`, `CustomAlcoholPanel`, `ui/basepanel/*`). `ui/reference/*` imports Industrial Evolution classes and is excluded from compilation in `.idea/Substance.Abuse.iml`.
- `ModPlugin.onGameLoad` calls `devAddCustomAlcohol()` on every new game, outside dev mode.
- Settings come from `data/config/settings.json` through `static final` fields in `AddictionBrain`; LunaLib and `SettingsHandler` are gone. `CREW_PER_UNIT_PER_MONTH` is 10 there, 25 in the Legacy default.
- Project files target Java 7 (`.idea/misc.xml`), `mod_info.json` says game `0.96a` and has no LunaLib dependency, `alcohol.version` says 1.0.j.
- `jars/production/` (54 IDE class files) is committed although `.gitignore` excludes it.
- `Changelog` opens with a todo list; the 2.0.a section lists only four entries.

### API breaks on dev

Each break below makes a consumer from [MOD_API.md](MOD_API.md) fail at run time.

| Symbol | Legacy | dev | Breaks |
|---|---|---|---|
| `AlcoholRepo.get(String)` | returns `Alcohol` | returns `AlcoholAPI` | VIC `addBrewery` (`NoSuchMethodError`) |
| `BaseAlcoholHullmodEffect.getAlcohol()` | returns `Alcohol` | returns `AlcoholAPI` | VIC `vic_booze`, likely SVC `svc_booze` |
| `FactionAlcoholHandler.getFactionAlcoholTypes(String)` | exists | renamed `getFactionStaticAlcoholTypes`, returns `List<AlcoholAPI>` | SVC `giveCocktailToPirates` |
| `FactionAlcoholHandler.setFactionAlcoholTypes` | read by `AlcoholResourceCondition` | still callable, class `@Deprecated`, ignored; `AlcoholResourceCondition` matches the drink's own faction | giving a drink to a second faction, the use the README documents for modders |
| `AlcoholRepo.isAlcohol(String)` | exists | removed (`isIndustrialAlcohol`) | any caller |
| `AlcoholAPI.addStatusTooltip(TooltipMakerAPI)` | in the interface | replaced by `(TooltipMakerAPI, boolean)` | any implementer or caller through the interface |
| `AlcoholAPI` | 4 methods | 16 methods | any other implementation (`AbstractMethodError`) |
| `memory.SettingsHandler` | exists | removed | any caller |

Save data: `AddictionMemory` moved from sector memory to `Global.getSector().getPersistentData()` under the same key `$AddictionMemory`. A 1.1.d save loaded on `dev` starts with a fresh addiction state and keeps the old object in sector memory.

### Bringing dev work to master

`dev` cannot be merged into `master` as it stands: the histories are unrelated, `dev` lacks every 1.1.x change, and it breaks the API above. Port features from `dev` instead, one task branch per feature, off `master`, under the [MOD_API.md](MOD_API.md) rule. Keep the old entry points (for example `AlcoholRepo.get` returning `Alcohol`, with a new method for `AlcoholAPI`), keep LunaLib settings, and keep `$AddictionMemory` where it is. `dev` stays as the reference copy until every feature is ported or dropped.

## Maintenance

Update this file when a migration step is done, when the repositories or default branches change, when `dev` gains or loses a feature, and when a break is fixed on `dev` or ported to `master`. Remove the migration plan once it is complete and keep only the resulting state.
