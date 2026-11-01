# Substance.Abuse

<img src="_substanceAbuse.png" alt="Substance.Abuse icon" width="64" align="right">

A [Starsector](https://fractalsoftworks.com/) mod that adds 10 alcoholic beverages to the sector. Keep your crew supplied and your whole fleet gets bonuses. Let the supply run dry and they go into withdrawal. Drink too much at once and you black out.

**Game version:** 0.98a | **Mod version:** 1.1.e | **Forum thread:** [fractalsoftworks.com/forum/index.php?topic=24378](https://fractalsoftworks.com/forum/index.php?topic=24378)

## Features

* **10 drinks**, each with its own fleet wide bonus and its own withdrawal penalty
* **Addiction system:** each drink is tracked separately. Your crew builds resistance over time, and consumption rises with it
* **Withdrawal:** stop drinking and the bonus becomes a penalty that slowly wears off
* **Blackouts:** drink too much too fast and you wake up alone, while your fleet waits where you left it
* **Brewery industry:** a rural industry that produces large stockpiles of whatever recipe is installed
* **Recipes and Recipe Books:** raid a planet with a brewery to steal its recipe. Merge recipes into a Recipe Book to brew all of them in a single brewery
* **Economy integration:** factions produce and trade their own drinks, and your colonies import what their breweries produce

## The Drinks

| Drink | Faction | Brewery | Effect |
|---|---|---|---|
| Phoenix Stout | Hegemony | Jangala | Limited armor regeneration, increased weapon range |
| Absynth | Tri-Tachyon | Port Tse | Lower OP cost for weapons and fighters |
| Tears of Ludd | Luddic Church | Gilead | More officer XP, better missiles |
| Prometheus Gift | Luddic Path | Chalcedon | Faster acceleration and speed, better maneuverability under sustained burn |
| Reapers Blood | Pirates | Qaras | Faster missiles and projectiles, better zero flux boost, aggressive ships |
| Askonia Sunshine | Sindrian Diktat | Volturn | Limited hard flux dissipation through shields, less phase flux, lower fuel use |
| Kings Favour | Persean League | Fikenhild | Higher max CR and CR recovery, fewer ship and crew losses |
| Sweet Freedom | Independent | Ailmar | Lower tariffs, lower sensor profile, better fighters |
| Phaged Darjeeling | Independent | Asharu | Better shield arc, unfold and turn rate, less flux and CR decay while phased |
| Hangover Cure | Independent | Orthrus | Slows effect and resistance buildup, speeds up withdrawal recovery |

Hover over a drink in your cargo to see the exact numbers for your current addiction level.

## How It Works

1. Buy a drink and keep it in your cargo. Your fleet starts drinking automatically.
2. Only the crew needed to keep your ships running (skeleton crew) drinks. Roughly 1 unit feeds 25 crew per month.
3. The effect grows stronger the longer you drink, and so does consumption (up to twice the base amount).
4. Run out and your fleet goes into withdrawal. Use Hangover Cure to recover faster.
5. Raise your intake too quickly and you black out. Your fleet stays behind, marked in your intel as a "Drunk Fleet". Fly back and rejoin it.

## Installation

1. Install [LunaLib](https://fractalsoftworks.com/forum/index.php?topic=25658) (required).
2. Download the latest release from the [Releases page](https://github.com/SirHartley/Substance.Abuse/releases).
3. Extract the zip into your `Starsector/mods` folder.
4. Enable **Substance.Abuse** and **LunaLib** in the launcher.

Can be added to an existing save. Supports Nexerelin random sector mode (breweries are placed on the largest market of each faction). Version Checker is supported.

## Settings

All settings are available in game through the LunaLib settings menu:

| Setting | Default | Description |
|---|---|---|
| Crew per unit of alcohol, per month | 25 | Higher means less consumption |
| Addiction gain per day | 0.005 | How fast addiction builds |
| Monthly addiction gain limit | 1 | How much you can drink at once before blacking out |
| Water Mult | 0.5 | How strongly Hangover Cure affects addiction gain and loss |
| OP adjustment | On | Auto adjusts loadouts that go over the OP limit when Absynth withdrawal hits |

## For Modders

Assign drinks to your faction (including vanilla drinks) from your mod plugin:

```java
FactionAlcoholHandler.setFactionAlcoholTypes("your_faction_id", AlcoholRepo.STOUT, AlcoholRepo.TEA);
```

Drink IDs are listed in `com.fs.starfarer.api.alcoholism.memory.AlcoholRepo`. Source code is in [`jars/src`](jars/src).

## Changelog

See [Changelog](Changelog).

## Credits

* **Concept, code, implementation:** SirHartley
* **Item sprites:** bought and modified, [Unity Asset Store](https://assetstore.unity.com/publishers/13841)
* **Industry image:** commission, [Anton Juntunen](https://www.artstation.com/antonjuntunen)
* **Special thanks:** Avanitia, SilverLight

## License

<img src="CC-license.png" alt="CC BY-NC-ND">

Licensed under [Creative Commons Attribution-NonCommercial-NoDerivatives 4.0](https://creativecommons.org/licenses/by-nc-nd/4.0/).

*Drink responsibly-ish.*
