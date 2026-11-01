package com.fs.starfarer.api.alcoholism.config;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.ModPlugin;
import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;

import java.util.EnumMap;
import java.util.Map;

public final class PlayerSettings implements LunaSettingsListener {

    private static Map<Setting, Object> current = defaults();

    private PlayerSettings() {
    }

    // lunalib keeps listeners in a static list for the whole session, so this one holds no state
    public static void install() {
        if (!LunaSettings.hasSettingsListenerOfClass(PlayerSettings.class)) {
            LunaSettings.addSettingsListener(new PlayerSettings());
        }
        reload();
    }

    public static Object get(Setting setting) {
        Object value = current.get(setting);
        return value != null ? value : setting.fallback;
    }

    @Override
    public void settingsChanged(String modId) {
        if (ModPlugin.MOD_ID.equals(modId)) reload();
    }

    public static void reload() {
        Map<Setting, Object> values = new EnumMap<>(Setting.class);
        for (Setting setting : Setting.values()) values.put(setting, setting.sanitize(read(setting)));
        current = values;

        Global.getLogger(PlayerSettings.class).info("settings " + current);
    }

    private static Object read(Setting setting) {
        if (setting.fallback instanceof Boolean) return LunaSettings.getBoolean(ModPlugin.MOD_ID, setting.id);
        return LunaSettings.getDouble(ModPlugin.MOD_ID, setting.id);
    }

    private static Map<Setting, Object> defaults() {
        Map<Setting, Object> result = new EnumMap<>(Setting.class);
        for (Setting setting : Setting.values()) result.put(setting, setting.fallback);
        return result;
    }
}
