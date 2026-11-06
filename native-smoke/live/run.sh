#!/usr/bin/env bash
# scripted live checks: run.sh GROUP[,GROUP...] from git bash, groups in src/livecheck/Groups.java
# BUILD picks the mod: 1.1.e the release zip (default), 1.1.d the baseline commit 1f15cd6 with its committed jar,
# any other git ref is compiled with lib/. every build has its own work folder and base save
# needs a copy of the starsector-native-smoke skill with gamectl.exe built in SMOKE_SKILL
# SMOKE_WORK scratch folder, SMOKE_LOCK machine wide lock, LOCKWAIT seconds to wait for it (default 1800)
# PREPARE=1 stages and builds, runs no game. LIMIT, FAST, JFR, KEEP, PROFILE_BASE: see the skill's run-live-checks.sh
HERE=$(cd -- "$(dirname -- "$0")" && pwd)
REPO=$(cd -- "$HERE/../.." && pwd)
GAME=${SMOKE_GAME:-"/e/Games/Starsector 0.98/Starsector 098a-RC8 - Modding"}
BUILD=${BUILD:-1.1.e}
LIVE=${SMOKE_WORK:-/c/Modding/.smoke-work/Substance.Abuse.$BUILD}
SKILL=$(cygpath -u "${SMOKE_SKILL:-/c/Modding/.smoke-work/Substance.Abuse/skill}")
LOCK=${SMOKE_LOCK:-/c/Modding/.smoke-lock}
GROUP=${1:?group}
export SMOKE_JAVAC_DIR=${SMOKE_JAVAC_DIR:-"$USERPROFILE\\.jdks\\corretto-17.0.12\\bin"}
export PATH="$(cygpath -u "$SMOKE_JAVAC_DIR"):$PATH"
LAZYLIB="$GAME/mods/LazyLib"
LUNALIB="$GAME/mods/LunaLib"

w() { ping -n $(( $1 + 1 )) 127.0.0.1 >/dev/null; }
anygame() { powershell -NoProfile -Command "if (Get-Process java -ErrorAction SilentlyContinue | Where-Object { \$_.Path -like '*Starsector*' }) { exit 0 } else { exit 1 }" >/dev/null 2>&1; }
leave() { [[ -n ${HELD:-} ]] && { rm -f "$LOCK/owner.txt"; rmdir "$LOCK"; }; }
trap leave EXIT

# one game per machine: gamectl finds the first game window, so two runs would click each other's game
take_lock() {
    for _ in $(seq 1 $(( ${LOCKWAIT:-1800} / 5 ))); do
        mkdir "$LOCK" 2>/dev/null && { echo "Substance.Abuse $BUILD $GROUP $(date '+%F %T')" > "$LOCK/owner.txt"; HELD=1; return; }
        [[ -z ${said:-} ]] && { said=1; echo "Waiting for $(cat "$LOCK/owner.txt" 2>/dev/null)"; }
        w 5
    done
    echo "$LOCK still held by $(cat "$LOCK/owner.txt" 2>/dev/null)"; exit 1
}

# the stage is a plain copy, it holds no junctions and is safe to delete
stage="$LIVE/stage/Substance.Abuse"
rm -rf "$LIVE/stage"
mkdir -p "$LIVE/stage" "$LIVE/release"
case $BUILD in
    1.1.e)
        zip="$LIVE/release/Substance.Abuse1.1.e.zip"
        [[ -f $zip ]] || gh release download 1.1.e -R SirHartley/Substance.Abuse -p 'Substance.Abuse1.1.e.zip' -D "$LIVE/release" || exit 1
        unzip -q "$zip" -d "$LIVE/stage" || exit 1
        ;;
    1.1.d)
        mkdir -p "$stage"
        git -C "$REPO" archive 1f15cd6 data graphics jars/alcoholism.jar mod_info.json alcohol.version | tar -x -C "$stage" || exit 1
        ;;
    *)
        mkdir -p "$stage/jars" "$LIVE/classes"
        git -C "$REPO" archive "$BUILD" jars/src data graphics lib mod_info.json alcohol.version | tar -x -C "$stage" || exit 1
        cp=$(ls "$stage"/lib/*.jar | while read -r j; do cygpath -m "$j"; done | paste -sd ';')
        (cd "$stage/jars/src" && find . -name '*.java' -not -path '*/tools/*') > "$LIVE/sources.txt"
        out=$(cd "$stage/jars/src" && javac --release 17 -nowarn -encoding UTF-8 -cp "$cp" -d "$(cygpath -m "$LIVE/classes")" "@$(cygpath -m "$LIVE/sources.txt")" 2>&1) ||
            { echo "$out" | grep -v '^Note: ' | tail -30; echo "COMPILE_FAILED: $BUILD does not build"; exit 1; }
        jar cf "$stage/jars/alcoholism.jar" -C "$(cygpath -m "$LIVE/classes")" .
        rm -rf "$stage/jars/src" "$stage/lib"
        ;;
esac

mod="$LIVE/live_checks"
rm -rf "$mod"; mkdir -p "$mod"; cp -r "$HERE/src" "$HERE/data" "$HERE/mod_info.json" "$mod/"
export CHECK_JARS="$(cygpath -m "$stage/jars/alcoholism.jar");$(cygpath -m "$LUNALIB/jars/LunaLib.jar");$(cygpath -m "$LAZYLIB/jars/LazyLib.jar");$(cygpath -m "$LAZYLIB/jars/internal/Kotlin-Runtime.jar")"
if [[ -n ${PREPARE:-} ]]; then
    bash "$SKILL/scripts/live/build-test-mod.sh" "$GAME" "$mod" live_checks "$stage/jars/alcoholism.jar" "$LUNALIB/jars/LunaLib.jar" \
        "$LAZYLIB/jars/LazyLib.jar" "$LAZYLIB/jars/internal/Kotlin-Runtime.jar" "$GAME/starsector-core/starfarer_obf.jar" "$GAME/starsector-core/fs.common_obf.jar"
    exit $?
fi

take_lock
anygame && { echo "A game is running. Close it or wait for its run to end."; exit 1; }

PROFILE_PKG=${PROFILE_PKG:-com.fs.starfarer.api.alcoholism} \
    bash "$SKILL/scripts/live/run-live-checks.sh" "$GAME" "$LIVE" "$mod" "$GROUP" lw_lazylib lunalib "$stage"
