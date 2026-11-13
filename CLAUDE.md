# Substance.Abuse

Starsector mod (id `alcoholism`): ten drinks with fleet wide effects, addiction, withdrawal, blackouts and a Brewery industry. Released line 1.1.e for game 0.98a, LunaLib required.

## Required workflow

These instructions apply to all coding agents. App-specific tools and model assignments are scoped below.

Reviews, explanations, audits, and proposals are read-only unless the user requests changes. For implementation, finish the authorized edit, commit, validation, pull request, and merge steps. Use reasonable assumptions for routine details; ask when a missing decision would materially change the scope or behavior. A request to stop or propose only overrides the implementation workflow.

### Read the current documentation

1. Read the current remote `CLAUDE.md` in full at the start of each new task. Within the same task, reuse the version already read unless the instructions change. Read new or changed instruction files before following them.
2. Use the `starsector-knowledge` skill before answering or changing anything related to Starsector. It covers one exact game version; name it when you use it. Do not rely on memory.
3. LunaLib and LazyLib are not covered by that skill. Read them from their jars, decompiled with the Vineflower bundled in the skill, into a temporary directory, never into the repository.
4. Select the required guides using [Which guide to read](#which-guide-to-read). Apply every matching row, including for reviews and fixes to existing behavior.

### Which guide to read

After reading this file, use the task to choose the next document. Read the relevant sections and their prerequisites. A task spanning several areas needs every matching route.

| Task | Required reading / order |
|---|---|
| Locate code or data, trace a feature, change an owner or lifecycle | [ARCHITECTURE.md](docs/ARCHITECTURE.md) before tracing code; follow its owner links. |
| Change any public class, member, constructor, return type or id; add a drink; touch `AlcoholRepo`, `Alcohol`, `FactionAlcoholHandler`, `AddictionMemory`, `BaseAlcoholHullmodEffect` or the item plugins | [MOD_API.md](docs/MOD_API.md) before editing, then [ARCHITECTURE.md](docs/ARCHITECTURE.md). |
| Any work on the `mixology-update` branch, porting a `mixology-update` feature, repository or default branch changes | [BRANCHES.md](docs/BRANCHES.md), then [MOD_API.md](docs/MOD_API.md). |
| Write or revise player-facing text: `descriptions.csv`, `tips.json`, tooltips, messages, intel, `LunaSettings.csv` help | The existing text in the same file for tone; the `starsector-knowledge` skill for vanilla wording. There is no text guide yet. |
| Change the build, a version number or a version file URL, or prepare a release | [RELEASE.md](docs/RELEASE.md) |
| Any implementation task | [CHANGELOG.md](docs/CHANGELOG.md): add or amend the task's entries in the newest unreleased section before merge. |
| Run or extend smoke checks | [SMOKE_TESTS.md](docs/SMOKE_TESTS.md) |
| Edit a guide or add documentation | [Documentation upkeep](#documentation-upkeep), the owning guide and its inbound links. |

### Technical references and conflicts

[ARCHITECTURE.md](docs/ARCHITECTURE.md) says where things live; [MOD_API.md](docs/MOD_API.md) says what may not change; [BRANCHES.md](docs/BRANCHES.md) says which line a fact belongs to. `README.md` is player and modder facing and is not a technical reference. Resolve any technical conflict against the exact source and record the evidence; do not pick a winner by filename, recency or an authority label.

### Make and record changes

- Use one task branch for the user's message.
- Make one commit per requested change. If one message contains several changes, commit them separately in the requested order.
- Commit messages are as short as possible: all lowercase, at most 8 words, programmer speak (`fix sweet freedom tariff sign`). One line only. No body, no conventional-commit prefix, no testing notes, agent or model names, co-author trailers, session metadata, links or trailing full stop.
- Update the affected documentation in the same commit, following [Documentation upkeep](#documentation-upkeep). Do this without a separate user request.
- Update `Changelog` in the same commit, using [CHANGELOG.md](docs/CHANGELOG.md).
- Rebuild and commit `jars/release/alcoholism.jar`, bump versions, run the release workflow, tag, and publish a release only when the user explicitly asks; follow [RELEASE.md](docs/RELEASE.md).
- Work only in the current task checkout. Live installations and unrelated checkouts are read-only unless the user explicitly asks you to change them.
- Do not deploy or synchronize after merge unless the user explicitly requests it.
- The mod is released and can be added to running saves. Keep saves from the current public version loading: do not rename or remove saved classes, their fields, ids or memory keys listed in [ARCHITECTURE.md](docs/ARCHITECTURE.md#data-identity). Never claim save compatibility without evidence.
- Keep the [MOD_API.md](docs/MOD_API.md) rule on every branch. A break needs the user's approval first.
- Prefer listeners and existing callbacks over polling. Before adding or retaining a polling solution, verify against the exact source that no existing callback covers the requirement. Prefer the mod's existing `NewDayListener`, `CargoTabListener` and `RefitTabListener` over a new poller.

### Validate and merge

- Every runtime-affecting final branch must pass the full clean build described in [Building](#building).
- Build the exact final remote task-branch revision. Earlier builds, partial builds, IDE analysis, and static checks do not satisfy the gate.
- Missing compilers or required dependencies are merge blockers. Documentation-only changes do not require a build. Changes to `data/` are runtime changes.
- A CSV edit must round-trip byte-identical before the change, keep LF line endings and the column count, and use straight ASCII quotes only.
- No CI runs on pushes or pull requests. Do not add workflows or triggers unless the user asks.
- Open a pull request and merge it when the work is complete. Do not leave finished work on an unmerged branch.
- PR title follows the commit message rules. PR body has `## Changes` (what changed, docs touched, then a `Changelog:` line with the entry or why there is none) and `## Validation` (build command, commit, JDK, result, what is not tested). Number several requested changes under `## Changes`. No attribution marker, "Requested by" line, Claude footer or session link.
- Use the connected GitHub app to create, inspect, and merge pull requests. Use `gh` only if the app is unavailable.
- Fetch current remote `dev` before branching and again before merging. Integrate intervening changes, push the exact final commit, and verify the merged remote revision.
- New work branches from and is merged into `dev` by pull request. Work for the `mixology-update` line branches from and merges into `mixology-update`; see [BRANCHES.md](docs/BRANCHES.md).

### Branch cleanup

GitHub normally deletes a pull request's head branch after merge. Verify that the branch is gone. Remove it manually when it was pushed without a pull request, its pull request was closed without merging, or it predates the repository setting. If deletion returns HTTP 403, report the branch name and leave it in place.

After merge, fast-forward the local task branch to the new `dev`.

## Documentation upkeep

Update documents automatically as part of each relevant change. Before committing, check which entries the diff makes stale and update those entries in that commit.

| Document | Owns | Update when / how |
|---|---|---|
| `AGENTS.md` | Discovery for other agents | Keep it a short pointer to this file. Do not copy workflow policy into it. |
| `CLAUDE.md` | Task scope, workflow, tools, commits, PRs, builds, comments, document maintenance | Replace changed policies here and update links. Keep provider-specific instructions explicitly scoped. |
| `docs/ARCHITECTURE.md` | Technical routing: owners, registrations, data flow, lifecycle, ids and cross-file constraints of `master` | Update the affected route or contract; remove obsolete owners. Exact paths and symbols. No release history or duplicate policy. |
| `docs/MOD_API.md` | Frozen public symbols and ids, known consumer mods | Add symbols and consumers as found. Never drop a row because the symbol changed; restore the symbol. |
| `docs/BRANCHES.md` | Repositories, branches, open migration steps, state and API breaks of `mixology-update` | Update when an open step is done, a branch changes, or a `dev` feature is ported or dropped. |
| `docs/RELEASE.md` | Version numbers, version file, release jar, package, release workflow | Update when a version location, URL, the jar build, the package or `.github/workflows/release.yml` changes. |
| `docs/SMOKE_TESTS.md` | Smoke check groups, builds and how to run them | Update when a group, a build or the run command changes. |
| `Changelog` and `docs/CHANGELOG.md` | Shipped change notes and how agents write them | Add or amend unreleased entries with each implementation task; preserve published entries and the credits block. |
| `README.md` | Player and modder facing overview | Update when a drink, setting, requirement or the documented modder call changes. |
| `.claude/agents/*.md` | Subagent definitions | Update when the dispatch rules below change. |

Give each fact one home. Move useful detail to its proper owner and link to it. After moving a section, repair inbound links and check that no requirement or technical constraint was lost.

For a new or renamed guide, update the required-reading table, this ownership table and the architecture reference table in the same commit. State when to read it and what it does not govern.

Write documentation in ordinary English. Use direct verbs and concrete nouns; remove slogans, inflated claims, repeated contrasts, and commentary about how important the document is. Keep identifiers exact. Documentation records the current state, not a development diary.

## Answers and wording

- Keep answers short and succinct: lead with the result, no preamble or recap. Detailed evidence goes in the pull request.
- Programming-related text (commits, PRs, comments, identifiers, technical docs) uses the vocabulary programmers have used for the last 20 years. Prefer the old word: use, fix, add, show; not leverage, address, introduce, surface.
- No em dashes and no hyphen used as a dash. No emojis unless topical.

## Comments

- Default to no comments. Add one only where it explains what is not apparent from the code: engine quirks, required ordering, lifecycle rules, save compatibility, serialization, reflection, units, invariants, non-obvious math, cross-file coupling.
- Comments are minimal, all lowercase, terse programmer speak, one line where possible (`// other mods put Alcohol into this map, keep the value type`). Code identifiers keep their exact spelling.
- Add no other comments: no javadoc, section headers, restated code, history, TODOs or commented-out code.
- Fix or remove a comment your change makes stale; leave untouched comments alone. The existing source has many comments outside this style; do not rewrite them in passing.

## Code layout

- Java under `jars/src/com/fs/starfarer/api/alcoholism/`, one package per area as in [ARCHITECTURE.md](docs/ARCHITECTURE.md#source-owners). The package name is part of the public API; do not move classes.
- New ids, tags and memory keys use the `alcoholism_` / `$alcoholism_` prefix. Existing unprefixed ones stay as they are.
- Order members for reading: constants, static fields, instance fields, constructors, lifecycle and public entry points in call order, helpers near their callers. Preserve initializer order when moving code could change behavior.

## Agent behavior

- Keep the user's selected model. The main agent owns planning, integration, and shipped code. Delegate bounded research only when authorized.
- Treat repository instructions as project requirements and skill guidance as scoped reference material. If a skill blocks work or changes its scope, identify the exact instruction and explain the effect.
- Give brief progress updates and concise final results. Put detailed evidence in the pull request.

### Subagent dispatch template

```text
Task: <specific question or result, and what counts as complete>
Checkout: <absolute path and revision; note relevant uncommitted changes>
Context: <user clarifications, constraints and work already completed>
Access: <allowed sources; permitted writes and paths, if any>
Starting points: <relevant guides, files or symbols, if known>
Return: <desired detail, format and output path, if needed>

1. Read AGENTS.md and CLAUDE.md, then follow the required-reading routes for
   this task. Starting points are navigation help, not the full reading list.
2. Use searches to locate material. Open the surrounding section before using
   a match as evidence; do not answer from filenames or snippets alone.
3. Preserve what each passage applies to: game version, dev, master or mixology-update line,
   vanilla or mod code, consumer mod code. Keep qualifiers such as "normally",
   "only", "never" and "not" when summarizing.
4. For behavioral claims, follow the code to its callers, registration and data.
   Do not invent APIs, names or behavior to fill gaps.
5. Distinguish documented facts, source-verified behavior and your inferences.
   If evidence is missing or conflicts, say so instead of guessing.
6. Return the result with file/line references or source URLs for material
   claims, plus the scope searched and what remains open.
```

The main agent checks the cited evidence before using a conclusion to change code or documentation. A subagent's report does not replace the parent's integration review or the required validation.

### Claude assignments

| Work | Who |
|---|---|
| Planning, architecture, sequencing, and all code changes | The main session, on the user's selected model |
| Repository search, call tracing, reading Starsector, library and consumer mod sources | The `researcher` agent (`.claude/agents/researcher.md`), read-only |

Subagents perform research and scoping, not shipped code.

## Version names

Versions use `major.minor.letter` (`1.1.d`). See [RELEASE.md](docs/RELEASE.md#versions); do not bump versions or publish tags as part of ordinary task work.

## Building

### Required compile gate

After the final runtime-affecting commit and before merge:

1. Fetch and build the exact remote task-branch revision, for example from `git archive` of the pushed commit.
2. Use a clean, empty output directory outside the repository.
3. Compile every file under `jars/src` with JDK 17 (`--release 17`, as in `.idea/misc.xml`) and the dependencies below.
4. Require a successful exit with no compile errors.
5. Record the command, branch commit, JDK version and result in the pull request. The final reply links the PR and states the result and any untested behavior.
6. Stop before merge if the build or a required dependency is unavailable.

Required compile dependencies, all in `lib/` (read-only, copied from Industrial Evolution's `lib/`, game 0.98a). The build fails without any one of them:

- Game jars from `starsector-core`: `starfarer.api.jar`, `starfarer_obf.jar` and `fs.common_obf.jar` (`BlackoutScript` uses `CampaignEngine`), `lwjgl.jar`, `lwjgl_util.jar`, `json.jar`, `log4j-1.2.9.jar`. Never substitute Maven versions.
- `LunaLib.jar` (LunaLib 2.0.1), for `lunalib.lunaSettings`.
- `LazyLib.jar` (LazyLib 3.0.0), for `DefenseUtils`.

Replace a jar only when the user supplies a new version, and note the version here. The game loads only the jars `mod_info.json` lists, so `lib/` never reaches the game; keep it out of the release zip ([RELEASE.md](docs/RELEASE.md#package)).

Reference command, from the repository root:

```sh
OUT=$(mktemp -d)
CP=$(ls lib/*.jar | tr '\n' ':')
javac --release 17 -encoding UTF-8 -cp "$CP" -d "$OUT" $(find jars/src -name '*.java')
```

## Repository navigation

After the required read of this file, follow [Which guide to read](#which-guide-to-read). [ARCHITECTURE.md](docs/ARCHITECTURE.md) is the code and data map, not a substitute for the task-specific guides.
