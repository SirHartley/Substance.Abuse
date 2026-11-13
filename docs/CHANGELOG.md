# Changelog guide

How agents write entries in the root `Changelog`. Read it for every implementation task. Version numbers and releases are in [RELEASE.md](RELEASE.md).

## When

- Every implementation task adds or amends its entries in the newest section before merge, in the same commit as the change.
- Read-only work and documentation-only changes add nothing. Say why in the PR when a change has no entry.
- Record only finished changes from the actual diff. Never list plans as shipped.

## Layout

The file opens with the credits block between the `---Credits---` and `---OoooooO---` lines; leave it unchanged unless asked. Versions follow, newest first, separated by a blank line:

```text
1.1.f - UNRELEASED

General
Added - Feature name: what it does
Adjusted - Changed value old -> new, with the unit
Fixed - The broken behaviour no longer happens
Removed - The thing (reason or replacement)
```

- Version heading alone on its line; ` - UNRELEASED` marks the section collecting work for the next release. If there is none, add one above the newest version and ask the user for the number when it is not obvious. Never write or change a save compatibility line unasked.
- Category headings are in-game feature names (`General` first, then `Drink effects`, `Blackouts`, `Markets` as in 1.1.e). A section with a few lines needs none.
- One change per line, starting `Added - `, `Adjusted - `, `Fixed - ` or `Removed - `, in that order inside a category.
- A `Note - ` line directly below an entry explains a limit or something the player must do.
- Plain text only. No Markdown, no bullets, no commit hashes, PR numbers, agent names or test logs.

## Wording

Entries are player-facing: what the player sees, with the drink or feature name as the game shows it, never class names or the code path.

- Under 80 characters. Go past 100 only for a new feature that needs its numbers.
- Capital letter after the dash, no full stop on a one-clause line. A second sentence is fine for a new feature; then both sentences end in a full stop.
- No semicolons, no curly quotes, no hyphen as a dash inside the text. Straight quotes for in-game strings.
- Parentheses carry the short extra: `(was 3)`, `(vanilla game limitation)`, `(new game)`, credits.
- Added: name, colon, what it does (`Added - Brewery: turns organics into drinks`), or `Added - Setting to ...`.
- Adjusted: numbers as old `->` new with the unit (`Adjusted - Cargo space per unit 0.5 -> 1.0`). Without numbers, a direction word: Reduced, Increased, Halved, Doubled. Say `(this is a buff)` or `(this is a nerf)` when the number hides it.
- Fixed: the broken behaviour that no longer happens, in these forms:
  - `Fixed - Stout no longer regenerates armor during withdrawal`
  - `Fixed - Tariffs are now properly reduced by Sweet Freedom`
  - `Fixed - Crash when a blackout starts in a starless system`
  - A short noun phrase for small things: `Fixed - Typos`
- Removed: the thing, then the reason or replacement in parentheses.
- Credits name real people only: `(thanks, Avanitia)`, `(reported by SilverLight)`. Never an agent.
- No first person, jokes, caps, exclamation marks outside a credit, or hedges about whether a fix works. Untested status goes in the PR.
- Keep the user's own lines verbatim, including their asides and spellings. Do not shorten, merge or tidy them, and do not imitate them.
- A feature new in the unreleased version gets one `Added - ` entry that is edited as it changes; it does not collect its own `Fixed - ` or `Adjusted - ` lines.
- Combine entries that fix the same visible problem. Never repeat a change per file or per commit.

Old forms not to copy: lines without the dash (`Fixed tooltip - again`), `Code adjustment - `, `Effect - `, `Cont. - `, `From dev release:`, indented sub-lines, `NOTE` in caps, `Known issue - ` inside a version, reporter tags (`[Wisp] - `), combined headings (`2.2.a/b`).

Published sections stay as released, even where a line breaks these rules.

## Maintenance

Update when the `Changelog` layout or the release process changes.
