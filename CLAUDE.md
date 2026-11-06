# Substance.Abuse, mixology-update branch

This `mixology-update` branch holds the unfinished 2.0.a rework of the Starsector mod `alcoholism`. The workflow, guides and compatibility rules live on `master`, the released line: read `CLAUDE.md` on `master` in full before starting a task and follow it here. The two branches share no history; `1717edd` is this branch's last commit from before the move.

`docs/BRANCHES.md` on `master` describes this line, the API breaks it contains and how its features move to `master`. `docs/MOD_API.md` there lists the symbols other mods use; do not add new breaks here.

What differs on this branch:

- Branch from and merge into `mixology-update`, not `master`.
- The compile gate uses this branch's own `lib/`: `master`'s jars without `LunaLib.jar`, plus `janino.jar` from `starsector-core` (`memory/CustomAlcohol` imports `org.codehaus.janino.Mod`). It leaves out `jars/src/com/fs/starfarer/api/alcoholism/ui/reference/` (Industrial Evolution imports, excluded in `.idea/Substance.Abuse.iml`). Reference command, from the repository root:

  ```sh
  OUT=$(mktemp -d)
  CP=$(ls lib/*.jar | tr '\n' ':')
  javac --release 17 -encoding UTF-8 -cp "$CP" -d "$OUT" $(find jars/src -name '*.java' -not -path '*/ui/reference/*')
  ```
- `.idea/misc.xml` targets Java 7 and `mod_info.json` game `0.96a`; both are out of date against `master`.
- `jars/production/` holds committed IDE output; do not add to it.
