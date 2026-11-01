# Release

Version numbers, the version file, the release jar and the package. Read it before changing the build, a version number or a version file URL, and before a release. Which repository serves the version file during the migration is in [BRANCHES.md](BRANCHES.md#version-file-hand-over). Change-note rules are in [CHANGELOG.md](CHANGELOG.md).

Release only when the user asks. No version bumps, tags, rebuilt jars or published releases as part of ordinary work.

## Versions

Scheme `major.minor.letter` (`1.1.d`), no `v` prefix. The letter moves for fixes and balance, the minor number for gameplay changes players notice, the major number for a major release. Lower parts reset when a higher one moves. After a release, ask which version comes next if it has not been chosen.

A version appears in four places; a release changes all four together:

| File | Field |
|---|---|
| `mod_info.json` | `version`; also `gameVersion` for a game update |
| `alcohol.version` | `modVersion` (`major`, `minor`, `patch` letter) and `directDownloadURL` |
| `Changelog` | newest version heading |
| GitHub release | tag and asset name, `<version>` and `Substance.Abuse<version>.zip` |

## Version file

`alcohol.version` is the Version Checker file, registered in `data/config/version/version_files.csv`. Version Checker reads `masterVersionFile` from the installed copy and fetches that URL; `directDownloadURL` and `changelogURL` are taken from the fetched file. The file allows `#` comments and trailing commas.

Current URLs point at `SirHartley/Substance.Abuse.Legacy` `master` and its releases. Do not change them outside the hand-over in [BRANCHES.md](BRANCHES.md#version-file-hand-over).

## Release jar

`jars/alcoholism.jar` is committed and is what players run (`mod_info.json` `jars`). It is built by the IntelliJ artifact `alcoholism:jar` and contains Java 17 class files. Ordinary changes leave it stale on purpose; rebuild and commit it only for a release the user asked for. There is no scripted builder or release workflow; add one only on request, run by hand, with no push or pull request triggers.

## Package

The contents of the release zip are not documented. Record them here at the next release, from the 1.1.d asset.

## Maintenance

Update when a version location, URL, the jar build or the package contents change.
