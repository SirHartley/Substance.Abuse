# Smoke tests

Scripted checks in the real game, driven by the `starsector-native-smoke` skill. Read this before running or extending them. Test code stays in `native-smoke/` and never reaches the release zip.

## Run

From Git Bash in a checkout:

```sh
bash native-smoke/live/run.sh fleet,drinks,market,codex,blackout
bash native-smoke/live/run.sh battle_drinking
bash native-smoke/live/run.sh battle_withdrawal
```

- `BUILD` picks the mod: `1.1.e` is the release zip (default), `1.1.d` is the baseline commit `1f15cd6` with its committed jar, and any other git ref is compiled with `lib/`.
- Each build has its own work folder, `C:\Modding\.smoke-work\Substance.Abuse.<build>`, with its base save and `runs/`.
- `run.sh` takes `C:\Modding\.smoke-lock` around the game and waits for it. Only one game runs on the machine at a time.
- It needs a copy of the skill's `scripts` and `templates` with `gamectl.exe` built, in `SMOKE_SKILL`. The header of `run.sh` lists the other variables.

`native-smoke/CodexTitleProbe.java` is a startup probe for `run-native-smoke.ps1`.

## Groups

| Group | Class | Covers |
|---|---|---|
| `fleet` | `FleetChecks` | Daily stat refresh, consumption forecast, LunaLib settings after a load |
| `drinks` | `DrinkChecks` | Absynth OP correction, module variants, tariffs, zero flux boost, Tears of Ludd XP, withdrawal after a load |
| `market` | `MarketChecks` | Drink supply and demand after an owner change, Light Industry inputs |
| `codex` | `CodexChecks` | Codex links and tooltips, tooltip wording |
| `blackout` | `BlackoutChecks` | Destinations, starless systems, drunk fleet, quips, drinking history. Run it last, it moves the player and splits the fleet |
| `battle_drinking`, `battle_withdrawal` | `DrinkBattle`, `DrinkBattleDriver` | Fighter damage, Reapers Blood AI, Stout armor regen, each in a real battle |

## Rules

- A check for a fix has to fail on the build before the fix. A check that passes there proves nothing.
- Read the `PROFILE` lines of every run and report any `PROFILE WARN`.
- LunaLib declares LazyLib as a dependency, so every run loads both.

## Maintenance

Update when a group, a build or the run command changes.
