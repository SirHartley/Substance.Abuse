package com.fs.starfarer.api.alcoholism.tools;

import java.util.Map;

public final class ReleaseBuildChecks {

    private static int checks;

    private ReleaseBuildChecks() {
    }

    public static void main(String[] args) {
        checkNotes();
        checkStamping();
        checkFiles();
        checkAssets();
        System.out.println("Release build: " + checks + " checks passed");
    }

    private static void checkNotes() {
        String changelog = "header\n\n1.1.e - UNRELEASED\nAdded - New thing\n\n1.1.d\nFixed - Old thing\n";
        ReleaseBuild.Notes preview = ReleaseBuild.notes(changelog, null);
        require(preview.version().equals("1.1.e") && preview.unreleased(), "preview reads newest section");
        require(!preview.text().contains("Old thing"), "notes stop at the next section");
        fails(() -> ReleaseBuild.notes(changelog, "1.1.e"), "release rejects UNRELEASED");
        fails(() -> ReleaseBuild.notes(changelog, "1.1.d"), "version must match newest section");
        fails(() -> ReleaseBuild.notes(changelog, "v1.1.e"), "version has no v prefix");
        String released = changelog.replace(" - UNRELEASED", "");
        require(ReleaseBuild.notes(released, "1.1.e").version().equals("1.1.e"), "release accepts released section");
        fails(() -> ReleaseBuild.notes("1.0.a\nNothing here\n", null), "section needs a change line");
        fails(() -> ReleaseBuild.notes("1.0.a\nAdded - x\n1.0.a\nAdded - y\n", null), "duplicate versions");
    }

    private static void checkStamping() {
        String file = "{\"masterVersionFile\":\"old\",\"modVersion\":{\"major\":1,\"minor\":1,\"patch\":\"d\"},\n"
                + "#comment\n\"directDownloadURL\":\"x\",\"changelogURL\":\"y\",}";
        String stamped = ReleaseBuild.stampVersion(file, "2.3.c");
        require(stamped.contains("\"major\":2") && stamped.contains("\"minor\":3")
                && stamped.contains("\"patch\":\"c\""), "version file stamped");
        require(stamped.contains("/" + ReleaseBuild.REPO + "/releases/download/2.3.c/Substance.Abuse2.3.c.zip"),
                "download url stamped");
        require(stamped.contains("/" + ReleaseBuild.REPO + "/refs/heads/master/alcohol.version"), "master url stamped");
        require(stamped.contains("/" + ReleaseBuild.REPO + "/refs/heads/master/Changelog"), "changelog url stamped");
        require(ReleaseBuild.stampInfo("{\"version\": \"0.1\"}", "2.3.c").contains("\"2.3.c\""), "mod_info stamped");
        fails(() -> ReleaseBuild.stampInfo("{}", "2.3.c"), "missing field rejected");
    }

    private static void checkFiles() {
        require(ReleaseBuild.releaseFile("Changelog"), "changelog shipped");
        require(ReleaseBuild.releaseFile("_substanceAbuse.png"), "luna icon shipped");
        require(ReleaseBuild.releaseFile("CC-license.png"), "license image shipped");
        require(ReleaseBuild.releaseFile("data/campaign/special_items.csv"), "data shipped");
        require(ReleaseBuild.releaseFile("graphics/items/absynth.png"), "graphics shipped");
        require(!ReleaseBuild.releaseFile("docs/ARCHITECTURE.md"), "docs skipped");
        require(!ReleaseBuild.releaseFile("CLAUDE.md"), "agent files skipped");
        require(!ReleaseBuild.releaseFile("lib/LunaLib.jar"), "compile libs skipped");
        require(!ReleaseBuild.releaseFile("jars/src/com/fs/starfarer/api/alcoholism/ModPlugin.java"), "source skipped");
        require(!ReleaseBuild.releaseFile("graphics/unused/old.png"), "unused art skipped");
        require(!ReleaseBuild.releaseFile("graphics/items/art.psd"), "source art skipped");
        require(!ReleaseBuild.releaseFile("data/.idea/x.json"), "hidden dirs skipped");
        require(!ReleaseBuild.runtimeClass(ReleaseBuild.PACKAGE + "tools/ReleaseBuild.class"), "tools left out of jar");
        require(ReleaseBuild.runtimeClass(ReleaseBuild.PLUGIN), "runtime classes kept");
    }

    private static void checkAssets() {
        String f = ReleaseBuild.FOLDER + "/";
        byte[] png = new byte[0];
        byte[] settings = "\"bg\":\"graphics/effects/bg.png\",\"intel\":\"graphics/icons/intel/investigation.png\""
                .getBytes();
        ReleaseBuild.checkAssets(Map.of(f + "data/config/settings.json", settings, f + "graphics/effects/bg.png", png));
        checks++;
        fails(() -> ReleaseBuild.checkAssets(Map.of(f + "data/config/settings.json", settings,
                f + "graphics/effects/frame.png", png)), "missing mod asset rejected");
    }

    private static void fails(Runnable action, String message) {
        try {
            action.run();
        } catch (IllegalStateException expected) {
            checks++;
            return;
        }
        throw new AssertionError(message);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }
}
