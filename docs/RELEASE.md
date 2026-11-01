# Release

Version numbers, the version file, the release jar, the package and the release workflow. Read it before changing the build, a version number or a version file URL, and before a release. Which repository serves the version file during the migration is in [BRANCHES.md](BRANCHES.md#version-file-hand-over). Change-note rules are in [CHANGELOG.md](CHANGELOG.md).

Release only when the user asks. No version bumps, tags, rebuilt jars or published releases as part of ordinary work.

## Versions

Scheme `major.minor.letter` (`1.1.d`), no `v` prefix. The letter moves for fixes and balance, the minor number for gameplay changes players notice, the major number for a major release. Lower parts reset when a higher one moves. After a release, ask which version comes next if it has not been chosen.

A version appears in four places. `ReleaseBuild package` stamps the first two into the ZIP, the workflow commits the stamped version file back after the release exists, and the changelog heading is edited by hand:

| File | Field |
|---|---|
| `mod_info.json` | `version` (stamped in the ZIP only); `gameVersion` by hand for a game update |
| `alcohol.version` | `modVersion` (`major`, `minor`, `patch` letter) and the three URLs |
| `Changelog` | newest version heading |
| GitHub release | tag `<version>`, asset `Substance.Abuse<version>.zip` |

## Version file

`alcohol.version` is the Version Checker file, registered in `data/config/version/version_files.csv`. Version Checker reads `masterVersionFile` from the installed copy and fetches that URL; `directDownloadURL` and `changelogURL` are taken from the fetched file. The file allows `#` comments and trailing commas.

The committed URLs point at `SirHartley/Substance.Abuse.Legacy` until the first release from this repository. `package` stamps `masterVersionFile` and `changelogURL` to this repository's `master` and `directDownloadURL` to this repository's release, so the first release does the URL part of the hand-over in [BRANCHES.md](BRANCHES.md#version-file-hand-over). Do not edit the URLs by hand.

## Release jar

`jars/release/alcoholism.jar` is committed and ships as `jars/alcoholism.jar` (`mod_info.json` `jars`). It holds Java 17 runtime classes only, without the `tools` package, and a `Source-SHA256` manifest entry over `jars/src` and the `lib/` jars. The current file is the 1.1.d jar, built by IntelliJ, without that entry, so it counts as stale.

```text
java jars/src/com/fs/starfarer/api/alcoholism/tools/ReleaseBuild.java build
```

Run from the repository root with JDK 17. It compiles every source against `lib/` into a fresh `out/release-build-*` directory, checks the jar and only then replaces `jars/release/alcoholism.jar`. Rebuild and commit it only for a release the user asked for.

The IntelliJ artifact `alcoholism:jar` writes `jars/alcoholism.jar`, which is gitignored and never ships.

## Package

```text
java jars/src/com/fs/starfarer/api/alcoholism/tools/ReleaseBuild.java package          # preview
java jars/src/com/fs/starfarer/api/alcoholism/tools/ReleaseBuild.java package 1.1.e    # release
```

Output in `out/release/`: `Substance.Abuse<version>.zip`, `release-notes.md` (the newest changelog section in a `text` block), the stamped `alcohol.version` and `version.txt`.

- A preview packages the newest section even if `UNRELEASED` and accepts a stale jar with a warning.
- A release needs the version to equal the newest section, that section without `UNRELEASED`, and a fresh jar.
- The ZIP has one top-level `Substance.Abuse/` folder, sorted entries and a fixed timestamp. 1.1.d and earlier used `Substance.Abuse<version>/`.
- `graphics/` and `sounds/` paths in `data/` that point into a directory the mod ships must exist in the ZIP; other paths are vanilla.

Contents are an allowlist over `git ls-files`; untracked files never ship. The constants at the top of `ReleaseBuild` own the list:

| Path | Shipped |
|---|---|
| Root | `mod_info.json`, `alcohol.version`, `Changelog`, `CC-license.png`, `_substanceAbuse.png` (LunaLib icon, `LunaSettingsConfig.json`) |
| `data/` | `csv json shader ship wpn proj system variant skin` |
| `graphics/` | `png jpg jpeg` |
| `sounds/` | `ogg wav` |
| `jars/release/alcoholism.jar` | as `jars/alcoholism.jar` |

Never shipped: `jars/src`, `lib/`, `docs/`, `README.md`, agent files, `.github/`, `.idea/`, `*.iml`, dot directories and `unused`, `unedited`, `tools` or `docs` directories. The 1.1.d ZIP also had `jars/source.url`, a Windows shortcut to the repository; it is not tracked and no longer ships. Add a new runtime format to `ASSETS` in the same commit as the first asset that needs it.

`ReleaseBuildChecks` tests changelog parsing, stamping and the file filters:

```text
javac --release 17 -encoding UTF-8 -d out/release-tools jars/src/com/fs/starfarer/api/alcoholism/tools/*.java
java -cp out/release-tools com.fs.starfarer.api.alcoholism.tools.ReleaseBuildChecks
```

## Release workflow

`.github/workflows/release.yml` runs only when started by hand (`workflow_dispatch`), from the Actions tab or `gh workflow run release.yml --ref master -f version=<version>`, and only on the user's command. Add no other trigger or workflow.

- The package job runs the checks and `package`, and uploads the ZIP, notes and version file as an artifact for 14 days. Without a version it stops there.
- With a version the publish job fails if the release or tag already exists, creates the release and tag on the packaged commit with the notes, then commits the stamped `alcohol.version` to `master` (`update version file to <version>`). If branch protection blocks that push, commit `out/release/alcohol.version` by hand.

## Running a release

1. Confirm the version.
2. Finish the newest `Changelog` section and remove ` - UNRELEASED`.
3. Run `build` and commit `jars/release/alcoholism.jar` with the changelog.
4. Run the checks and `package <version>` locally.
5. Open a pull request and merge it.
6. Start the Release workflow on `master` with `version` set.
7. Check the GitHub release and that `master`'s `alcohol.version` names it.
8. First release from this repository only: finish the remaining hand-over steps in [BRANCHES.md](BRANCHES.md#version-file-hand-over).

## Maintenance

Update when a version location, URL, the jar build, the package contents or the workflow change. Keep the `ReleaseBuild` constants, `release.yml` and this file aligned.
