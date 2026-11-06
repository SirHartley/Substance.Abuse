#!/usr/bin/env bash
# scripted live checks: run.sh GROUP[,GROUP...] from git bash, groups in src/livecheck/Groups.java
# needs a copy of the starsector-native-smoke skill with gamectl.exe built in SMOKE_SKILL
# REF=<commit> tests that commit, default is the working tree. nothing in the checkout is written
# SMOKE_REPO tests another checkout, such as uncommitted work. SMOKE_WORK scratch folder, SMOKE_LOCK machine wide lock, LOCKWAIT seconds to wait for it (default 1800)
# LIMIT, FAST, JFR, KEEP, PROFILE_BASE: see the skill's run-live-checks.sh
HERE=$(cd -- "$(dirname -- "$0")" && pwd)
REPO=$(cd -- "${SMOKE_REPO:-$HERE/../..}" && pwd)
GAME=${SMOKE_GAME:-"/e/Games/Starsector 0.98/Starsector 098a-RC8 - Modding"}
LIVE=${SMOKE_WORK:-/c/Modding/.smoke-work/Substance.Abuse.dev}
SKILL=$(cygpath -u "${SMOKE_SKILL:-/c/Modding/.smoke-work/Substance.Abuse/skill}")
LOCK=${SMOKE_LOCK:-/c/Modding/.smoke-lock}
GROUP=${1:?group}
export SMOKE_JAVAC_DIR=${SMOKE_JAVAC_DIR:-"$USERPROFILE\\.jdks\\corretto-17.0.12\\bin"}
export PATH="$(cygpath -u "$SMOKE_JAVAC_DIR"):$PATH"
CORE="$GAME/starsector-core"
LAZYLIB="$GAME/mods/LazyLib"

w() { ping -n $(( $1 + 1 )) 127.0.0.1 >/dev/null; }
anygame() { powershell -NoProfile -Command "if (Get-Process java -ErrorAction SilentlyContinue | Where-Object { \$_.Path -like '*Starsector*' }) { exit 0 } else { exit 1 }" >/dev/null 2>&1; }
leave() { [[ -n ${HELD:-} ]] && { rm -f "$LOCK/owner.txt"; rmdir "$LOCK"; }; }
trap leave EXIT

# one game per machine: gamectl finds the first game window, so two runs would click each other's game
take_lock() {
    for _ in $(seq 1 $(( ${LOCKWAIT:-1800} / 5 ))); do
        mkdir "$LOCK" 2>/dev/null && { echo "Substance.Abuse $GROUP $(date '+%F %T')" > "$LOCK/owner.txt"; HELD=1; return; }
        [[ -z ${said:-} ]] && { said=1; echo "Waiting for $(cat "$LOCK/owner.txt" 2>/dev/null)"; }
        w 5
    done
    echo "$LOCK still held by $(cat "$LOCK/owner.txt" 2>/dev/null)"; exit 1
}

# the stage is a plain copy, it holds no junctions and is safe to delete
stage="$LIVE/stage/Substance.Abuse"
rm -rf "$LIVE/stage" "$LIVE/classes"
mkdir -p "$stage" "$LIVE/classes"
if [[ -n ${REF:-} ]]; then
    git -C "$REPO" archive "$REF" jars/src data graphics mod_info.json alcohol.version | tar -x -C "$stage" || exit 1
else
    mkdir -p "$stage/jars"
    cp -r "$REPO/jars/src" "$stage/jars/"
    cp -r "$REPO/data" "$REPO/graphics" "$REPO/mod_info.json" "$REPO/alcohol.version" "$stage/"
fi

# same excludes as the idea module, commons-compiler is only there for a stray import
cp_jars=("$CORE/starfarer.api.jar" "$CORE/starfarer_obf.jar" "$CORE/fs.common_obf.jar" "$CORE/lwjgl.jar" "$CORE/lwjgl_util.jar"
    "$CORE/json.jar" "$CORE/log4j-1.2.9.jar" "$CORE/janino.jar" "$CORE/commons-compiler.jar" "$LAZYLIB/jars/LazyLib.jar")
cp=$(for j in "${cp_jars[@]}"; do cygpath -m "$j"; done | paste -sd ';')
empty=$(mktemp -d)
(cd "$stage/jars/src" && find . -name '*.java' | grep -vE '/(ui/reference|scripts/legioNuclearOption)/') > "$LIVE/sources.txt"
out=$(cd "$stage/jars/src" && javac --release 17 -nowarn -encoding UTF-8 -sourcepath "$(cygpath -m "$empty")" -implicit:none \
    -cp "$cp" -d "$(cygpath -m "$LIVE/classes")" "@$(cygpath -m "$LIVE/sources.txt")" 2>&1) ||
    { echo "$out" | grep -v '^Note: ' | tail -30; echo "COMPILE_FAILED: ${REF:-working tree} does not build"; exit 1; }
rm -rf "$stage/jars/src"
jar cf "$stage/jars/alcoholism.jar" -C "$(cygpath -m "$LIVE/classes")" .

mod="$LIVE/live_checks"
rm -rf "$mod"; mkdir -p "$mod"; cp -r "$HERE/src" "$HERE/data" "$HERE/mod_info.json" "$mod/"

take_lock
anygame && { echo "A game is running. Close it or wait for its run to end."; exit 1; }

CHECK_JARS="$(cygpath -m "$stage/jars/alcoholism.jar");$(cygpath -m "$LAZYLIB/jars/LazyLib.jar")" \
PROFILE_PKG=${PROFILE_PKG:-com.fs.starfarer.api.alcoholism} \
    bash "$SKILL/scripts/live/run-live-checks.sh" "$GAME" "$LIVE" "$mod" "$GROUP" lw_lazylib "$stage"
