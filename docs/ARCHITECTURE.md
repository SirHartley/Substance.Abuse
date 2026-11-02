# Architecture

Technical routing for the current implementation on `master` (1.1.e line). Code paths below are relative to `jars/src/com/fs/starfarer/api/alcoholism/`; data paths are repository-relative. Search the named owner before adding another implementation.

The `dev` line (2.0.a rework) differs; see [BRANCHES.md](BRANCHES.md#the-dev-line). This file does not describe it.

| Reference | Scope |
|---|---|
| [CLAUDE.md](../CLAUDE.md) | Workflow, build gate and document upkeep |
| [MOD_API.md](MOD_API.md) | Public classes, members and ids other mods use; what must not change |
| [BRANCHES.md](BRANCHES.md) | Repository and branch model, open migration steps, state of the `dev` line |
| [RELEASE.md](RELEASE.md) | Version numbers, version file, release jar, package and release workflow |
| [CHANGELOG.md](CHANGELOG.md) | Change-note format and upkeep for every implementation task |

## Start here

| Change / symptom | Route |
|---|---|
| Daily consumption, addiction gain, withdrawal, run-out messages | `NewDayTracker -> NewDayListener -> listeners/AlcoholConsumptionManager.onNewDay -> memory/AddictionStatus -> memory/AddictionBrain` |
| Effect hullmod missing, stuck or wrong on ships and modules | `AlcoholConsumptionManager.reapply / applyActiveAlcoholHullmods / applHullmodToFleet / unapplyHullmodFromFleet` (perma mods on variants); refit tab via `RefitUIOpenChecker -> RefitTabListener` |
| Effect strength, effect and withdrawal numbers | `hullmods/alcoholEffectHullmods/<Drink>_HullmodEffect` on `hullmods/BaseAlcoholHullmodEffect`; scaling from `AddictionStatus.getAddictionValue()` |
| Drink tooltip, cargo prediction, "will last for" text | `memory/Alcohol.addEffectTooltip / addStatusTooltip`, `TooltipHelper` |
| Codex pages for drinks and their hullmods | `Global.CODEX_TOOLTIP_MODE` branches in `Alcohol.addStatusTooltip / addEffectTooltip / addFullHullmodEffectSection` and `BaseAlcoholHullmodEffect.addPostDescriptionSection`; links in `ModPlugin.onAboutToLinkCodexEntries` |
| Cargo shows commodity instead of item, or the reverse | `CargoUIOpenChecker -> CargoTabListener -> listeners/AlcoholStackReplacer`; `AlcoholRepo.convertID` |
| Right-click consume toggle, item render | `itemPlugins/AlcoholItemPlugin` (`IntervalTracker` drives the glow) |
| Blackout, drunk fleet, rejoining | `AlcoholConsumptionManager.onNewDay -> scripts/BlackoutScript.register -> scripts/FleetMergeScript`; `intel/DrunkFleetIntel` |
| Brewery output and recipe install | `industry/Brewery`; `itemPlugins/RecipeInstallableItemEffect`, `RecipeBookInstallableItemEffect` in `ItemEffectsRepo.ITEM_EFFECTS` |
| Which markets get a brewery | `industry/BreweryPlacer.placeBreweries` |
| Drink supply and demand on markets | `listeners/ResourceConditionApplicator -> conditions/AlcoholResourceCondition -> FactionAlcoholHandler.getFactionAlcoholTypes` |
| Which faction makes which drink | `memory/FactionAlcoholHandler.assignFactionAlcohols / setFactionAlcoholTypes` |
| Absynth withdrawal pushes ships over OP | `hullmods/campaignEffects/ExcessOPStripper`; ships are tagged `alcoholism_absynth_fit` on refit save by `AlcoholConsumptionManager.reportFleetMemberVariantSaved` (`RefitScreenListener`) and only tagged ships are stripped |
| Tariffs, officer XP, sustained burn, terrain effects | `hullmods/campaignEffects/TariffModifier`, `OfficerXPGainModifier`, `SustainedBurnNavigationModifier`, `TerrainEffectModifier`, registered from the owning drink's hullmod in every state that has a campaign effect, withdrawal included, since they are transient |
| Player settings | `config/Setting`, `config/PlayerSettings` and `data/config/LunaSettings.csv` |

## Registration and lifecycle

| Registry / hook | Owners |
|---|---|
| `ModPlugin.onApplicationLoad()` | `PlayerSettings.install()` |
| `ModPlugin.onGameLoad()` | Registers every script and listener below; each `register()` checks for an existing instance first. |
| `ModPlugin.onAboutToLinkCodexEntries()` | Links each drink's commodity, item, recipe and hullmod Codex entries with `CodexDataV2.makeRelated` |
| `data/campaign/commodities.csv` | Drink commodities `<drink>_c` |
| `data/campaign/special_items.csv` | Drink items `<drink>` (`AlcoholItemPlugin`), recipes `<drink>_item` (`RecipeItemPlugin`), `alcoholism_recipe_book` (`RecipeBookItemPlugin`) |
| `data/campaign/industries.csv` | `alcohol_brewery` (`Brewery`) |
| `data/campaign/market_conditions.csv` | `alcohol_resourceCondition` (`AlcoholResourceCondition`) |
| `data/hullmods/hull_mods.csv` | Effect hullmods `<drink>` (`<Drink>_HullmodEffect`), tagged `codex_require_related` so the Codex unlocks them with their drink; `alcoholism_bonus_*` (`SelfRepairingBuiltInHullmod`) |
| `data/config/LunaSettings.csv` | LunaLib settings tab; read by `PlayerSettings` |
| `data/config/settings.json` | Sprite keys under `graphics.alcohol` |
| `data/strings/descriptions.csv`, `data/strings/tips.json` | Descriptions and loading tips |
| `data/config/version/version_files.csv` | Version Checker entry, see [RELEASE.md](RELEASE.md) |

Load order in `ModPlugin.onGameLoad`: dev actions (dev mode only) -> `NewDayTracker` -> `CargoUIOpenChecker` -> `RefitUIOpenChecker` -> `PlayerSettings.install` -> remove `alcoholism_settings` from persistent data -> `AddictionMemory.refresh` -> `AlcoholStackReplacer` -> `AlcoholConsumptionManager` -> `ExcessOPStripper` -> `FactionAlcoholHandler.assignFactionAlcohols` -> `ResourceConditionApplicator` -> `BreweryPlacer.placeBreweries` -> recipe and recipe book item effects.

`onApplicationLoad` only installs the settings. `AlcoholRepo.ALCOHOL_MAP` is filled in its static initializer; other mods add to it from their own plugins (see [MOD_API.md](MOD_API.md)).

### Settings

`config/Setting` holds each LunaLib field id from `LunaSettings.csv` with its default and bounds; defaults and bounds mirror the CSV. `config/PlayerSettings` caches the values in a static map, nothing of it is saved. Its stateless `LunaSettingsListener` is added once per session in `onApplicationLoad` and reloads the cache on `settingsChanged("alcoholism")`; `onGameLoad` reloads it too. Runtime code reads `Setting.X.value()` or `enabled()`. Days per month is the constant `AddictionBrain.DAYS_PER_MONTH`, not a setting.

`memory/SettingsHandler` is a field-only save shell: saves made before 1.1.e hold one under `alcoholism_settings`, which `onGameLoad` removes. Delete the class on the next main game version update.

### Build and authoring tools

`tools/ReleaseBuild` builds the committed release jar `jars/release/alcoholism.jar` and packages the release ZIP; `tools/ReleaseBuildChecks` tests it; `.github/workflows/release.yml` runs both by hand. The `tools` package never ships. The IntelliJ artifact `alcoholism:jar` (`.idea/artifacts/alcoholism_jar.xml`) writes the gitignored `jars/alcoholism.jar`. Compile dependencies are in `lib/`, build input only. See [RELEASE.md](RELEASE.md) and [CLAUDE.md](../CLAUDE.md#building).

## Data identity

Ids, keys and class names are bound to saves and to other mods. Display names may change; these may not.

| Kind | Stable value |
|---|---|
| Mod id | `alcoholism` |
| Drink ids (`AlcoholRepo` constants) | `alcoholism_stout`, `alcoholism_absynth`, `alcoholism_tears`, `alcoholism_fuel`, `alcoholism_blood`, `alcoholism_sunshine`, `alcoholism_king`, `alcoholism_freedom`, `alcoholism_water`, `alcoholism_tea` |
| Commodity / item / recipe ids | `<drink>_c` / `<drink>` / `<drink>_item` |
| Industry, condition, recipe book | `alcohol_brewery`, `alcohol_resourceCondition`, `alcoholism_recipe_book` |
| Memory keys | `$AddictionMemory` and `DrunkFleetIntel.MEMORY_KEY_PRE + fleet id` (sector); `$BreweriesHaveBeenPlaced` (sector and market); `$AlcoholWarningHasBeenShow` (`ExcessOPStripper`); `$Alcohol_hasDroppedItem` (`BlackoutScript`) |
| Faction memory key | `$alcoholism_faction_alcohol_types` |
| Variant tag | `alcoholism_absynth_fit` (`ExcessOPStripper.FITTED_ON_ABSYNTH_TAG`) |
| Persistent data key | `alcoholism_settings` (old saves only, removed on load; `SettingsHandler` stays for them) |

Legacy names that survive: Prometheus Gift is `fuel`, Hangover Cure is `water`, Phaged Darjeeling is `tea`, Absynth keeps its spelling in ids. Several keys lack the mod prefix (`$AddictionMemory`, `alcohol_brewery`); keep them as they are, renaming breaks saves and the consumer mods.

## Source owners

Use `rg --files jars/src` for the full inventory. The entries below are state and integration owners.

| File | Owner / connection |
|---|---|
| `ModPlugin` | Registration order; `log()` writes only in dev mode |
| `memory/AlcoholRepo` | Drink registry `ALCOHOL_MAP`, id constants, commodity/item id conversion, recipe item effect registration |
| `memory/Alcohol` | One drink: id, multiplier, faction, import/export mods, production demands, tooltips |
| `memory/AddictionMemory` | Per-drink `AddictionStatus` map and the rolling monthly addiction gain used for blackouts; sector memory `$AddictionMemory` |
| `memory/AddictionStatus` | Consuming / addicted / withdrawal state and addiction value for one drink |
| `memory/AddictionBrain` | The addiction and consumption maths |
| `memory/FactionAlcoholHandler` | Faction to drink list in faction memory |
| `config/Setting`, `config/PlayerSettings` | LunaLib setting ids, defaults and bounds; static settings cache |
| `memory/SettingsHandler` | Save shell for old saves, scheduled for removal |
| `listeners/AlcoholConsumptionManager` | Daily loop, effect hullmod application, run-out warnings, blackout trigger, Absynth refit tag |
| `listeners/AlcoholStackReplacer` | Commodity and item stack conversion around the cargo tab |
| `listeners/ResourceConditionApplicator` | Adds `alcohol_resourceCondition` to markets on economy ticks and colonisation |
| `scripts/BlackoutScript` | Blackout sequence: detaches the fleet, spawns the drunk fleet, intel, moves the player to a system with a distress jump point, avoiding pulsar, black hole, cut off, hidden (`THEME_HIDDEN`) and temporary systems when any other is left |
| `industry/Brewery`, `industry/BreweryPlacer` | Brewery industry and the one-time placement |
| `hullmods/BaseAlcoholHullmodEffect` | Base for every effect hullmod, including other mods' drinks |

## Contracts

### Engine

- `BlackoutScript` calls `com.fs.starfarer.campaign.CampaignEngine.getInstance().getCampaignUI().showNoise(...)`, an obfuscated game class. It needs `starfarer_obf.jar` to compile and can break on a game update.
- `AlcoholItemPlugin.render` uses raw `GL11` blend and colour mask calls; restore the colour mask after drawing.
- The Codex sets `Global.CODEX_TOOLTIP_MODE` while it builds a page, also on the title screen, where there is no player fleet. Drink tooltips then show every effect section at addiction 1.0 and skip the fleet and cargo status.
- Refresh player fleet stats after hullmod changes with one `FleetDataAPI.setSyncNeeded()` and `syncIfNeeded()` (`AlcoholConsumptionManager.syncFleetStats`), not `FleetMemberAPI.updateStats()` per member. Each `updateStats()` sets the fleet sync flag, so a `getCargo()` call from another mod's skill or hullmod during the next member's update resyncs the whole fleet once per member.
- `ShipVariantAPI.getModuleVariant` returns the shared stock variant until the ship has its own module copy. Edit modules only through `AlcoholConsumptionManager.getEditableModuleVariant`, which clones a stock module into the ship first, as vanilla does.
- `HullVariantSpec` caches the stats used for OP costs until a hullmod is added or removed. The daily remove and re-add in `AlcoholConsumptionManager.reapply` clears that cache, which keeps the Absynth OP discount in step with the addiction value.

### Polling

- `NewDayTracker`, `CargoUIOpenChecker` and `RefitUIOpenChecker` are transient `EveryFrameScript`s that poll the clock and `CampaignUIAPI.getCurrentCoreTab()` and fire the mod's own `NewDayListener`, `CargoTabListener` and `RefitTabListener`. Whether a vanilla callback now covers these cases is not verified; check the API before adding another poller, and prefer one of these three listeners.

### Save data

- The mod is released and can be added to an existing save. Classes stored in saves (`AddictionMemory`, `AddictionStatus`, `SettingsHandler` (old saves), the non-transient `ExcessOPStripper` listener, scripts added with `addScript`, intel) keep their package, name and fields. `ExcessOPStripper.taggedExistingFits` is false on older saves and tags their Absynth fleet ships once.
- `AddictionMemory.refresh()` adds missing statuses for drinks added since the save, including other mods' drinks.

### Shared cross-file constraints

- `AlcoholResourceCondition` writes drink supply and demand under its own condition id and clears it in `unapply` for every drink, whoever owns the market. The Brewery recipe effects use the industry's index 0 id through the static `applySupply` and `applyDemand`. Industry supply and demand are not saved (`CoreLifecyclePluginImpl.econPreSaveCleanup`) and are rebuilt by `reapplyConditions` then `reapplyIndustries` on load, so changing these ids needs no save migration.
- `OfficerXPGainModifier` snapshots officer XP on the first `reportPlayerEngagement` of a battle and applies the Tears of Ludd change in `reportBattleOccurred`, which fires after vanilla grants battle XP (`FleetEncounterContext`). Autoresolved battles do not fire it; their XP counts towards the next battle.
- The daily drink amount, at least one unit, comes from `AlcoholConsumptionManager.getRequiredUnitsPerDay`. Consumption and the forecasts in `TooltipHelper` both use it.
- `AlcoholConsumptionManager.onNewDay` iterates `AlcoholRepo.ALCOHOL_MAP.values()` as `Alcohol`; every entry, including other mods', must be an `Alcohol`.
- `FactionAlcoholHandler.assignFactionAlcohols()` overwrites the vanilla factions' lists on every load. A mod that appends to a vanilla faction's list (Symbiotic Void Creatures appends to `pirates`) depends on running after it; the mod load order is not verified.
- Recipe item effects are registered from `ALCOHOL_MAP` at the end of this mod's `onGameLoad`, so a drink added to the map before that gets its recipe effect without registering it itself.

## Dead or dormant

| Component | State |
|---|---|
| `ModPlugin.devActions`, `AlcoholConsumptionManager.devActions` | Dev mode only: adds drinks and supplies, 10 days of addiction per day |
| `memory/SettingsHandler` | Save shell, no callers; see [Settings](#settings) |
