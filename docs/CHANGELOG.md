# Changelog guide

How agents write entries in the root `Changelog`. Read it for every implementation task. Version numbers and releases are in [RELEASE.md](RELEASE.md).

## When

- Every implementation task adds or amends its entries in the newest section before merge, in the same commit as the change.
- Read-only work and documentation-only changes add nothing. Say why in the PR when a change has no entry.
- Record only finished changes from the actual diff. Never list plans as shipped.

## Layout

The file opens with the credits block between the `---Credits---` and `---OoooooO---` lines; leave it unchanged unless asked. Versions follow, newest first, separated by a blank line:

```text
1.1.e - UNRELEASED
Added - Short description of the new thing
Adjusted - What changed, with old and new values
Fixed - The broken behaviour that no longer happens
```

- Version heading alone on its line; ` - UNRELEASED` marks the section collecting work for the next release. If there is none, add one above the newest version and ask the user for the number when it is not obvious.
- One change per line, starting `Added - `, `Adjusted - `, `Fixed - ` or `Removed - `.
- A `Note - ` line directly below an entry explains a limit or something the player must do.
- Plain text only. No Markdown, no bullets, no commit hashes, PR numbers, agent names or test logs.

## Wording

Entries are player-facing: sentence case, plain words, the drink or feature name a player sees.

- Balance changes give old and new values, as the 1.1.c entries do (`Cargo space per unit 0.5 -> 1.0`).
- A fix describes what was wrong for the player, not the code path.
- A feature new in the unreleased version gets one `Added - ` entry that is edited as it changes; it does not collect its own `Fixed - ` or `Adjusted - ` lines.
- Combine entries that fix the same visible problem. Never repeat a change per file or per commit.

## Maintenance

Update when the `Changelog` layout or the release process changes.
