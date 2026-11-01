# Substance.Abuse, dev branch

This `dev` branch holds the unfinished 2.0.a rework of the Starsector mod `alcoholism`. The workflow, guides and compatibility rules live on `master`, the released line: read `CLAUDE.md` on `master` in full before starting a task and follow it here. The two branches share no history; `pre-legacy-master` tags this branch's last commit before the move.

`docs/BRANCHES.md` on `master` describes this line, the API breaks it contains and how its features move to `master`. `docs/MOD_API.md` there lists the symbols other mods use; do not add new breaks here.

What differs on this branch:

- Branch from and merge into `dev`, not `master`.
- The compile gate uses `master`'s dependencies without `LunaLib.jar`, and leaves out `jars/src/com/fs/starfarer/api/alcoholism/ui/reference/` (Industrial Evolution imports, excluded in `.idea/Substance.Abuse.iml`). `memory/CustomAlcohol` imports `org.codehaus.janino.Mod`, so `janino.jar` from `starsector-core` is needed too.
- `.idea/misc.xml` targets Java 7 and `mod_info.json` game `0.96a`; both are out of date against `master`.
- `jars/production/` holds committed IDE output; do not add to it.
