# Smoke tests

Scripted checks in the real game, driven by the `starsector-native-smoke` skill. Read this before running or extending them. Test code stays in `native-smoke/` and never reaches the release zip.

## Run

From Git Bash in a checkout:

```sh
bash native-smoke/live/run.sh fleet,drinks,market,codex,survival,blackout
bash native-smoke/live/run.sh battle_drinking
bash native-smoke/live/run.sh battle_withdrawal
```

- The game is the 0.98.5a-RC7 install named in `run.sh` (`SMOKE_GAME` overrides it). Every run enables LunaLib, LazyLib and PatchLib.
- `BUILD` picks the mod: `1.1.e` is the release zip (default), `1.1.d` is the baseline commit `1f15cd6` with its committed jar, and any other git ref is compiled with `lib/`.
- Each build has its own work folder in the smoke folder, `C:\Modding\smoke\smoke-work\Substance.Abuse.<build>`, with its base save and `runs/`.
- It needs a copy of the skill 2.1.0 `scripts` and `templates` with `gamectl.exe` built, in `SMOKE_SKILL` (default `C:\Modding\smoke\smoke-work\Substance.Abuse\skill-2.1.0`). The skill's runner takes the machine slot and `C:\Modding\.smoke-lock`; `run.sh` takes no lock of its own. The header of `run.sh` lists the other variables.
- To test a branch without touching a working checkout, add a `git worktree` of it under `C:\Modding\smoke\worktrees` and run its `run.sh`.

`native-smoke/CodexTitleProbe.java` is a startup probe for `run-native-smoke.ps1`.

## Groups

| Group | Class | Covers |
|---|---|---|
| `fleet` | `FleetChecks` | Daily stat refresh, consumption forecast, LunaLib settings after a load |
| `drinks` | `DrinkChecks` | Absynth OP correction, module variants, tariffs, zero flux boost, Tears of Ludd XP, withdrawal after a load |
| `market` | `MarketChecks` | Drink supply and demand after an owner change, Light Industry inputs |
| `codex` | `CodexChecks` | Codex links and tooltips, tooltip wording |
| `survival` | `SurvivalChecks` | Drink floor and opened units, Survival stock, withdrawal, Sweet Freedom tariffs, Askonia Sunshine and Kings Favour values, recipe prices, Absynth stripping on bought ships, Qaras without a spool. Each step turns Survival mode on with `SectorAPI.setExtMode` and off again |
| `blackout` | `BlackoutChecks` | Destinations, starless systems, drunk fleet, quips, drinking history, no recipe gift, the Survival blackout. Run it last, it moves the player and splits the fleet |
| `battle_drinking`, `battle_withdrawal` | `DrinkBattle`, `DrinkBattleDriver` | Fighter damage, Reapers Blood AI, Stout armor regen and its pool, each in a real battle |

## Rules

- A check for a fix has to fail on the build before the fix. A check that passes there proves nothing.
- Checks reach members added after the last release through `MethodHandles`, so the check mod still compiles against an older build and those checks fail there.
- Read the `PROFILE` lines of every run and report any `PROFILE WARN`.
- LunaLib declares LazyLib as a dependency, so every run loads both.

## Maintenance

Update when a group, a build or the run command changes.
